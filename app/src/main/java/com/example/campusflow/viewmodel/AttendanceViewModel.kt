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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.ceil

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
    val overallStats: OverallAttendanceStats = OverallAttendanceStats(0, 0, 0, 100f),
    val allRecords: List<AttendanceRecordDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    // View state combining database streams
    val uiState: StateFlow<AttendanceUiState> = combine(
        attendanceRepository.getAllRecordsStream(),
        subjectRepository.getAllSubjectsStream()
    ) { records, subjects ->
        
        var totalPresent = 0
        var totalAbsent = 0

        // Calculate stats for each subject
        val subjectsStats = subjects.map { subject ->
            val subjectRecords = records.filter { it.subjectId == subject.id }
            val present = subjectRecords.count { it.isPresent }
            val absent = subjectRecords.count { !it.isPresent }
            val total = present + absent
            val percentage = if (total == 0) 0f else (present.toFloat() / total) * 100f
            
            totalPresent += present
            totalAbsent += absent

            val status = when {
                percentage >= subject.attendanceThreshold + 5f -> AttendanceStatus.SAFE
                percentage >= subject.attendanceThreshold -> AttendanceStatus.WARNING
                else -> AttendanceStatus.LOW
            }

            // Calculate classes needed to reach threshold
            val thresholdRatio = subject.attendanceThreshold / 100f
            val classesToReachThreshold = if (percentage < subject.attendanceThreshold && thresholdRatio < 1.0f) {
                val needed = ceil((thresholdRatio * total - present) / (1 - thresholdRatio)).toInt()
                if (needed < 0) 0 else needed
            } else if (percentage < subject.attendanceThreshold && thresholdRatio == 1.0f) {
                -1 // Impossible to reach 100% if missed any class
            } else {
                0
            }

            SubjectAttendanceStats(
                subject = subject,
                present = present,
                absent = absent,
                total = total,
                percentage = percentage,
                status = status,
                classesToReachThreshold = classesToReachThreshold
            )
        }.sortedBy { it.percentage } // Sort by lowest attendance first

        val overallTotal = totalPresent + totalAbsent
        val overallPercentage = if (overallTotal == 0) 0f else (totalPresent.toFloat() / overallTotal) * 100f
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

    // Form states for Attendance History filtering
    val historySubjectFilter = MutableStateFlow<Int?>(null)

    val filteredHistory: StateFlow<List<AttendanceRecordDetails>> = combine(
        uiState, historySubjectFilter
    ) { state, subjectId ->
        if (subjectId == null) state.allRecords 
        else state.allRecords.filter { it.subject?.id == subjectId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Subject Operations
    fun addSubject(subject: Subject) = viewModelScope.launch { subjectRepository.insertSubject(subject) }
    fun updateSubject(subject: Subject) = viewModelScope.launch { subjectRepository.updateSubject(subject) }
    fun deleteSubject(subject: Subject) = viewModelScope.launch { subjectRepository.deleteSubject(subject) }
    suspend fun getSubject(id: Int): Subject? = subjectRepository.getSubjectStream(id)

    // Attendance Record Operations
    fun markAttendance(subjectId: Int, date: LocalDate, isPresent: Boolean) {
        viewModelScope.launch {
            // Check if record already exists for this date and subject
            // Note: Since Flow represents UI state, we can quickly search the cached list
            // or perform a direct DB query. Searching cached list is faster here.
            val existingRecord = uiState.value.allRecords.find { 
                it.record.subjectId == subjectId && it.record.date == date 
            }?.record

            if (existingRecord != null) {
                attendanceRepository.updateRecord(existingRecord.copy(isPresent = isPresent))
            } else {
                attendanceRepository.insertRecord(AttendanceRecord(subjectId = subjectId, date = date, isPresent = isPresent))
            }
        }
    }

    fun updateRecord(record: AttendanceRecord) = viewModelScope.launch { attendanceRepository.updateRecord(record) }
    fun deleteRecord(record: AttendanceRecord) = viewModelScope.launch { attendanceRepository.deleteRecord(record) }
    suspend fun getRecord(id: Int): AttendanceRecord? = attendanceRepository.getRecordStream(id)
}