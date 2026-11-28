package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.remote.FirebaseErrorMapper
import com.example.campusflow.data.remote.FirestoreMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class StudyPlannerRepository(
    private val authRepository: AuthRepository,
    private val subjectRepository: SubjectRepository,
    private val firestore: FirebaseFirestore
) {
    private val idToDocId = ConcurrentHashMap<Int, String>()

    fun getAllSessionsStream(): Flow<List<StudySession>> =
        authRepository.authStateFlow.flatMapLatest { user ->
            if (user == null) {
                idToDocId.clear()
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val listener = firestore.collection("users").document(user.uid)
                        .collection("studySessions")
                        .orderBy("startTimeMillis", Query.Direction.ASCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val sessions = snapshot?.documents?.mapNotNull { doc ->
                                mapDocumentToSession(doc)?.also { session ->
                                    idToDocId[session.id] = doc.id
                                }
                            } ?: emptyList()
                            trySend(sessions)
                        }
                    awaitClose { listener.remove() }
                }
            }
        }

    suspend fun getSessionStream(id: Int): StudySession? {
        val uid = authRepository.currentUid ?: return null
        val docId = idToDocId[id] ?: return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("studySessions").document(docId).get().await()
            if (doc.exists()) mapDocumentToSession(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun insertSession(session: StudySession) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to schedule a study session.")
        validateSessionTime(session.startTime, session.durationMinutes)

        try {
            val subjectRemoteId = subjectRepository.resolveRemoteId(session.subjectId) ?: ""
            val docRef = firestore.collection("users").document(uid)
                .collection("studySessions").document()
            val startMillis = FirestoreMapper.localDateTimeToMillis(session.startTime)
            val endMillis = FirestoreMapper.localDateTimeToMillis(
                session.startTime.plusMinutes(session.durationMinutes.toLong())
            )
            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "subjectId" to session.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "title" to session.title.ifBlank { "Study Session" },
                "focusMethod" to session.focusMethod.ifBlank { "POMODORO" },
                "date" to FirestoreMapper.localDateToIso(session.date),
                "startTimeMillis" to startMillis,
                "endTimeMillis" to endMillis,
                "durationMinutes" to session.durationMinutes,
                "isCompleted" to session.isCompleted,
                "createdAtMillis" to System.currentTimeMillis(),
                "updatedAtMillis" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            idToDocId[FirestoreMapper.stableIntId(docRef.id)] = docRef.id
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun updateSession(session: StudySession) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to update study sessions.")
        val docId = idToDocId[session.id]
            ?: throw IllegalStateException("Study session reference could not be resolved.")
        validateSessionTime(session.startTime, session.durationMinutes)

        try {
            val subjectRemoteId = subjectRepository.resolveRemoteId(session.subjectId) ?: ""
            val startMillis = FirestoreMapper.localDateTimeToMillis(session.startTime)
            val endMillis = FirestoreMapper.localDateTimeToMillis(
                session.startTime.plusMinutes(session.durationMinutes.toLong())
            )
            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "subjectId" to session.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "title" to session.title.ifBlank { "Study Session" },
                "focusMethod" to session.focusMethod.ifBlank { "POMODORO" },
                "date" to FirestoreMapper.localDateToIso(session.date),
                "startTimeMillis" to startMillis,
                "endTimeMillis" to endMillis,
                "durationMinutes" to session.durationMinutes,
                "isCompleted" to session.isCompleted,
                "updatedAtMillis" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("studySessions").document(docId)
                .update(data)
                .await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun markSessionCompleted(session: StudySession, isCompleted: Boolean) {
        updateSession(session.copy(isCompleted = isCompleted))
    }

    suspend fun deleteSession(session: StudySession) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to delete study sessions.")
        val docId = idToDocId[session.id] ?: return

        try {
            firestore.collection("users").document(uid)
                .collection("studySessions").document(docId)
                .delete()
                .await()
            idToDocId.remove(session.id)
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun mapDocumentToSession(doc: DocumentSnapshot): StudySession? {
        val duration = (doc.getLong("durationMinutes") ?: 0L).toInt()
        if (duration <= 0) return null
        val startMillis = doc.getLong("startTimeMillis") ?: System.currentTimeMillis()
        val subjectRemoteId = doc.getString("subjectRemoteId")
        val resolvedSubjectId = if (!subjectRemoteId.isNullOrBlank()) {
            FirestoreMapper.stableIntId(subjectRemoteId)
        } else {
            (doc.getLong("subjectId") ?: 0L).toInt()
        }

        return StudySession(
            id = FirestoreMapper.stableIntId(doc.id),
            subjectId = resolvedSubjectId,
            date = FirestoreMapper.isoToLocalDate(doc.getString("date")),
            startTime = FirestoreMapper.millisToLocalDateTime(startMillis),
            durationMinutes = duration,
            title = doc.getString("title") ?: "Study Session",
            focusMethod = doc.getString("focusMethod") ?: "POMODORO",
            isCompleted = doc.getBoolean("isCompleted") ?: false
        )
    }

    companion object {
        fun validateSessionTime(startTime: LocalDateTime, durationMinutes: Int) {
            if (durationMinutes <= 0 || durationMinutes > 1440) {
                throw IllegalArgumentException("Duration must be between 1 and 1440 minutes.")
            }
            val endTime = startTime.plusMinutes(durationMinutes.toLong())
            if (!endTime.isAfter(startTime)) {
                throw IllegalArgumentException("End time must be after start time.")
            }
        }

        fun calculateDurationMinutes(startTime: LocalDateTime, endTime: LocalDateTime): Int {
            if (!endTime.isAfter(startTime)) {
                throw IllegalArgumentException("End time must be after start time.")
            }
            return Duration.between(startTime, endTime).toMinutes().toInt()
        }
    }
}
