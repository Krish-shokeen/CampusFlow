package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.repository.StudyPlannerRepository
import com.example.campusflow.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SessionDetails(
    val session: StudySession,
    val subject: Subject?
)

data class PlannerUiState(
    val sessions: List<SessionDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val totalStudyMinutes: Int = 0,
    val todayStudyMinutes: Int = 0,
    val isLoading: Boolean = true
)

class StudyPlannerViewModel(
    private val plannerRepository: StudyPlannerRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val dateFilter = MutableStateFlow<LocalDate?>(null)
    val subjectFilter = MutableStateFlow<Int?>(null)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<PlannerUiState> = combine(
        plannerRepository.getAllSessionsStream(),
        subjectRepository.getAllSubjectsStream(),
        dateFilter,
        subjectFilter
    ) { sessions, subjects, selectedDate, selectedSubjectId ->
        val today = LocalDate.now()
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val todayMinutes = sessions.filter { it.date == today }.sumOf { it.durationMinutes }

        var filtered = sessions
        if (selectedDate != null) {
            filtered = filtered.filter { it.date == selectedDate }
        }
        if (selectedSubjectId != null) {
            filtered = filtered.filter { it.subjectId == selectedSubjectId }
        }

        val details = filtered.map { session ->
            SessionDetails(session, subjects.find { it.id == session.subjectId })
        }.sortedWith(compareBy({ it.session.date }, { it.session.startTime }))

        PlannerUiState(
            sessions = details,
            subjects = subjects,
            totalStudyMinutes = totalMinutes,
            todayStudyMinutes = todayMinutes,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlannerUiState(isLoading = true))

    fun addSession(session: StudySession) = viewModelScope.launch {
        runCatching { plannerRepository.insertSession(session) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to create study session." }
    }

    fun updateSession(session: StudySession) = viewModelScope.launch {
        runCatching { plannerRepository.updateSession(session) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to update study session." }
    }

    fun markSessionCompleted(session: StudySession, isCompleted: Boolean) = viewModelScope.launch {
        runCatching { plannerRepository.markSessionCompleted(session, isCompleted) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to update session status." }
    }

    fun saveSession(session: StudySession) = viewModelScope.launch {
        runCatching {
            if (session.id == 0) plannerRepository.insertSession(session)
            else plannerRepository.updateSession(session)
        }.onFailure {
            _errorMessage.value = it.message ?: "Failed to save study session."
        }
    }

    fun deleteSession(session: StudySession) = viewModelScope.launch {
        runCatching { plannerRepository.deleteSession(session) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to delete study session." }
    }

    suspend fun getSession(id: Int): StudySession? = plannerRepository.getSessionStream(id)

    fun clearError() {
        _errorMessage.value = null
    }
}
