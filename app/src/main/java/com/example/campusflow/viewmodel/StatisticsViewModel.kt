package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.data.repository.*
import kotlinx.coroutines.flow.*

data class StatisticsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalAssignments: Int = 0,
    val completedAssignments: Int = 0,
    val totalStudyMinutes: Int = 0,
    val quizAverage: Float = 0f,
    val totalQuizzes: Int = 0,
    val overallAttendance: Float = 0f
)

class StatisticsViewModel(
    taskRepo: TaskRepository,
    assignmentRepo: AssignmentRepository,
    plannerRepo: StudyPlannerRepository,
    quizRepo: QuizRepository,
    attendanceRepo: AttendanceRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        taskRepo.getAllTasksStream(),
        assignmentRepo.getAllAssignmentsStream(),
        plannerRepo.getAllSessionsStream(),
        quizRepo.getAllQuizResultsStream(),
        attendanceRepo.getAllRecordsStream()
    ) { tasks, assignments, sessions, quizzes, attendance ->
        
        val attTotal = attendance.size
        val attPresent = attendance.count { it.isPresent }
        
        StatisticsUiState(
            totalTasks = tasks.size,
            completedTasks = tasks.count { it.status == TaskStatus.COMPLETED },
            totalAssignments = assignments.size,
            completedAssignments = assignments.count { it.isCompleted },
            totalStudyMinutes = sessions.sumOf { it.durationMinutes },
            quizAverage = if (quizzes.isNotEmpty()) quizzes.map { it.score.toFloat() / it.totalQuestions }.average().toFloat() * 100f else 0f,
            totalQuizzes = quizzes.size,
            overallAttendance = if (attTotal > 0) (attPresent.toFloat() / attTotal) * 100f else 0f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())
}