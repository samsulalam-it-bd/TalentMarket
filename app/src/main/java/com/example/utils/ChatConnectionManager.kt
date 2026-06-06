package com.example.utils

import android.util.Log
import com.example.data.ChatMessage
import com.example.data.ChatRoom
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ChatConnectionManager {
    private const val TAG = "ChatConnectionManager"
    
    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        RECONNECTING,
        ERROR
    }

    private val _roomsStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val roomsStatus: StateFlow<ConnectionStatus> = _roomsStatus.asStateFlow()

    private val _messagesStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val messagesStatus: StateFlow<ConnectionStatus> = _messagesStatus.asStateFlow()

    private var roomsListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    
    private var scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var roomsRetryJob: Job? = null
    private var messagesRetryJob: Job? = null

    // Logging helper
    fun logEvent(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, "🟢 [CHAT] $message", throwable)
        } else {
            Log.d(TAG, "🟢 [CHAT] $message")
        }
    }

    /**
     * Sanitizes and validates the chat message input.
     * Trims leading/trailing spacing, caps length at 5000 chars, and sanitizes potential HTML/Script injections.
     * Returns the sanitized text, or null if the message is blank or empty.
     */
    fun sanitizeAndValidateMessage(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        
        // Max length validation (5,000 characters)
        val capped = if (trimmed.length > 5000) {
            trimmed.substring(0, 5000)
        } else {
            trimmed
        }
        
        // Input sanitization to render scripts/HTML neutral and safe from mallicious formatting
        return capped
            .replace("<script>", "[script]", ignoreCase = true)
            .replace("</script>", "[/script]", ignoreCase = true)
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(Regex("(?i)\\bjavascript:"), "[safe_javascript:]")
    }

    fun startListeningRooms(
        firestore: FirebaseFirestore?,
        userId: String,
        onUpdate: (List<ChatRoom>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (!scope.isActive) {
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        }
        if (firestore == null || userId.isBlank()) {
            _roomsStatus.value = ConnectionStatus.ERROR
            logEvent("Cannot start rooms listener: firestore is null or userId is empty")
            return
        }

        roomsRetryJob?.cancel()
        roomsListener?.remove()
        
        logEvent("Registering/Reconnecting Chat Rooms Listener for user: $userId")
        _roomsStatus.value = ConnectionStatus.RECONNECTING

        var retryDelay = 2000L

        fun connect() {
            roomsListener = firestore.collection("chatRooms")
                .whereArrayContains("participants", userId)
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        logEvent("Rooms listener error: ${e.message}", e)
                        _roomsStatus.value = ConnectionStatus.ERROR
                        onError(e)
                        
                        // Schedule retry
                        roomsRetryJob?.cancel()
                        roomsRetryJob = scope.launch {
                            logEvent("Scheduling rooms listener reconnect attempt in ${retryDelay}ms")
                            delay(retryDelay)
                            retryDelay = (retryDelay * 2).coerceAtMost(30000L) // Exponential backoff
                            connect()
                        }
                        return@addSnapshotListener
                    }

                    // On successful update, reset backoff delay
                    retryDelay = 2000L
                    _roomsStatus.value = ConnectionStatus.CONNECTED
                    logEvent("Rooms listener successfully connected/received updates (document count: ${snap?.size() ?: 0})")

                    try {
                        val rooms = snap?.documents?.mapNotNull { doc ->
                            try {
                                doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                            } catch (ex: Exception) {
                                logEvent("Serialization error parsing room ${doc.id}: ${ex.message}", ex)
                                null
                            }
                        } ?: emptyList()
                        onUpdate(rooms)
                    } catch (ex: Exception) {
                        logEvent("Critical error parsing rooms snapshot: ${ex.message}", ex)
                    }
                }
        }

        connect()
    }

    fun stopListeningRooms() {
        roomsRetryJob?.cancel()
        roomsListener?.remove()
        roomsListener = null
        _roomsStatus.value = ConnectionStatus.DISCONNECTED
        logEvent("Chat Rooms listener stopped")
    }

    fun startListeningMessages(
        firestore: FirebaseFirestore?,
        roomId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (!scope.isActive) {
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        }
        if (firestore == null || roomId.isBlank()) {
            _messagesStatus.value = ConnectionStatus.ERROR
            logEvent("Cannot start messages listener: firestore is null or roomId is empty")
            return
        }

        messagesRetryJob?.cancel()
        messagesListener?.remove()

        logEvent("Registering/Reconnecting Messages Listener for room: $roomId")
        _messagesStatus.value = ConnectionStatus.RECONNECTING

        var retryDelay = 2000L

        fun connect() {
            messagesListener = firestore.collection("chatRooms")
                .document(roomId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        logEvent("Messages listener error: ${e.message}", e)
                        _messagesStatus.value = ConnectionStatus.ERROR
                        onError(e)

                        // Schedule retry
                        messagesRetryJob?.cancel()
                        messagesRetryJob = scope.launch {
                            logEvent("Scheduling messages listener reconnect attempt in ${retryDelay}ms")
                            delay(retryDelay)
                            retryDelay = (retryDelay * 2).coerceAtMost(30000L) // Exponential backoff
                            connect()
                        }
                        return@addSnapshotListener
                    }

                    // On successful update, reset backoff delay
                    retryDelay = 2000L
                    _messagesStatus.value = ConnectionStatus.CONNECTED
                    logEvent("Messages listener successfully connected/received updates (message count: ${snap?.size() ?: 0})")

                    try {
                        val msgs = snap?.documents?.mapNotNull { doc ->
                            try {
                                val hasPending = doc.metadata.hasPendingWrites()
                                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id, isPending = hasPending)
                            } catch (ex: Exception) {
                                logEvent("Serialization error parsing message ${doc.id}: ${ex.message}", ex)
                                null
                            }
                        } ?: emptyList()
                        onUpdate(msgs)
                    } catch (ex: Exception) {
                        logEvent("Critical error parsing messages snapshot: ${ex.message}", ex)
                    }
                }
        }

        connect()
    }

    fun stopListeningMessages() {
        messagesRetryJob?.cancel()
        messagesListener?.remove()
        messagesListener = null
        _messagesStatus.value = ConnectionStatus.DISCONNECTED
        logEvent("Messages listener stopped")
    }
    
    fun cleanUp() {
        stopListeningRooms()
        stopListeningMessages()
        try {
            scope.cancel()
        } catch (e: Exception) {
            logEvent("Error cancelling scope during cleanUp", e)
        }
        logEvent("All listener connections cleaned up and scope cancelled")
    }
}
