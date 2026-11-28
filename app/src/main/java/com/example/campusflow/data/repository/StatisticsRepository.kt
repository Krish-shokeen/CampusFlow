package com.example.campusflow.data.repository

import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.AttendanceRecord
import com.example.campusflow.data.entities.QuizResult
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

data class AcademicStatistics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val totalAssignments: Int = 0,
    val completedAssignments: Int = 0,
    val overdueAssignments: Int = 0,
    val assignmentCompletionPercentage: Float = 0f,
    val attendedClasses: Int = 0,
    val missedClasses: Int = 0,
    val totalClasses: Int = 0,
    val attendancePercentage: Float = 0f,
    val totalQuizzesAttempted: Int = 0,
    val averageQuizScore: Float = 0f,
    val bestQuizScore: Float = 0f,
    val totalStudyMinutes: Int = 0,
    val totalStudyHours: Float = 0f,
    val weeklyStudyMinutes: Int = 0,
    val weeklyProductivityScore: Int = 0
)

class StatisticsRepository(
    private val taskRepository: TaskRepository,
    private val assignmentRepository: AssignmentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val quizRepository: QuizRepository,
    private val studyPlannerRepository: StudyPlannerRepository
) {

    val statisticsFlow: Flow<AcademicStatistics> = combine(
        taskRepository.getAllTasksStream(),
        assignmentRepository.getAllAssignmentsStream(),
        attendanceRepository.getAllRecordsStream(),
        quizRepository.getAllQuizResultsStream(),
        studyPlannerRepository.getAllSessionsStream()
    ) { tasks, assignments, attendance, quizzes, sessions ->
        computeStatistics(tasks, assignments, attendance, quizzes, sessions)
    }

    companion object {
        fun computeStatistics(
            tasks: List<Task>,
            assignments: List<Assignment>,
            attendance: List<AttendanceRecord>,
            quizzes: List<QuizResult>,
            sessions: List<StudySession>,
            today: LocalDate = LocalDate.now()
        ): AcademicStatistics {
            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
            val pendingTasks = totalTasks - completedTasks

            val totalAssignments = assignments.size
            val completedAssignments = assignments.count { it.isCompleted }
            val overdueAssignments = assignments.count {
                !it.isCompleted && DateUtils.isOverdue(it.dueDate)
            }
            val assignmentPct = if (totalAssignments > 0) {
                (completedAssignments.toFloat() / totalAssignments) * 100f
            } else 0f

            val totalClasses = attendance.size
            val attendedClasses = attendance.count { it.isPresent }
            val missedClasses = totalClasses - attendedClasses
            val attendancePct = if (totalClasses > 0) {
                (attendedClasses.toFloat() / totalClasses) * 100f
            } else 0f

            val totalQuizzes = quizzes.size
            val quizPercentages = quizzes.map { it.percentage }
            val averageQuizScore = if (quizPercentages.isNotEmpty()) {
                quizPercentages.average().toFloat()
            } else 0f
            val bestQuizScore = quizPercentages.maxOrNull() ?: 0f

            val totalStudyMinutes = sessions.sumOf { it.durationMinutes }
            val totalStudyHours = totalStudyMinutes / 60f
            val weekStart = today.minusDays(6)
            val weeklyStudyMinutes = sessions
                .filter { !it.date.isBefore(weekStart) && !it.date.isAfter(today) }
                .sumOf { it.durationMinutes }

            // Compute real productivity index (0..100) from actual records only
            val components = mutableListOf<Float>()
            if (totalTasks > 0) components.add((completedTasks.toFloat() / totalTasks) * 100f)
            if (totalAssignments > 0) components.add(assignmentPct)
            if (totalClasses > 0) components.add(attendancePct)
            if (totalQuizzes > 0) components.add(averageQuizScore)
            if (weeklyStudyMinutes > 0) {
                // Target 10 hours/week = 600 mins
                components.add(((weeklyStudyMinutes / 600f) * 100f).coerceAtMost(100f))
            }
            val productivityScore = if (components.isNotEmpty()) {
                components.average().toInt().coerceIn(0, 100)
            } else 0

            return AcademicStatistics(
                totalTasks = totalTasks,
                completedTasks = completedTasks,
                pendingTasks = pendingTasks,
                totalAssignments = totalAssignments,
                completedAssignments = completedAssignments,
                overdueAssignments = overdueAssignments,
                assignmentCompletionPercentage = assignmentPct,
                attendedClasses = attendedClasses,
                missedClasses = missedClasses,
                totalClasses = totalClasses,
                attendancePercentage = attendancePct,
                totalQuizzesAttempted = totalQuizzes,
                averageQuizScore = averageQuizScore,
                bestQuizScore = bestQuizScore,
                totalStudyMinutes = totalStudyMinutes,
                totalStudyHours = totalStudyHours,
                weeklyStudyMinutes = weeklyStudyMinutes,
                weeklyProductivityScore = productivityScore
            )
        }
    }
}
