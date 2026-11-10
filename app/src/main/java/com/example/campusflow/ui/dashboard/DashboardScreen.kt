package com.example.campusflow.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AssignmentViewModel
import com.example.campusflow.viewmodel.AttendanceStatus
import com.example.campusflow.viewmodel.AttendanceViewModel
import com.example.campusflow.viewmodel.QuizViewModel
import com.example.campusflow.viewmodel.TaskViewModel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import java.util.Locale

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    assignmentViewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory),
    attendanceViewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory),
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasksState by taskViewModel.uiState.collectAsState()
    val assignmentsState by assignmentViewModel.uiState.collectAsState()
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val quizState by quizViewModel.homeUiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Task Summary Card
        val pendingTasks = tasksState.tasks.count { it.status != TaskStatus.COMPLETED }
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Tasks", style = MaterialTheme.typography.titleLarge)
                    Text("$pendingTasks pending tasks", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Assignment Summary Card
        val pendingAssignments = assignmentsState.assignments.count { !it.assignment.isCompleted }
        val overdueAssignments = assignmentsState.assignments.count { 
            !it.assignment.isCompleted && DateUtils.isOverdue(it.assignment.dueDate) 
        }
        val nextAssignment = assignmentsState.assignments
            .filter { !it.assignment.isCompleted && it.assignment.dueDate != null }
            .minByOrNull { it.assignment.dueDate!! }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Assignments", style = MaterialTheme.typography.titleLarge)
                    Text("$pendingAssignments pending ($overdueAssignments overdue)", style = MaterialTheme.typography.bodyMedium)
                    if (nextAssignment != null) {
                        Text(
                            "Next: ${nextAssignment.assignment.title} on ${DateUtils.formatDate(nextAssignment.assignment.dueDate)}", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Attendance Summary Card
        val overallPercent = attendanceState.overallStats.percentage
        val lowestSubject = attendanceState.subjectsStats.minByOrNull { it.percentage }
        val lowSubjectsCount = attendanceState.subjectsStats.count { it.status == AttendanceStatus.LOW }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Attendance", style = MaterialTheme.typography.titleLarge)
                    Text(String.format(Locale.US, "Overall: %.1f%%", overallPercent), style = MaterialTheme.typography.bodyMedium)
                    if (lowSubjectsCount > 0) {
                        Text(
                            "$lowSubjectsCount subject(s) below threshold.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (lowestSubject != null) {
                        Text(
                            "Lowest: ${lowestSubject.subject.subjectName} (${String.format(Locale.US, "%.1f%%", lowestSubject.percentage)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Quiz Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Quizzes", style = MaterialTheme.typography.titleLarge)
                    Text("${quizState.totalAttempted} quizzes taken", style = MaterialTheme.typography.bodyMedium)
                    if (quizState.totalAttempted > 0) {
                        Text(
                            "Best Score: ${quizState.bestScore} | Avg: ${String.format(Locale.US, "%.1f%%", quizState.averageScore)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}