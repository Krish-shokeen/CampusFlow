package com.example.campusflow.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AssignmentViewModel
import com.example.campusflow.viewmodel.AttendanceViewModel
import com.example.campusflow.viewmodel.QuizViewModel
import com.example.campusflow.viewmodel.StudyPlannerViewModel
import com.example.campusflow.viewmodel.TaskViewModel
import java.time.LocalDate

@Composable
fun StatisticsScreen(
    taskViewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    assignmentViewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory),
    attendanceViewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory),
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory),
    plannerViewModel: StudyPlannerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasks by taskViewModel.uiState.collectAsState()
    val assignments by assignmentViewModel.uiState.collectAsState()
    val attendance by attendanceViewModel.uiState.collectAsState()
    val quizzes by quizViewModel.homeUiState.collectAsState()
    val planner by plannerViewModel.uiState.collectAsState()

    val completedTasks = tasks.tasks.count { it.status == TaskStatus.COMPLETED }
    val completionRate = if (tasks.tasks.isEmpty()) 0f else completedTasks * 100f / tasks.tasks.size
    val weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
    val studyMinutes = planner.sessions.sumOf {
        if (it.session.date >= weekStart && it.session.date <= LocalDate.now()) it.session.durationMinutes else 0
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Tasks completed", "$completedTasks / ${tasks.tasks.size}", Modifier.weight(1f))
            MetricCard("Assignments due", assignments.assignments.count { !it.assignment.isCompleted }.toString(), Modifier.weight(1f))
        }
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Task completion", style = MaterialTheme.typography.titleMedium)
                Text("${completionRate.toInt()}%", style = MaterialTheme.typography.headlineSmall)
                LinearProgressIndicator(
                    progress = { completionRate / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Attendance", "${attendance.overallStats.percentage.toInt()}%", Modifier.weight(1f))
            MetricCard("Quizzes taken", quizzes.totalAttempted.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Quiz average", "${quizzes.averageScore.toInt()}%", Modifier.weight(1f))
            MetricCard("Study time this week", "${studyMinutes / 60}h ${studyMinutes % 60}m", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
