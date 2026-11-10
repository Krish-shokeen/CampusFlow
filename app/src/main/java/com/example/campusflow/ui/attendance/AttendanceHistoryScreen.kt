package com.example.campusflow.ui.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    onNavigateToEditRecord: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyFilter by viewModel.historySubjectFilter.collectAsState()
    val history by viewModel.filteredHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance History") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = historyFilter == null,
                        onClick = { viewModel.historySubjectFilter.value = null },
                        label = { Text("All Subjects") }
                    )
                }
                items(uiState.subjects) { subject ->
                    FilterChip(
                        selected = historyFilter == subject.id,
                        onClick = { viewModel.historySubjectFilter.value = subject.id },
                        label = { Text(subject.subjectName) }
                    )
                }
            }

            if (history.isEmpty()) {
                EmptyState(
                    title = "No History Found",
                    message = "No attendance records match the selected filter.",
                    icon = Icons.Default.History
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(history, key = { it.record.id }) { details ->
                        val record = details.record
                        val subjectName = details.subject?.subjectName ?: "Unknown Subject"
                        ListItem(
                            headlineContent = { Text(subjectName) },
                            supportingContent = { Text(DateUtils.formatLocalDate(record.date)) },
                            trailingContent = {
                                Surface(
                                    color = if (record.isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = if (record.isPresent) "Present" else "Absent",
                                        color = if (record.isPresent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onNavigateToEditRecord(record.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}