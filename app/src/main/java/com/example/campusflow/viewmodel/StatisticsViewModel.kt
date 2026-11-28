package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val totalAssignments: Int = 0,
    val completedAssignments: Int = 0,
    val overdueAssignments: Int = 0,
    val assignmentCompletionPercentage: Float = 0f,
    val totalStudyMinutes: Int = 0,
    val totalStudyHours: Float = 0f,
    val weeklyStudyMinutes: Int = 0,
    val quizAverage: Float = 0f,
    val bestQuizScore: Float = 0f,
    val totalQuizzes: Int = 0,
    val overallAttendance: Float = 0f,
    val attendedClasses: Int = 0,
    val totalClasses: Int = 0,
    val weeklyProductivityScore: Int = 0
)

class StatisticsViewModel(
    statisticsRepository: StatisticsRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = statisticsRepository.statisticsFlow
        .map { stats ->
            StatisticsUiState(
                totalTasks = stats.totalTasks,
                completedTasks = stats.completedTasks,
                pendingTasks = stats.pendingTasks,
                totalAssignments = stats.totalAssignments,
                completedAssignments = stats.completedAssignments,
                overdueAssignments = stats.overdueAssignments,
                assignmentCompletionPercentage = stats.assignmentCompletionPercentage,
                totalStudyMinutes = stats.totalStudyMinutes,
                totalStudyHours = stats.totalStudyHours,
                weeklyStudyMinutes = stats.weeklyStudyMinutes,
                quizAverage = stats.averageQuizScore,
                bestQuizScore = stats.bestQuizScore,
                totalQuizzes = stats.totalQuizzesAttempted,
                overallAttendance = stats.attendancePercentage,
                attendedClasses = stats.attendedClasses,
                totalClasses = stats.totalClasses,
                weeklyProductivityScore = stats.weeklyProductivityScore
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())
}
