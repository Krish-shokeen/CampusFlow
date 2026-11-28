package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.Subject
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
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {
    private val idToDocId = ConcurrentHashMap<Int, String>()

    fun resolveRemoteId(localId: Int): String? = idToDocId[localId]

    fun getAllSubjectsStream(): Flow<List<Subject>> = authRepository.authStateFlow.flatMapLatest { user ->
        if (user == null) {
            idToDocId.clear()
            flowOf(emptyList())
        } else {
            callbackFlow {
                val listener = firestore.collection("users").document(user.uid)
                    .collection("subjects")
                    .orderBy("subjectName", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        val subjects = snapshot?.documents?.mapNotNull { doc ->
                            mapDocumentToSubject(doc)?.also { subject ->
                                idToDocId[subject.id] = doc.id
                            }
                        } ?: emptyList()
                        trySend(subjects)
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    suspend fun getSubjectStream(id: Int): Subject? {
        val uid = authRepository.currentUid ?: return null
        val docId = idToDocId[id] ?: return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("subjects").document(docId).get().await()
            if (doc.exists()) mapDocumentToSubject(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun insertSubject(subject: Subject): Long {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to add a subject.")
        val cleanName = subject.subjectName.trim()
        if (cleanName.isEmpty()) {
            throw IllegalArgumentException("Subject name cannot be empty.")
        }

        return try {
            val docRef = firestore.collection("users").document(uid).collection("subjects").document()
            val localId = FirestoreMapper.stableIntId(docRef.id)
            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "subjectName" to cleanName,
                "attendanceThreshold" to subject.attendanceThreshold.coerceIn(0f, 100f),
                "createdAtMillis" to System.currentTimeMillis(),
                "updatedAtMillis" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            idToDocId[localId] = docRef.id
            localId.toLong()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun updateSubject(subject: Subject) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to update subjects.")
        val docId = idToDocId[subject.id]
            ?: throw IllegalStateException("Subject reference could not be resolved.")
        val cleanName = subject.subjectName.trim()
        if (cleanName.isEmpty()) {
            throw IllegalArgumentException("Subject name cannot be empty.")
        }

        try {
            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "subjectName" to cleanName,
                "attendanceThreshold" to subject.attendanceThreshold.coerceIn(0f, 100f),
                "updatedAtMillis" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("subjects").document(docId)
                .update(data)
                .await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun deleteSubject(subject: Subject) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to delete subjects.")
        val docId = idToDocId[subject.id] ?: return

        try {
            val userRef = firestore.collection("users").document(uid)
            val batch = firestore.batch()

            // Cascade delete assignments, attendance, and study sessions for this subject
            val assignmentsSnap = userRef.collection("assignments")
                .whereEqualTo("subjectRemoteId", docId).get().await()
            assignmentsSnap.documents.forEach { batch.delete(it.reference) }

            val attendanceSnap = userRef.collection("attendance")
                .whereEqualTo("subjectRemoteId", docId).get().await()
            attendanceSnap.documents.forEach { batch.delete(it.reference) }

            val sessionsSnap = userRef.collection("studySessions")
                .whereEqualTo("subjectRemoteId", docId).get().await()
            sessionsSnap.documents.forEach { batch.delete(it.reference) }

            // Detach notes linked to this subject (SET_NULL semantics)
            val notesSnap = userRef.collection("notes")
                .whereEqualTo("subjectRemoteId", docId).get().await()
            notesSnap.documents.forEach {
                batch.update(it.reference, mapOf("subjectId" to null, "subjectRemoteId" to null))
            }

            batch.delete(userRef.collection("subjects").document(docId))
            batch.commit().await()
            idToDocId.remove(subject.id)
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun mapDocumentToSubject(doc: DocumentSnapshot): Subject? {
        val name = doc.getString("subjectName") ?: return null
        val threshold = doc.getDouble("attendanceThreshold")?.toFloat() ?: 75.0f
        return Subject(
            id = FirestoreMapper.stableIntId(doc.id),
            subjectName = name,
            attendanceThreshold = threshold
        )
    }
}
