package com.example.campusflow.ui.dashboard

<<<<<<< Updated upstream
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
=======
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
import com.example.campusflow.viewmodel.ProfileSettingsViewModel
=======
import com.example.campusflow.viewmodel.ProfileViewModel
>>>>>>> Stashed changes
import com.example.campusflow.viewmodel.QuizViewModel
import com.example.campusflow.viewmodel.StudyPlannerViewModel
import com.example.campusflow.viewmodel.TaskViewModel
import java.time.LocalDate
<<<<<<< Updated upstream
import java.time.format.DateTimeFormatter
=======
import java.time.LocalTime
>>>>>>> Stashed changes
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable

@Composable
fun DashboardScreen(
<<<<<<< Updated upstream
    onNavigateToTasks: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToPlanner: () -> Unit,
=======
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToQuizzes: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
>>>>>>> Stashed changes
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    assignmentViewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory),
    attendanceViewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory),
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory),
<<<<<<< Updated upstream
    profileViewModel: ProfileSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
=======
    plannerViewModel: StudyPlannerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
>>>>>>> Stashed changes
) {
    val tasksState by taskViewModel.uiState.collectAsState()
    val assignmentsState by assignmentViewModel.uiState.collectAsState()
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val quizState by quizViewModel.homeUiState.collectAsState()
<<<<<<< Updated upstream
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
=======
    val plannerState by plannerViewModel.uiState.collectAsState()
    val profile by profileViewModel.userProfile.collectAsState()

    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "$greeting, ${profile.name}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${profile.course} | ${profile.semester}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Task Summary Card
        val pendingTasks = tasksState.tasks.count { it.status != TaskStatus.COMPLETED }
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
=======
        
        // Quiz Summary Card
        Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToQuizzes() }) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
>>>>>>> Stashed changes
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quiz progress", style = MaterialTheme.typography.titleMedium)
                    Text("${quizState.totalAttempted} quizzes · ${quizState.averageScore.toInt()}% average")
                }
            }
        }
        
        // Study Planner Summary Card
        val todaySessions = plannerState.sessions.filter { it.session.date == LocalDate.now() }
        Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToPlanner() }) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF16A34A))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Study Planner", style = MaterialTheme.typography.titleLarge)
                    Text("${todaySessions.size} sessions planned today", style = MaterialTheme.typography.bodyMedium)
                    if (todaySessions.isNotEmpty()) {
                        val nextSession = todaySessions.first()
                        Text(
                            "Next up: ${nextSession.subject?.subjectName ?: "General Study"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
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
