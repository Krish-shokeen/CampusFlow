package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
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
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {
    private val idToDocId = ConcurrentHashMap<Int, String>()

    fun getAllTasksStream(): Flow<List<Task>> = authRepository.authStateFlow.flatMapLatest { user ->
        if (user == null) {
            idToDocId.clear()
            flowOf(emptyList())
        } else {
            callbackFlow {
                val listener = firestore.collection("users").document(user.uid)
                    .collection("tasks")
                    .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        val tasks = snapshot?.documents?.mapNotNull { doc ->
                            mapDocumentToTask(doc)?.also { task ->
                                idToDocId[task.id] = doc.id
                            }
                        } ?: emptyList()
                        trySend(tasks)
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    fun getTasksByStatusStream(status: String): Flow<List<Task>> =
        getAllTasksStream().map { tasks ->
            tasks.filter { it.status.name == status }
        }

    suspend fun getTaskStream(id: Int): Task? {
        val uid = authRepository.currentUid ?: return null
        val docId = idToDocId[id] ?: return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("tasks").document(docId).get().await()
            if (doc.exists()) mapDocumentToTask(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun insertTask(task: Task) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to create a task.")
        val cleanTitle = task.title.trim()
        if (cleanTitle.isEmpty()) {
            throw IllegalArgumentException("Task title cannot be empty.")
        }

        try {
            val collection = firestore.collection("users").document(uid).collection("tasks")
            val docRef = collection.document()
            val nowMillis = System.currentTimeMillis()
            val createdMillis = FirestoreMapper.localDateTimeToMillis(task.createdAt)
            val data = hashMapOf<String, Any?>(
                "ownerUid" to uid,
                "title" to cleanTitle,
                "description" to task.description.trim(),
                "priority" to task.priority.name,
                "status" to task.status.name,
                "dueDateMillis" to task.dueDate?.let { FirestoreMapper.localDateTimeToMillis(it) },
                "createdAtMillis" to createdMillis,
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

    suspend fun updateTask(task: Task) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to update tasks.")
        val docId = idToDocId[task.id]
            ?: throw IllegalStateException("Task reference could not be resolved.")
        val cleanTitle = task.title.trim()
        if (cleanTitle.isEmpty()) {
            throw IllegalArgumentException("Task title cannot be empty.")
        }

        try {
            val data = hashMapOf<String, Any?>(
                "ownerUid" to uid,
                "title" to cleanTitle,
                "description" to task.description.trim(),
                "priority" to task.priority.name,
                "status" to task.status.name,
                "dueDateMillis" to task.dueDate?.let { FirestoreMapper.localDateTimeToMillis(it) },
                "updatedAtMillis" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("tasks").document(docId)
                .update(data)
                .await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun deleteTask(task: Task) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to delete tasks.")
        val docId = idToDocId[task.id] ?: return

        try {
            firestore.collection("users").document(uid)
                .collection("tasks").document(docId)
                .delete()
                .await()
            idToDocId.remove(task.id)
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun mapDocumentToTask(doc: DocumentSnapshot): Task? {
        val title = doc.getString("title") ?: return null
        val description = doc.getString("description") ?: ""
        val priority = doc.getString("priority")?.let {
            runCatching { Priority.valueOf(it) }.getOrNull()
        } ?: Priority.MEDIUM
        val status = doc.getString("status")?.let {
            runCatching { TaskStatus.valueOf(it) }.getOrNull()
        } ?: TaskStatus.PENDING
        val dueDateMillis = doc.getLong("dueDateMillis")
        val createdAtMillis = doc.getLong("createdAtMillis")

        return Task(
            id = FirestoreMapper.stableIntId(doc.id),
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDateMillis?.let { FirestoreMapper.millisToLocalDateTime(it) },
            status = status,
            createdAt = createdAtMillis?.let { FirestoreMapper.millisToLocalDateTime(it) }
                ?: LocalDateTime.now()
        )
    }
}
