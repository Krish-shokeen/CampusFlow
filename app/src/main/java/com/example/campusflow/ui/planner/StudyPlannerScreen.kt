package com.example.campusflow.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.StudyPlannerViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun StudyPlannerScreen(
    viewModel: StudyPlannerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var sessionToEdit by remember { mutableStateOf<StudySession?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<StudySession?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { sessionToEdit = null; showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Schedule study session")
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Study Planner", style = MaterialTheme.typography.headlineMedium)
            if (state.subjects.isEmpty()) {
                Text(
                    "Add a subject in Attendance before scheduling a study session.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (state.sessions.isEmpty()) {
                Text("Your scheduled sessions will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.sessions, key = { it.session.id }) { details ->
                        val session = details.session
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(details.subject?.subjectName ?: "Study session", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${session.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))} · " +
                                            "${session.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} · " +
                                            "${session.durationMinutes} min",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                IconButton(onClick = { sessionToEdit = session; showEditor = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit session")
                                }
                                IconButton(onClick = { sessionToDelete = session }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete session", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        StudySessionDialog(
            session = sessionToEdit,
            subjects = state.subjects,
            onDismiss = { showEditor = false },
            onSave = { session -> viewModel.saveSession(session); showEditor = false }
        )
    }
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete study session?") },
            text = { Text("This session will be removed from your planner.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSession(session); sessionToDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { sessionToDelete = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudySessionDialog(
    session: StudySession?,
    subjects: List<com.example.campusflow.data.entities.Subject>,
    onDismiss: () -> Unit,
    onSave: (StudySession) -> Unit
) {
    var subjectId by remember(session?.id, subjects) { mutableStateOf(session?.subjectId ?: subjects.firstOrNull()?.id) }
    var date by remember(session?.id) { mutableStateOf(session?.date ?: LocalDate.now()) }
    var time by remember(session?.id) {
        mutableStateOf(session?.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "16:00")
    }
    var duration by remember(session?.id) { mutableStateOf(session?.durationMinutes?.toString() ?: "60") }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = DateUtils.convertLocalDateToMillis(date))

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
        title = { Text(if (session == null) "Schedule study session" else "Edit study session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subjects.find { it.id == subjectId }?.subjectName.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subjectMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.subjectName) },
                                onClick = { subjectId = subject.id; subjectMenuExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = DateUtils.formatLocalDate(date),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Choose date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it; showValidationError = false },
                    label = { Text("Start time (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { value -> if (value.all(Char::isDigit)) duration = value; showValidationError = false },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showValidationError) {
                    Text(
                        "Choose a subject, valid time, and duration greater than zero.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedTime = try {
                    LocalTime.parse(time.trim())
                } catch (_: DateTimeParseException) {
                    null
                }
                val parsedDuration = duration.toIntOrNull()
                if (subjectId == null || parsedTime == null || parsedDuration == null || parsedDuration <= 0) {
                    showValidationError = true
                } else {
                    onSave(
                        StudySession(
                            id = session?.id ?: 0,
                            subjectId = subjectId!!,
                            date = date,
                            startTime = LocalDateTime.of(date, parsedTime),
                            durationMinutes = parsedDuration
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = DateUtils.convertMillisToLocalDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
