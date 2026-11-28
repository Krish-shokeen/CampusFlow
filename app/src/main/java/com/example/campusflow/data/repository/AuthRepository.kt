package com.example.campusflow.data.repository

import com.example.campusflow.data.remote.AuthValidator
import com.example.campusflow.data.remote.FirebaseErrorMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val _authStateFlow = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val authStateFlow: StateFlow<FirebaseUser?> = _authStateFlow.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _authStateFlow.value = auth.currentUser
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val currentUid: String?
        get() = firebaseAuth.currentUser?.uid

    val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<FirebaseUser> {
        AuthValidator.validateRegistration(fullName, email, password, confirmPassword)?.let { error ->
            return Result.failure(IllegalArgumentException(error))
        }

        return try {
            val cleanName = fullName.trim()
            val cleanEmail = email.trim()
            val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Registration succeeded but user session is unavailable."))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            user.updateProfile(profileUpdates).await()

            initializeUserProfileDocument(user, cleanName)
            _authStateFlow.value = firebaseAuth.currentUser
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(FirebaseErrorMapper.map(e), e))
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        AuthValidator.validateLogin(email, password)?.let { error ->
            return Result.failure(IllegalArgumentException(error))
        }

        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Sign in failed. Please try again."))

            ensureProfileExists(user)
            _authStateFlow.value = firebaseAuth.currentUser
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(FirebaseErrorMapper.map(e), e))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        AuthValidator.validateEmail(email)?.let { error ->
            return Result.failure(IllegalArgumentException(error))
        }

        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(FirebaseErrorMapper.map(e), e))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _authStateFlow.value = null
    }

    private suspend fun initializeUserProfileDocument(user: FirebaseUser, displayName: String) {
        val uid = user.uid
        val userDocRef = firestore.collection("users").document(uid)
        val profileData = mapOf(
            "uid" to uid,
            "ownerUid" to uid,
            "name" to displayName,
            "email" to (user.email ?: ""),
            "university" to "K.R. Mangalam University",
            "course" to "B.Tech",
            "semester" to "Semester V",
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDocRef.set(profileData, SetOptions.merge()).await()

        val prefsData = mapOf(
            "ownerUid" to uid,
            "themeMode" to "SYSTEM",
            "notificationsEnabled" to true,
            "dailyStudyGoalMinutes" to 300,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDocRef.collection("settings").document("preferences")
            .set(prefsData, SetOptions.merge())
            .await()
    }

    private suspend fun ensureProfileExists(user: FirebaseUser) {
        val userDocRef = firestore.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()
        if (!snapshot.exists()) {
            val fallbackName = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: "Student"
            initializeUserProfileDocument(user, fallbackName)
        }
    }
}
