package com.example.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {

    private val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }

    val currentUserId: String get() = auth?.currentUser?.uid ?: ""

    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    private val _chatRooms = MutableStateFlow<List<com.example.data.ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<com.example.data.ChatRoom>> = _chatRooms.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<com.example.data.ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<com.example.data.ChatMessage>> = _currentMessages.asStateFlow()

    private fun logFirestoreError(error: Throwable?) {
        if (error == null) return
        val msg = error.message ?: ""
        android.util.Log.e("ChatViewModel", "Firestore Error occurred: $msg", error)
        val isPermissionError = msg.contains("permission", ignoreCase = true) || 
                                 msg.contains("denied", ignoreCase = true)
        if (isPermissionError) return
        // You could emit to _uiMessage if needed, optionally
    }

    fun loadChatRooms() {
        val currentUserId = auth?.currentUser?.uid ?: return
        com.example.utils.ChatConnectionManager.startListeningRooms(
            firestore = firestore,
            userId = currentUserId,
            onUpdate = { rooms ->
                _chatRooms.value = rooms.sortedByDescending { it.lastMessageAt }
            },
            onError = { err ->
                logFirestoreError(err)
            }
        )
    }

    fun startChat(
        otherUserId: String,
        otherUserName: String,
        otherUserPhoto: String,
        onRoomCreated: (String) -> Unit
    ) {
        val currentUserId = auth?.currentUser?.uid ?: return
        val currentUserName = auth?.currentUser?.displayName ?: "User"
        val currentUserPhoto = auth?.currentUser?.photoUrl?.toString() ?: ""

        // Try local list cache first
        val existingRoom = _chatRooms.value.find { it.participants.contains(otherUserId) }
        if (existingRoom != null) {
            onRoomCreated(existingRoom.id)
            return
        }

        // Query Firestore to guarantee there are no remote duplicate rooms
        firestore?.collection("chatRooms")
            ?.whereArrayContains("participants", currentUserId)
            ?.get()
            ?.addOnSuccessListener { snap ->
                val fbExistingRoom = try {
                    snap?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(com.example.data.ChatRoom::class.java)?.copy(id = doc.id)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            null
                        }
                    } ?: emptyList()
                } catch (ex: Exception) {
                    emptyList()
                }.find { it.participants.contains(otherUserId) }
                
                if (fbExistingRoom != null) {
                    onRoomCreated(fbExistingRoom.id)
                } else {
                    // Create new room if none exists
                    val roomId = firestore?.collection("chatRooms")?.document()?.id ?: return@addOnSuccessListener
                    val newRoom = com.example.data.ChatRoom(
                        id = roomId,
                        participants = listOf(currentUserId, otherUserId),
                        participantNames = mapOf(currentUserId to currentUserName, otherUserId to otherUserName),
                        participantPhotos = mapOf(currentUserId to currentUserPhoto, otherUserId to otherUserPhoto),
                        lastMessage = "",
                        lastMessageAt = System.currentTimeMillis()
                    )

                    firestore?.collection("chatRooms")?.document(roomId)?.set(newRoom)
                        ?.addOnSuccessListener { onRoomCreated(roomId) }
                        ?.addOnFailureListener { e ->
                            _uiMessage.tryEmit(e.message ?: "Failed to start chat")
                        }
                }
            }
            ?.addOnFailureListener {
                // Failure fallback: create a new room
                val roomId = firestore?.collection("chatRooms")?.document()?.id ?: return@addOnFailureListener
                val newRoom = com.example.data.ChatRoom(
                    id = roomId,
                    participants = listOf(currentUserId, otherUserId),
                    participantNames = mapOf(currentUserId to currentUserName, otherUserId to otherUserName),
                    participantPhotos = mapOf(currentUserId to currentUserPhoto, otherUserId to otherUserPhoto),
                    lastMessage = "",
                    lastMessageAt = System.currentTimeMillis()
                )

                firestore?.collection("chatRooms")?.document(roomId)?.set(newRoom)
                    ?.addOnSuccessListener { onRoomCreated(roomId) }
                    ?.addOnFailureListener { e ->
                        _uiMessage.tryEmit(e.message ?: "Failed to start chat")
                    }
            }
    }

    fun loadMessages(roomId: String) {
        com.example.utils.ChatConnectionManager.startListeningMessages(
            firestore = firestore,
            roomId = roomId,
            onUpdate = { msgs ->
                _currentMessages.value = msgs
            },
            onError = { err ->
                logFirestoreError(err)
            }
        )
    }

    fun sendMessage(roomId: String, text: String, otherUserId: String) {
        val currentUserId = auth?.currentUser?.uid ?: return
        val sanitizedText = com.example.utils.ChatConnectionManager.sanitizeAndValidateMessage(text)
        if (sanitizedText == null) {
            com.example.utils.ChatConnectionManager.logEvent("Blocking send: message input is empty or invalid after sanitization.")
            return
        }

        val messageId = firestore?.collection("chatRooms")?.document(roomId)?.collection("messages")?.document()?.id ?: return
        val chatMessage = com.example.data.ChatMessage(
            id = messageId,
            roomId = roomId,
            senderId = currentUserId,
            text = sanitizedText,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        com.example.utils.ChatConnectionManager.logEvent("Sending chat message: Room ID: $roomId, length: ${sanitizedText.length}")

        firestore?.collection("chatRooms")?.document(roomId)?.collection("messages")?.document(messageId)?.set(chatMessage)
            ?.addOnFailureListener { e ->
                com.example.utils.ChatConnectionManager.logEvent("Failed to send message: ${e.message}", e)
            }

        // Update last message and increment unread count for other user
        val updates = hashMapOf<String, Any>(
            "lastMessage" to sanitizedText,
            "lastMessageAt" to System.currentTimeMillis(),
            "unreadCounts.$otherUserId" to com.google.firebase.firestore.FieldValue.increment(1)
        )
        firestore?.collection("chatRooms")?.document(roomId)?.update(updates)
            ?.addOnFailureListener { e ->
                com.example.utils.ChatConnectionManager.logEvent("Failed to update chatRoom info: ${e.message}", e)
            }
    }

    fun markMessagesAsRead(roomId: String) {
        val currentUserId = auth?.currentUser?.uid ?: return
        
        // Reset unread count for current user
        firestore?.collection("chatRooms")?.document(roomId)
            ?.update("unreadCounts.$currentUserId", 0)

        // Mark messages from other user as read
        firestore?.collection("chatRooms")
            ?.document(roomId)
            ?.collection("messages")
            ?.whereNotEqualTo("senderId", currentUserId)
            ?.get()
            ?.addOnSuccessListener { snap ->
                val batch = firestore?.batch() ?: return@addOnSuccessListener
                var updated = false
                if (snap != null) {
                    for (doc in snap.documents) {
                        val isRead = doc.getBoolean("isRead") ?: false
                        if (!isRead) {
                            batch.update(doc.reference, "isRead", true)
                            updated = true
                        }
                    }
                }
                if (updated) {
                    batch.commit()
                }
            }
    }

    fun leaveChatRoom() {
        com.example.utils.ChatConnectionManager.stopListeningMessages()
        _currentMessages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        com.example.utils.ChatConnectionManager.cleanUp()
    }
}
