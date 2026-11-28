package com.example.campusflow.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.ui.components.StatCard
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AttendanceViewModel
import java.util.Locale

@Composable
fun AttendanceScreen(
    onNavigateToSubjectEntry: (Int?) -> Unit,
    onNavigateToMarkAttendance: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column {
                SmallFloatingActionButton(
                    onClick = { onNavigateToSubjectEntry(null) },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subject")
                }
                ExtendedFloatingActionButton(
                    onClick = onNavigateToMarkAttendance,
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Mark Attendance") },
                    text = { Text("Mark Attendance") }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Header Action Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track your academic attendance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToHistory) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History")
                }
            }

            // Overall Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Overall",
                    value = String.format(Locale.US, "%.1f%%", uiState.overallStats.percentage),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Attended",
                    value = "${uiState.overallStats.present}/${uiState.overallStats.total}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Subject List
            if (uiState.subjectsStats.isEmpty()) {
                EmptyState(
                    title = "No Subjects Found",
                    message = "Add a subject to start tracking your attendance.",
                    icon = Icons.Default.Add
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.subjectsStats, key = { it.subject.id }) { stats ->
                        SubjectAttendanceCard(
                            stats = stats,
                            onEditSubject = { onNavigateToSubjectEntry(stats.subject.id) }
                        )
                    }
                }
            }
        }
    }
}