package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.repository.StudyPlannerRepository
import com.example.campusflow.data.repository.SubjectRepository
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
