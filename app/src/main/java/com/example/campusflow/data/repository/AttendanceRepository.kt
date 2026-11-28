package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.AttendanceRecord
import com.example.campusflow.data.remote.FirebaseErrorMapper
import com.example.campusflow.data.remote.FirestoreMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceRepository(
    private val authRepository: AuthRepository,
    private val subjectRepository: SubjectRepository,
    private val firestore: FirebaseFirestore
) {
    private val idToDocId = ConcurrentHashMap<Int, String>()

    fun getAllRecordsStream(): Flow<List<AttendanceRecord>> =
        authRepository.authStateFlow.flatMapLatest { user ->
            if (user == null) {
                idToDocId.clear()
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val listener = firestore.collection("users").document(user.uid)
                        .collection("attendance")
                        .orderBy("date", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val records = snapshot?.documents?.mapNotNull { doc ->
                                mapDocumentToRecord(doc)?.also { record ->
                                    idToDocId[record.id] = doc.id
                                }
                            } ?: emptyList()
                            trySend(records)
                        }
                    awaitClose { listener.remove() }
                }
            }
        }

    fun getRecordsBySubjectStream(subjectId: Int): Flow<List<AttendanceRecord>> =
        getAllRecordsStream().map { records ->
            records.filter { it.subjectId == subjectId }
        }

    suspend fun getRecordStream(id: Int): AttendanceRecord? {
        val uid = authRepository.currentUid ?: return null
        val docId = idToDocId[id] ?: return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("attendance").document(docId).get().await()
            if (doc.exists()) mapDocumentToRecord(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Inserts or overwrites an attendance record using a deterministic document ID
     * (`{subjectKey}_{YYYY-MM-DD}`) to prevent duplicate entries for the same subject and date.
     */
    suspend fun insertRecord(record: AttendanceRecord) {
        markAttendance(record.subjectId, record.date, record.isPresent)
    }

    suspend fun markAttendance(subjectId: Int, date: LocalDate, isPresent: Boolean) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to mark attendance.")

        try {
            val subjectRemoteId = subjectRepository.resolveRemoteId(subjectId) ?: subjectId.toString()
            val dateIso = FirestoreMapper.localDateToIso(date)
            val deterministicDocId = "${subjectRemoteId}_$dateIso"

            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "subjectId" to subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "date" to dateIso,
                "isPresent" to isPresent,
                "updatedAtMillis" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("attendance").document(deterministicDocId)
                .set(data, SetOptions.merge())
                .await()

            idToDocId[FirestoreMapper.stableIntId(deterministicDocId)] = deterministicDocId
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun updateRecord(record: AttendanceRecord) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to update attendance.")
        val subjectRemoteId = subjectRepository.resolveRemoteId(record.subjectId) ?: record.subjectId.toString()
        val dateIso = FirestoreMapper.localDateToIso(record.date)
        val docId = idToDocId[record.id] ?: "${subjectRemoteId}_$dateIso"

        try {
            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "subjectId" to record.subjectId,
                "subjectRemoteId" to subjectRemoteId,
                "date" to dateIso,
                "isPresent" to record.isPresent,
                "updatedAtMillis" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .collection("attendance").document(docId)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    suspend fun deleteRecord(record: AttendanceRecord) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to delete attendance records.")
        val docId = idToDocId[record.id] ?: return

        try {
            firestore.collection("users").document(uid)
                .collection("attendance").document(docId)
                .delete()
                .await()
            idToDocId.remove(record.id)
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun mapDocumentToRecord(doc: DocumentSnapshot): AttendanceRecord? {
        val dateIso = doc.getString("date") ?: return null
        val isPresent = doc.getBoolean("isPresent") ?: return null
        val subjectRemoteId = doc.getString("subjectRemoteId")
        val resolvedSubjectId = if (!subjectRemoteId.isNullOrBlank()) {
            FirestoreMapper.stableIntId(subjectRemoteId)
        } else {
            (doc.getLong("subjectId") ?: 0L).toInt()
        }

        return AttendanceRecord(
            id = FirestoreMapper.stableIntId(doc.id),
            subjectId = resolvedSubjectId,
            date = FirestoreMapper.isoToLocalDate(dateIso),
            isPresent = isPresent
        )
    }

    companion object {
        fun calculatePercentage(present: Int, total: Int): Float {
            return if (total <= 0) 0f else (present.toFloat() / total) * 100f
        }

        fun calculateClassesToReachThreshold(present: Int, total: Int, thresholdPercent: Float): Int {
            val currentPct = calculatePercentage(present, total)
            if (total == 0 && thresholdPercent > 0f) return 1
            if (currentPct >= thresholdPercent) return 0
            val ratio = thresholdPercent / 100f
            if (ratio >= 1.0f) return -1
            val needed = ceil((ratio * total - present) / (1f - ratio)).toInt()
            return needed.coerceAtLeast(0)
        }
    }
}
