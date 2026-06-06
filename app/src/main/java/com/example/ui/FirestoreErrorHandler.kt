package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object FirestoreErrorHandler {
    @Volatile var appContext: Context? = null
    private val handlerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _compositeIndexErrors = MutableSharedFlow<IndexErrorInfo>()
    val compositeIndexErrors = _compositeIndexErrors.asSharedFlow()

    data class IndexErrorInfo(
        val originalMessage: String,
        val url: String
    )

    fun handleError(error: Throwable?, contextTag: String = "Firestore") {
        if (error == null) return
        val msg = error.message ?: ""
        
        if (msg.contains("index", ignoreCase = true) || 
            msg.contains("FAILED_PRECONDITION", ignoreCase = true) || 
            msg.contains("failed-precondition", ignoreCase = true)
        ) {
            val regex = "https://[a-zA-Z0-9./?_=-]+".toRegex()
            val match = regex.find(msg)
            // Use the real user's project console database indexes URL as fallback
            val url = match?.value ?: "https://console.firebase.google.com/project/jobsite-85482/firestore/indexes"
            
            // Format requested precisely with CLICKABLE link label
            val customMsg = "[CLICKABLE LINK TO CREATE INDEX]: $url"
            
            Log.e("FirebaseConsole", "========================================================")
            Log.e("FirebaseConsole", "FAILED-PRECONDITION / MISSING INDEX ERROR DETECTED")
            Log.e("FirebaseConsole", "[CLICKABLE LINK TO CREATE INDEX]: $url")
            Log.e("FirebaseConsole", "========================================================")
            
            System.err.println(customMsg)
            System.out.println(customMsg)

            handlerScope.launch {
                try {
                    _compositeIndexErrors.emit(IndexErrorInfo(msg, url))
                } catch (e: Exception) {
                    Log.e("FirestoreErrorHandler", "Failed to emit index error", e)
                }
            }
        } else {
            Log.e("FirebaseError", "$contextTag error: ", error)
            
            // Emit or show general toast error safely
            val toastMessage = "Firebase Error ($contextTag): ${error.localizedMessage ?: msg}"
            appContext?.let { ctx ->
                handlerScope.launch {
                    try {
                        Toast.makeText(ctx, toastMessage, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Wraps a com.google.android.gms.tasks.Task with automatic error handling.
     */
    fun <T> wrapTask(task: com.google.android.gms.tasks.Task<T>, contextTag: String = "Firestore"): com.google.android.gms.tasks.Task<T> {
        return task.addOnFailureListener { exception ->
            handleError(exception, contextTag)
        }
    }

    /**
     * Executes a Firestore query / task block with try-catch wrapping.
     */
    inline fun <R> wrapQuery(contextTag: String = "Firestore", block: () -> R): R? {
        return try {
            block()
        } catch (e: Exception) {
            handleError(e, contextTag)
            null
        }
    }

    fun openIndexUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("FirestoreErrorHandler", "Failed to open index URL", e)
            Toast.makeText(context, "Cannot open browser. Please copy link instead.", Toast.LENGTH_LONG).show()
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Firebase Index URL", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "লিংকটি ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FirestoreErrorHandler", "Failed to copy text", e)
        }
    }
}
