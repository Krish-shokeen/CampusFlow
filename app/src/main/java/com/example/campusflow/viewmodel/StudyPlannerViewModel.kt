package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.repository.StudyPlannerRepository
import com.example.campusflow.data.repository.SubjectRepository
<<<<<<< Updated upstream
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudySessionDetails(
    val session: StudySession,
    val subject: Subject?
)

data class StudyPlannerUiState(
    val sessions: List<StudySessionDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = true
)

class StudyPlannerViewModel(
    private val studyPlannerRepository: StudyPlannerRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {
    val uiState: StateFlow<StudyPlannerUiState> = combine(
        studyPlannerRepository.getAllSessionsStream(),
        subjectRepository.getAllSubjectsStream()
    ) { sessions, subjects ->
        StudyPlannerUiState(
            sessions = sessions
                .map { session -> StudySessionDetails(session, subjects.find { it.id == session.subjectId }) }
                .sortedWith(compareBy({ it.session.date }, { it.session.startTime })),
            subjects = subjects,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudyPlannerUiState())

    fun saveSession(session: StudySession) {
        viewModelScope.launch {
            if (session.id == 0) studyPlannerRepository.insertSession(session)
            else studyPlannerRepository.updateSession(session)
        }
    }

    fun deleteSession(session: StudySession) {
        viewModelScope.launch { studyPlannerRepository.deleteSession(session) }
    }
}
=======
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionDetails(val session: StudySession, val subject: Subject?)

data class PlannerUiState(
    val sessions: List<SessionDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false
)

class StudyPlannerViewModel(
    private val plannerRepository: StudyPlannerRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {
    
    val uiState: StateFlow<PlannerUiState> = combine(
        plannerRepository.getAllSessionsStream(),
        subjectRepository.getAllSubjectsStream()
    ) { sessions, subjects ->
        val details = sessions.map { session ->
            SessionDetails(session, subjects.find { it.id == session.subjectId })
        }.sortedWith(compareBy({ it.session.date }, { it.session.startTime }))
        PlannerUiState(sessions = details, subjects = subjects, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlannerUiState(isLoading = true))

    fun addSession(session: StudySession) = viewModelScope.launch { plannerRepository.insertSession(session) }
    fun updateSession(session: StudySession) = viewModelScope.launch { plannerRepository.updateSession(session) }
    fun deleteSession(session: StudySession) = viewModelScope.launch { plannerRepository.deleteSession(session) }
    suspend fun getSession(id: Int): StudySession? = plannerRepository.getSessionStream(id)
}
>>>>>>> Stashed changes
