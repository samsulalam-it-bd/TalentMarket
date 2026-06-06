package com.example.ui

import com.google.firebase.auth.FirebaseAuth

object UserService {

    fun getDisplayName(
        firestoreUserData: Map<String, Any>?,
        firebaseAuthUser: com.google.firebase.auth.FirebaseUser? = null
    ): String {
        // Priority 1: Firestore name
        val firestoreName = firestoreUserData?.get("name")?.toString()?.trim()
        if (!firestoreName.isNullOrEmpty() && !firestoreName.contains("@")) {
            return firestoreName
        }

        // Priority 2: Firebase Auth displayName
        val resolvedUser = firebaseAuthUser ?: try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        val authDisplayName = resolvedUser?.displayName?.trim()
        if (!authDisplayName.isNullOrEmpty() && !authDisplayName.contains("@")) {
            return authDisplayName
        }

        // Priority 3: NEVER show email or email prefix
        // Show "User" as fallback instead
        return "User"
    }
    
    /**
     * Use this for other users' names (like in Job Cards, Workers List)
     * where we only have the raw name string.
     */
    fun sanitizeName(rawName: String?): String {
        val trimmed = rawName?.trim()
        if (trimmed.isNullOrEmpty() || trimmed.contains("@")) {
            return "User"
        }
        return trimmed
    }

    /**
     * Checks if a fallback bottom sheet prompt is required.
     */
    fun isNameUpdateRequired(
        firestoreUserData: Map<String, Any>?,
        firebaseAuthUser: com.google.firebase.auth.FirebaseUser? = null
    ): Boolean {
        val resolvedUser = firebaseAuthUser ?: try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        if (resolvedUser == null || firestoreUserData == null) return false
        val currentName = getDisplayName(firestoreUserData, resolvedUser)
        return currentName == "User" || (firestoreUserData["name"]?.toString()?.contains("@") == true) || firestoreUserData["name"]?.toString()?.trim()?.isEmpty() == true
    }

    /**
     * Determines a suggested name to pre-fill the name update prompt.
     */
    fun getSuggestedName(firebaseAuthUser: com.google.firebase.auth.FirebaseUser?): String {
        val authDisplayName = firebaseAuthUser?.displayName?.trim()
        if (!authDisplayName.isNullOrEmpty() && !authDisplayName.contains("@")) {
            return authDisplayName
        }
        return ""
    }
}
