package com.example.campusflow.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.StudyPlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    onNavigateToSessionEntry: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudyPlannerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var sessionToDelete by remember { mutableStateOf<StudySession?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToSessionEntry(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = "Study Planner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.sessions.isEmpty()) {
                EmptyState(
                    title = "No Sessions Planned",
                    message = "Plan your studies to stay on top of your subjects.",
                    icon = Icons.Default.Timer
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.sessions, key = { it.session.id }) { details ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToSessionEntry(details.session.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = details.subject?.subjectName ?: "Unknown Subject",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateUtils.formatLocalDate(details.session.date),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${DateUtils.formatDateTime(details.session.startTime)} | ${details.session.durationMinutes} mins",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { sessionToDelete = details.session }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (sessionToDelete != null) {
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                title = { Text("Delete Session") },
                text = { Text("Are you sure you want to delete this study session?") },
                confirmButton = {
                    TextButton(onClick = { sessionToDelete?.let { viewModel.deleteSession(it) }; sessionToDelete = null }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = { TextButton(onClick = { sessionToDelete = null }) { Text("Cancel") } }
            )
        }
    }
}