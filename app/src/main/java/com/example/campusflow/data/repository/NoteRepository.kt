package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.Note
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
class NoteRepository(
    private val authRepository: AuthRepository,
    private val subjectRepository: SubjectRepository,
    private val firestore: FirebaseFirestore
) {
    private val idToDocId = ConcurrentHashMap<Int, String>()

    fun getAllNotesStream(): Flow<List<Note>> = authRepository.authStateFlow.flatMapLatest { user ->
        if (user == null) {
            idToDocId.clear()
            flowOf(emptyList())
        } else {
            callbackFlow {
                val listener = firestore.collection("users").document(user.uid)
                    .collection("notes")
                    .orderBy("updatedAtMillis", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        val notes = snapshot?.documents?.mapNotNull { doc ->
                            mapDocumentToNote(doc)?.also { note ->
                                idToDocId[note.id] = doc.id
                            }
                        } ?: emptyList()
                        trySend(notes)
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    fun searchNotesStream(query: String): Flow<List<Note>> =
        getAllNotesStream().map { notes ->
            if (query.isBlank()) notes
            else notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }
        }

    suspend fun getNoteStream(id: Int): Note? {
        val uid = authRepository.currentUid ?: return null
        val docId = idToDocId[id] ?: return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("notes").document(docId).get().await()
            if (doc.exists()) mapDocumentToNote(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun insertNote(note: Note) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to create notes.")

        try {
            val docRef = firestore.collection("users").document(uid).collection("notes").document()
            val createdMillis = FirestoreMapper.localDateTimeToMillis(note.createdAt)
            val updatedMillis = System.currentTimeMillis()
            val subjectRemoteId = note.subjectId?.let { subjectRepository.resolveRemoteId(it) }

            val data = hashMapOf<String, Any?>(
                "ownerUid" to uid,
                "subjectId" to note.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "title" to note.title.trim(),
                "content" to note.content,
                "category" to note.category.ifBlank { "General" },
                "isPinned" to note.isPinned,
                "createdAtMillis" to createdMillis,
                "updatedAtMillis" to updatedMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            idToDocId[FirestoreMapper.stableIntId(docRef.id)] = docRef.id
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun updateNote(note: Note) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to update notes.")
        val docId = idToDocId[note.id]
            ?: throw IllegalStateException("Note reference could not be resolved.")

        try {
            val subjectRemoteId = note.subjectId?.let { subjectRepository.resolveRemoteId(it) }
            val data = hashMapOf<String, Any?>(
                "ownerUid" to uid,
                "subjectId" to note.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "title" to note.title.trim(),
                "content" to note.content,
                "category" to note.category.ifBlank { "General" },
                "isPinned" to note.isPinned,
                "updatedAtMillis" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("notes").document(docId)
                .update(data)
                .await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun togglePin(note: Note) {
        updateNote(note.copy(isPinned = !note.isPinned, updatedAt = LocalDateTime.now()))
    }

    suspend fun deleteNote(note: Note) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to delete notes.")
        val docId = idToDocId[note.id] ?: return

        try {
            firestore.collection("users").document(uid)
                .collection("notes").document(docId)
                .delete()
                .await()
            idToDocId.remove(note.id)
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun mapDocumentToNote(doc: DocumentSnapshot): Note? {
        val title = doc.getString("title") ?: ""
        val content = doc.getString("content") ?: ""
        val subjectRemoteId = doc.getString("subjectRemoteId")
        val resolvedSubjectId = when {
            !subjectRemoteId.isNullOrBlank() -> FirestoreMapper.stableIntId(subjectRemoteId)
            doc.contains("subjectId") && doc.getLong("subjectId") != null -> doc.getLong("subjectId")!!.toInt()
            else -> null
        }
        val createdMillis = doc.getLong("createdAtMillis")
        val updatedMillis = doc.getLong("updatedAtMillis")

        return Note(
            id = FirestoreMapper.stableIntId(doc.id),
            subjectId = resolvedSubjectId,
            title = title,
            content = content,
            createdAt = createdMillis?.let { FirestoreMapper.millisToLocalDateTime(it) }
                ?: LocalDateTime.now(),
            updatedAt = updatedMillis?.let { FirestoreMapper.millisToLocalDateTime(it) }
                ?: LocalDateTime.now(),
            category = doc.getString("category") ?: "General",
            isPinned = doc.getBoolean("isPinned") ?: false
        )
    }
}

typealias NotesRepository = NoteRepository
