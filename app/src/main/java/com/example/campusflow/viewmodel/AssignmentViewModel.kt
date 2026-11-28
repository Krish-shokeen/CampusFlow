package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.repository.AssignmentRepository
import com.example.campusflow.data.repository.SubjectRepository
import com.example.campusflow.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class AssignmentFilter { ALL, PENDING, COMPLETED, OVERDUE }
enum class AssignmentSort { DEADLINE, PRIORITY, NEWEST }

data class AssignmentDetails(
    val assignment: Assignment,
    val subject: Subject?
)

data class AssignmentsUiState(
    val assignments: List<AssignmentDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false
)

class AssignmentViewModel(
    private val assignmentRepository: AssignmentRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow(AssignmentFilter.ALL)
    val sortOption = MutableStateFlow(AssignmentSort.DEADLINE)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<AssignmentsUiState> = combine(
        assignmentRepository.getAllAssignmentsStream(),
        subjectRepository.getAllSubjectsStream(),
        searchQuery,
        statusFilter,
        sortOption
    ) { assignments, subjects, query, status, sort ->
        var detailsList = assignments.map { assignment ->
            AssignmentDetails(
                assignment = assignment,
                subject = subjects.find { it.id == assignment.subjectId }
            )
        }

        if (query.isNotBlank()) {
            detailsList = detailsList.filter {
                it.assignment.title.contains(query, ignoreCase = true) ||
                    (it.subject?.subjectName?.contains(query, ignoreCase = true) == true)
            }
        }

        detailsList = when (status) {
            AssignmentFilter.ALL -> detailsList
            AssignmentFilter.PENDING -> detailsList.filter { !it.assignment.isCompleted }
            AssignmentFilter.COMPLETED -> detailsList.filter { it.assignment.isCompleted }
            AssignmentFilter.OVERDUE -> detailsList.filter {
                !it.assignment.isCompleted && DateUtils.isOverdue(it.assignment.dueDate)
            }
        }

        detailsList = when (sort) {
            AssignmentSort.DEADLINE -> detailsList.sortedBy { it.assignment.dueDate ?: LocalDateTime.MAX }
            AssignmentSort.PRIORITY -> detailsList.sortedByDescending { it.assignment.priority.ordinal }
            AssignmentSort.NEWEST -> detailsList.sortedByDescending { it.assignment.id }
        }

        AssignmentsUiState(
            assignments = detailsList,
            subjects = subjects,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssignmentsUiState(isLoading = true)
    )

    fun addAssignment(assignment: Assignment) {
        viewModelScope.launch {
            runCatching { assignmentRepository.insertAssignment(assignment) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to create assignment." }
        }
    }

    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            runCatching { assignmentRepository.updateAssignment(assignment) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to update assignment." }
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            runCatching { assignmentRepository.deleteAssignment(assignment) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to delete assignment." }
        }
    }

    fun markAssignmentCompleted(assignment: Assignment, isCompleted: Boolean) {
        updateAssignment(assignment.copy(isCompleted = isCompleted))
    }

    suspend fun getAssignment(id: Int): Assignment? = assignmentRepository.getAssignmentStream(id)

    fun addSubject(name: String) {
        viewModelScope.launch {
            runCatching {
                subjectRepository.insertSubject(Subject(subjectName = name, attendanceThreshold = 75f))
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to create subject."
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
