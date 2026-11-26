package com.example.campusflow.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AssignmentViewModel
import com.example.campusflow.viewmodel.AttendanceViewModel
import com.example.campusflow.viewmodel.ProfileSettingsViewModel
import com.example.campusflow.viewmodel.QuizViewModel
import com.example.campusflow.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    assignmentViewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory),
    attendanceViewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory),
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory),
    profileViewModel: ProfileSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasksState by taskViewModel.uiState.collectAsState()
    val assignmentsState by assignmentViewModel.uiState.collectAsState()
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val quizState by quizViewModel.homeUiState.collectAsState()
    val profile by profileViewModel.uiState.collectAsState()

    val pendingTasks = tasksState.tasks.count { it.status != TaskStatus.COMPLETED }
    val pendingAssignments = assignmentsState.assignments.filter { !it.assignment.isCompleted }
    val upcomingAssignment = pendingAssignments
        .filter { it.assignment.dueDate != null && !DateUtils.isOverdue(it.assignment.dueDate) }
        .minByOrNull { it.assignment.dueDate!! }
    val attendancePercent = attendanceState.overallStats.percentage
    val animatedAttendance by animateFloatAsState(
        targetValue = attendancePercent / 100f,
        label = "attendance progress"
    )
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (profile.name.isBlank()) "Welcome back" else "Welcome back, ${profile.name}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(date, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Tasks",
                    value = pendingTasks.toString(),
                    detail = "pending",
                    icon = { Icon(Icons.Default.TaskAlt, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Assignments",
                    value = pendingAssignments.size.toString(),
                    detail = "pending",
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = MaterialTheme.colorScheme.tertiary) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("  Attendance", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        Text(
                            String.format(Locale.getDefault(), "%.0f%%", attendancePercent),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    LinearProgressIndicator(progress = { animatedAttendance }, modifier = Modifier.fillMaxWidth())
                    Text(
                        "${attendanceState.overallStats.present} of ${attendanceState.overallStats.total} classes attended",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("  Next assignment", style = MaterialTheme.typography.titleMedium)
                    }
                    if (upcomingAssignment == null) {
                        Text("No upcoming deadlines. You're all caught up.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(upcomingAssignment.assignment.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${upcomingAssignment.subject?.subjectName ?: "Assignment"} · " +
                                DateUtils.formatDate(upcomingAssignment.assignment.dueDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${pendingAssignments.count { DateUtils.isOverdue(it.assignment.dueDate) }} overdue",
                        color = if (pendingAssignments.any { DateUtils.isOverdue(it.assignment.dueDate) })
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        item {
            Text("Quick actions", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onNavigateToTasks, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.TaskAlt, null, modifier = Modifier.size(18.dp))
                    Text(" Tasks")
                }
                OutlinedButton(onClick = onNavigateToAssignments, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, null, modifier = Modifier.size(18.dp))
                    Text(" Assignments")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onNavigateToAttendance, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Text(" Attendance")
                }
                OutlinedButton(onClick = onNavigateToPlanner, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(18.dp))
                    Text(" Study")
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quiz progress", style = MaterialTheme.typography.titleMedium)
                    Text("${quizState.totalAttempted} quizzes · ${quizState.averageScore.toInt()}% average")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    detail: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text("$title $detail", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
