package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.AttendanceRecord
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.repository.AttendanceRepository
import com.example.campusflow.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AttendanceStatus { SAFE, WARNING, LOW }

data class SubjectAttendanceStats(
    val subject: Subject,
    val present: Int,
    val absent: Int,
    val total: Int,
    val percentage: Float,
    val status: AttendanceStatus,
    val classesToReachThreshold: Int
)

data class OverallAttendanceStats(
    val present: Int,
    val absent: Int,
    val total: Int,
    val percentage: Float
)

data class AttendanceRecordDetails(
    val record: AttendanceRecord,
    val subject: Subject?
)

data class AttendanceUiState(
    val subjectsStats: List<SubjectAttendanceStats> = emptyList(),
    val overallStats: OverallAttendanceStats = OverallAttendanceStats(0, 0, 0, 0f),
    val allRecords: List<AttendanceRecordDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<AttendanceUiState> = combine(
        attendanceRepository.getAllRecordsStream(),
        subjectRepository.getAllSubjectsStream()
    ) { records, subjects ->
        var totalPresent = 0
        var totalAbsent = 0

        val subjectsStats = subjects.map { subject ->
            val subjectRecords = records.filter { it.subjectId == subject.id }
            val present = subjectRecords.count { it.isPresent }
            val absent = subjectRecords.count { !it.isPresent }
            val total = present + absent
            val percentage = AttendanceRepository.calculatePercentage(present, total)

            totalPresent += present
            totalAbsent += absent

            val status = when {
                total == 0 -> AttendanceStatus.SAFE
                percentage >= subject.attendanceThreshold + 5f -> AttendanceStatus.SAFE
                percentage >= subject.attendanceThreshold -> AttendanceStatus.WARNING
                else -> AttendanceStatus.LOW
            }

            val classesToReachThreshold = AttendanceRepository.calculateClassesToReachThreshold(
                present = present,
                total = total,
                thresholdPercent = subject.attendanceThreshold
            )

            SubjectAttendanceStats(
                subject = subject,
                present = present,
                absent = absent,
                total = total,
                percentage = percentage,
                status = status,
                classesToReachThreshold = classesToReachThreshold
            )
        }.sortedBy { it.percentage }

        val overallTotal = totalPresent + totalAbsent
        val overallPercentage = AttendanceRepository.calculatePercentage(totalPresent, overallTotal)
        val overallStats = OverallAttendanceStats(
            present = totalPresent,
            absent = totalAbsent,
            total = overallTotal,
            percentage = overallPercentage
        )

        val recordDetails = records.map { record ->
            AttendanceRecordDetails(
                record = record,
                subject = subjects.find { it.id == record.subjectId }
            )
        }

        AttendanceUiState(
            subjectsStats = subjectsStats,
            overallStats = overallStats,
            allRecords = recordDetails,
            subjects = subjects,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttendanceUiState(isLoading = true)
    )

    val historySubjectFilter = MutableStateFlow<Int?>(null)

    val filteredHistory: StateFlow<List<AttendanceRecordDetails>> = combine(
        uiState, historySubjectFilter
    ) { state, subjectId ->
        if (subjectId == null) state.allRecords
        else state.allRecords.filter { it.subject?.id == subjectId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(subject: Subject) = viewModelScope.launch {
        runCatching { subjectRepository.insertSubject(subject) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to add subject." }
    }

    fun updateSubject(subject: Subject) = viewModelScope.launch {
        runCatching { subjectRepository.updateSubject(subject) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to update subject." }
    }

    fun deleteSubject(subject: Subject) = viewModelScope.launch {
        runCatching { subjectRepository.deleteSubject(subject) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to delete subject." }
    }

    suspend fun getSubject(id: Int): Subject? = subjectRepository.getSubjectStream(id)

    fun markAttendance(subjectId: Int, date: LocalDate, isPresent: Boolean) {
        viewModelScope.launch {
            runCatching {
                attendanceRepository.markAttendance(subjectId, date, isPresent)
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to mark attendance."
            }
        }
    }

    fun updateRecord(record: AttendanceRecord) = viewModelScope.launch {
        runCatching { attendanceRepository.updateRecord(record) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to update attendance." }
    }

    fun deleteRecord(record: AttendanceRecord) = viewModelScope.launch {
        runCatching { attendanceRepository.deleteRecord(record) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to delete attendance record." }
    }

    suspend fun getRecord(id: Int): AttendanceRecord? = attendanceRepository.getRecordStream(id)

    fun clearError() {
        _errorMessage.value = null
    }
}
