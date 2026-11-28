package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.Priority
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class AssignmentRepository(
    private val authRepository: AuthRepository,
    private val subjectRepository: SubjectRepository,
    private val firestore: FirebaseFirestore
) {
    private val idToDocId = ConcurrentHashMap<Int, String>()

    fun getAllAssignmentsStream(): Flow<List<Assignment>> =
        authRepository.authStateFlow.flatMapLatest { user ->
            if (user == null) {
                idToDocId.clear()
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val listener = firestore.collection("users").document(user.uid)
                        .collection("assignments")
                        .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val list = snapshot?.documents?.mapNotNull { doc ->
                                mapDocumentToAssignment(doc)?.also { assignment ->
                                    idToDocId[assignment.id] = doc.id
                                }
                            } ?: emptyList()
                            trySend(list)
                        }
                    awaitClose { listener.remove() }
                }
            }
        }

    fun getAssignmentsBySubjectStream(subjectId: Int): Flow<List<Assignment>> =
        getAllAssignmentsStream().map { list ->
            list.filter { it.subjectId == subjectId }
        }

    suspend fun getAssignmentStream(id: Int): Assignment? {
        val uid = authRepository.currentUid ?: return null
        val docId = idToDocId[id] ?: return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("assignments").document(docId).get().await()
            if (doc.exists()) mapDocumentToAssignment(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun insertAssignment(assignment: Assignment) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to create an assignment.")
        val cleanTitle = assignment.title.trim()
        if (cleanTitle.isEmpty()) {
            throw IllegalArgumentException("Assignment title cannot be empty.")
        }

        try {
            val subjectRemoteId = subjectRepository.resolveRemoteId(assignment.subjectId) ?: ""
            val docRef = firestore.collection("users").document(uid)
                .collection("assignments").document()
            val nowMillis = System.currentTimeMillis()
            val data = hashMapOf<String, Any?>(
                "ownerUid" to uid,
                "subjectId" to assignment.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "title" to cleanTitle,
                "description" to assignment.description.trim(),
                "dueDateMillis" to assignment.dueDate?.let { FirestoreMapper.localDateTimeToMillis(it) },
                "priority" to assignment.priority.name,
                "isCompleted" to assignment.isCompleted,
                "createdAtMillis" to nowMillis,
                "updatedAtMillis" to nowMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            idToDocId[FirestoreMapper.stableIntId(docRef.id)] = docRef.id
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun updateAssignment(assignment: Assignment) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to update assignments.")
        val docId = idToDocId[assignment.id]
            ?: throw IllegalStateException("Assignment reference could not be resolved.")
        val cleanTitle = assignment.title.trim()
        if (cleanTitle.isEmpty()) {
            throw IllegalArgumentException("Assignment title cannot be empty.")
        }

        try {
            val subjectRemoteId = subjectRepository.resolveRemoteId(assignment.subjectId) ?: ""
            val data = hashMapOf<String, Any?>(
                "ownerUid" to uid,
                "subjectId" to assignment.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "title" to cleanTitle,
                "description" to assignment.description.trim(),
                "dueDateMillis" to assignment.dueDate?.let { FirestoreMapper.localDateTimeToMillis(it) },
                "priority" to assignment.priority.name,
                "isCompleted" to assignment.isCompleted,
                "updatedAtMillis" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("assignments").document(docId)
                .update(data)
                .await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun deleteAssignment(assignment: Assignment) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to delete assignments.")
        val docId = idToDocId[assignment.id] ?: return

        try {
            firestore.collection("users").document(uid)
                .collection("assignments").document(docId)
                .delete()
                .await()
            idToDocId.remove(assignment.id)
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun mapDocumentToAssignment(doc: DocumentSnapshot): Assignment? {
        val title = doc.getString("title") ?: return null
        val subjectRemoteId = doc.getString("subjectRemoteId")
        val resolvedSubjectId = if (!subjectRemoteId.isNullOrBlank()) {
            FirestoreMapper.stableIntId(subjectRemoteId)
        } else {
            (doc.getLong("subjectId") ?: 0L).toInt()
        }
        val priority = doc.getString("priority")?.let {
            runCatching { Priority.valueOf(it) }.getOrNull()
        } ?: Priority.MEDIUM

        return Assignment(
            id = FirestoreMapper.stableIntId(doc.id),
            subjectId = resolvedSubjectId,
            title = title,
            description = doc.getString("description") ?: "",
            dueDate = doc.getLong("dueDateMillis")?.let { FirestoreMapper.millisToLocalDateTime(it) },
            priority = priority,
            isCompleted = doc.getBoolean("isCompleted") ?: false
        )
    }
}
