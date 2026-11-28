package com.example.campusflow.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.R
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.ui.components.ProgressRing
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AssignmentViewModel
import com.example.campusflow.viewmodel.AttendanceViewModel
import com.example.campusflow.viewmodel.ProfileViewModel
import com.example.campusflow.viewmodel.QuizViewModel
import com.example.campusflow.viewmodel.StudyPlannerViewModel
import com.example.campusflow.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToQuizzes: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    assignmentViewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory),
    attendanceViewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory),
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory),
    plannerViewModel: StudyPlannerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasksState by taskViewModel.uiState.collectAsState()
    val assignmentsState by assignmentViewModel.uiState.collectAsState()
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val quizState by quizViewModel.homeUiState.collectAsState()
    val plannerState by plannerViewModel.uiState.collectAsState()
    val profile by profileViewModel.userProfile.collectAsState()

    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val pendingTasks = tasksState.tasks.count { it.status != TaskStatus.COMPLETED }
    val completedTasks = tasksState.tasks.count { it.status == TaskStatus.COMPLETED }
    val totalTasks = tasksState.tasks.size
    val pendingAssignments = assignmentsState.assignments.filter { !it.assignment.isCompleted }
    val upcomingAssignment = pendingAssignments
        .filter { it.assignment.dueDate != null && !DateUtils.isOverdue(it.assignment.dueDate) }
        .minByOrNull { it.assignment.dueDate!! }
    val attendancePercent = attendanceState.overallStats.percentage
    val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$formattedDate • ${profile.semester.ifBlank { "Semester V" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting, ${profile.name.ifBlank { "Student" }} 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.course.ifBlank { "CSE • B.Tech" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_secondary),
                        contentDescription = "CampusFlow Logo",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Hero Daily Sprint Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.Unspecified)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val goalPercent = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0.78f
                            ProgressRing(
                                progress = goalPercent,
                                size = 72.dp,
                                strokeWidth = 7.dp,
                                progressColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                backgroundColor = Color.White.copy(alpha = 0.2f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DAILY SPRINT TARGET",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (pendingTasks <= 2) "Almost Crushed It!" else "Keep Moving Forward!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$pendingTasks tasks remaining today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tasks Done: $completedTasks / $totalTasks",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Focus: 3.5 hrs",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    label = "Add Task",
                    icon = Icons.Default.AddTask,
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Assignment",
                    icon = Icons.Default.NoteAdd,
                    onClick = onNavigateToAssignments,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Attendance",
                    icon = Icons.Default.HowToReg,
                    onClick = onNavigateToAttendance,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Quiz Prep",
                    icon = Icons.Default.Psychology,
                    onClick = onNavigateToQuizzes,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4 Core Academic Performance Grid (2x2)
        item {
            Text("Academic Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PerformanceCard(
                        title = "Pending Tasks",
                        value = pendingTasks.toString(),
                        badgeText = "$pendingTasks Due",
                        badgeColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f).clickable { onNavigateToTasks() }
                    )
                    PerformanceCard(
                        title = "Assignments Pending",
                        value = pendingAssignments.size.toString(),
                        badgeText = "${pendingAssignments.size} Pending",
                        badgeColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f).clickable { onNavigateToAssignments() }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PerformanceCard(
                        title = "Attendance",
                        value = String.format(Locale.US, "%.1f%%", attendancePercent),
                        badgeText = if (attendancePercent >= 75) "Safe" else "Low!",
                        badgeColor = if (attendancePercent >= 75) Color(0xFF16A34A) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f).clickable { onNavigateToAttendance() }
                    )
                    PerformanceCard(
                        title = "Quiz Average",
                        value = "${quizState.averageScore.toInt()}%",
                        badgeText = "${quizState.totalAttempted} Quiz",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).clickable { onNavigateToQuizzes() }
                    )
                }
            }
        }

        // Next Assignment Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToAssignments() },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Next Assignment Deadline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    if (upcomingAssignment == null) {
                        Text("No upcoming deadlines. You're all caught up!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(upcomingAssignment.assignment.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${upcomingAssignment.subject?.subjectName ?: "Assignment"} · Due " +
                                DateUtils.formatDate(upcomingAssignment.assignment.dueDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Study Planner Summary
        item {
            val todaySessions = plannerState.sessions.filter { it.session.date == LocalDate.now() }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToPlanner() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Study Planner Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PerformanceCard(
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
