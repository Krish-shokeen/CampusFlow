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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class AssignmentFilter { ALL, PENDING, COMPLETED, OVERDUE }
enum class AssignmentSort { DEADLINE, PRIORITY, NEWEST }

// Wrapper data class to hold the assignment along with its resolved subject
data class AssignmentDetails(
    val assignment: Assignment,
    val subject: Subject?
)

data class AssignmentsUiState(
    val assignments: List<AssignmentDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(), // Provide subjects to UI for the Add/Edit form
    val isLoading: Boolean = false
)

class AssignmentViewModel(
    private val assignmentRepository: AssignmentRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow(AssignmentFilter.ALL)
    val sortOption = MutableStateFlow(AssignmentSort.DEADLINE)

    // Combine flows to emit a fresh state whenever the DB or a filter changes
    val uiState: StateFlow<AssignmentsUiState> = combine(
        assignmentRepository.getAllAssignmentsStream(),
        subjectRepository.getAllSubjectsStream(),
        searchQuery,
        statusFilter,
        sortOption
    ) { assignments, subjects, query, status, sort ->
        
        // 1. Map Assignments to AssignmentDetails by finding the matching subject
        var detailsList = assignments.map { assignment ->
            AssignmentDetails(
                assignment = assignment,
                subject = subjects.find { it.id == assignment.subjectId }
            )
        }

        // 2. Apply Search
        if (query.isNotBlank()) {
            detailsList = detailsList.filter { 
                it.assignment.title.contains(query, ignoreCase = true) || 
                (it.subject?.subjectName?.contains(query, ignoreCase = true) == true) 
            }
        }

        // 3. Apply Filter
        detailsList = when (status) {
            AssignmentFilter.ALL -> detailsList
            AssignmentFilter.PENDING -> detailsList.filter { !it.assignment.isCompleted }
            AssignmentFilter.COMPLETED -> detailsList.filter { it.assignment.isCompleted }
            AssignmentFilter.OVERDUE -> detailsList.filter { 
                !it.assignment.isCompleted && DateUtils.isOverdue(it.assignment.dueDate) 
            }
        }

        // 4. Apply Sort
        detailsList = when (sort) {
            AssignmentSort.DEADLINE -> {
                detailsList.sortedBy { it.assignment.dueDate ?: LocalDateTime.MAX }
            }
            AssignmentSort.PRIORITY -> {
                detailsList.sortedByDescending { it.assignment.priority.ordinal }
            }
            AssignmentSort.NEWEST -> {
                // Since our model doesn't have createdAt, ID descending is a perfect proxy for newest
                detailsList.sortedByDescending { it.assignment.id }
            }
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

    // Database Actions
    fun addAssignment(assignment: Assignment) {
        viewModelScope.launch {
            assignmentRepository.insertAssignment(assignment)
        }
    }

    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            assignmentRepository.updateAssignment(assignment)
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            assignmentRepository.deleteAssignment(assignment)
        }
    }

    fun markAssignmentCompleted(assignment: Assignment, isCompleted: Boolean) {
        updateAssignment(assignment.copy(isCompleted = isCompleted))
    }

    suspend fun getAssignment(id: Int): Assignment? {
        return assignmentRepository.getAssignmentStream(id)
    }

    // Helper to allow users to quickly add a subject if none exist during assignment creation
    fun addSubject(name: String) {
        viewModelScope.launch {
            subjectRepository.insertSubject(Subject(subjectName = name, attendanceThreshold = 75f))
        }
    }
}