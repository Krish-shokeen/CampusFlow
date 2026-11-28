package com.example.campusflow.data.repository

import android.net.Uri
import android.util.Base64
import com.example.campusflow.data.preferences.UserPreferencesRepository
import com.example.campusflow.data.preferences.UserProfile
import com.example.campusflow.data.remote.FirebaseErrorMapper
import com.example.campusflow.ui.theme.ThemeMode
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferencesRepository: UserPreferencesRepository
) {

    val userProfileFlow: Flow<UserProfile> = authRepository.authStateFlow.flatMapLatest { user ->
        if (user == null) {
            flowOf(UserProfile())
        } else {
            val uid = user.uid
            val userDocFlow = callbackFlow {
                val listener = firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        trySend(snapshot)
                    }
                awaitClose { listener.remove() }
            }
            val prefsDocFlow = callbackFlow {
                val listener = firestore.collection("users").document(uid)
                    .collection("settings").document("preferences")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        trySend(snapshot)
                    }
                awaitClose { listener.remove() }
            }

            combine(userDocFlow, prefsDocFlow) { userSnap, prefsSnap ->
                val rawTheme = prefsSnap?.getString("themeMode")
                val parsedTheme = rawTheme?.let {
                    runCatching { ThemeMode.valueOf(it) }.getOrNull()
                } ?: ThemeMode.SYSTEM

                UserProfile(
                    uid = uid,
                    email = userSnap?.getString("email") ?: (user.email ?: ""),
                    name = userSnap?.getString("name")
                        ?: user.displayName?.takeIf { it.isNotBlank() }
                        ?: "Student",
                    university = userSnap?.getString("university") ?: "K.R. Mangalam University",
                    course = userSnap?.getString("course") ?: "B.Tech",
                    semester = userSnap?.getString("semester") ?: "Semester V",
                    photoUrl = userSnap?.getString("photoUrl") ?: (user.photoUrl?.toString() ?: ""),
                    themeMode = parsedTheme,
                    notificationsEnabled = prefsSnap?.getBoolean("notificationsEnabled") ?: true,
                    dailyStudyGoalMinutes = (prefsSnap?.getLong("dailyStudyGoalMinutes") ?: 300L).toInt()
                )
            }.onEach { profile ->
                preferencesRepository.updateProfile(profile)
            }
        }
    }

    suspend fun fetchProfile(): Result<UserProfile> {
        val user = authRepository.currentUser
            ?: return Result.failure(IllegalStateException("Not authenticated."))

        return try {
            val uid = user.uid
            val userSnap = firestore.collection("users").document(uid).get().await()
            val prefsSnap = firestore.collection("users").document(uid)
                .collection("settings").document("preferences").get().await()

            val rawTheme = prefsSnap.getString("themeMode")
            val parsedTheme = rawTheme?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM

            val profile = UserProfile(
                uid = uid,
                email = userSnap.getString("email") ?: (user.email ?: ""),
                name = userSnap.getString("name") ?: (user.displayName ?: "Student"),
                university = userSnap.getString("university") ?: "K.R. Mangalam University",
                course = userSnap.getString("course") ?: "B.Tech",
                semester = userSnap.getString("semester") ?: "Semester V",
                photoUrl = userSnap.getString("photoUrl") ?: "",
                themeMode = parsedTheme,
                notificationsEnabled = prefsSnap.getBoolean("notificationsEnabled") ?: true,
                dailyStudyGoalMinutes = (prefsSnap.getLong("dailyStudyGoalMinutes") ?: 300L).toInt()
            )
            preferencesRepository.updateProfile(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(FirebaseErrorMapper.map(e), e))
        }
    }

    suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        val user = authRepository.currentUser
            ?: return Result.failure(IllegalStateException("Please sign in to update your profile."))

        val cleanName = profile.name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty."))
        }

        return try {
            val uid = user.uid
            val userDocRef = firestore.collection("users").document(uid)

            val profileData = mapOf(
                "uid" to uid,
                "ownerUid" to uid,
                "name" to cleanName,
                "email" to (profile.email.ifBlank { user.email ?: "" }),
                "university" to profile.university.trim(),
                "course" to profile.course.trim(),
                "semester" to profile.semester.trim(),
                "photoUrl" to profile.photoUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(profileData, SetOptions.merge()).await()

            val prefsData = mapOf(
                "ownerUid" to uid,
                "themeMode" to profile.themeMode.name,
                "notificationsEnabled" to profile.notificationsEnabled,
                "dailyStudyGoalMinutes" to profile.dailyStudyGoalMinutes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDocRef.collection("settings").document("preferences")
                .set(prefsData, SetOptions.merge())
                .await()

            if (user.displayName != cleanName) {
                val updateReq = UserProfileChangeRequest.Builder()
                    .setDisplayName(cleanName)
                    .build()
                user.updateProfile(updateReq).await()
            }

            preferencesRepository.updateProfile(profile.copy(uid = uid, name = cleanName))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(FirebaseErrorMapper.map(e), e))
        }
    }

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        val user = authRepository.currentUser
            ?: return Result.failure(IllegalStateException("Please sign in to upload a profile image."))

        return try {
            val uid = user.uid
            val imageRef = storage.reference.child("users/$uid/profile/avatar.jpg")
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            firestore.collection("users").document(uid)
                .set(
                    mapOf(
                        "photoUrl" to downloadUrl,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()

            val updateReq = UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(downloadUrl))
                .build()
            user.updateProfile(updateReq).await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(FirebaseErrorMapper.map(e), e))
        }
    }

    suspend fun saveFcmToken(token: String) {
        val uid = authRepository.currentUid ?: return
        if (token.isBlank()) return
        runCatching {
            val tokenHash = Base64.encodeToString(
                token.toByteArray(Charsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            ).take(64)
            firestore.collection("users").document(uid)
                .collection("fcmTokens").document(tokenHash)
                .set(
                    mapOf(
                        "ownerUid" to uid,
                        "token" to token,
                        "platform" to "android",
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()
        }
    }

    suspend fun registerCurrentFcmToken() {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            saveFcmToken(token)
        }
    }

    suspend fun removeCurrentFcmToken() {
        val uid = authRepository.currentUid ?: return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNotBlank()) {
                val tokenHash = Base64.encodeToString(
                    token.toByteArray(Charsets.UTF_8),
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                ).take(64)
                firestore.collection("users").document(uid)
                    .collection("fcmTokens").document(tokenHash)
                    .delete()
                    .await()
            }
        }
    }

    suspend fun clearLocalSessionData() {
        preferencesRepository.clearUserProfile()
    }
}
