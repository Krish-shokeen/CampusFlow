package com.example.campusflow.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.StudyPlannerViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

enum class FocusMethod(val title: String, val description: String) {
    POMODORO("Pomodoro Sprint", "25m high-focus / 5m structured pause"),
    DEEP_WORK("Deep Work Block", "90m uninterrupted flow state"),
    RECALL("Review & Recall", "Spaced repetition & self-testing")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionEntryScreen(
    sessionId: Int?,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudyPlannerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var sessionTitle by remember { mutableStateOf("Operating Systems Practice") }
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var durationStr by remember { mutableStateOf("90") }
    var selectedFocusMethod by remember { mutableStateOf(FocusMethod.POMODORO) }

    var isSubjectError by remember { mutableStateOf(false) }
    var durationError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.convertLocalDateToMillis(date)
    )

    val suggestionChips = listOf("Exam Cram", "Lab Assignment Prep", "Code Sprint", "Lecture Revision", "Quiz Practice")

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            viewModel.getSession(sessionId)?.let { session ->
                selectedSubjectId = session.subjectId
                date = session.date
                startTime = session.startTime
                durationStr = session.durationMinutes.toString()
                sessionTitle = session.title
                selectedFocusMethod = runCatching {
                    FocusMethod.valueOf(session.focusMethod)
                }.getOrDefault(FocusMethod.POMODORO)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sessionId == null) "Schedule Study Session" else "Edit Session") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Assistant Header Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("AI Study Assistant Ready", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Set dedicated focus time and structured goals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Session Title Input
            OutlinedTextField(
                value = sessionTitle,
                onValueChange = { sessionTitle = it },
                label = { Text("Session Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Suggestion Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(suggestionChips) { idea ->
                    FilterChip(
                        selected = sessionTitle == idea,
                        onClick = { sessionTitle = idea },
                        label = { Text(idea, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Course Selector Pills
            Text("Select Course", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.subjects) { subject ->
                    val isSelected = selectedSubjectId == subject.id
                    Surface(
                        onClick = { selectedSubjectId = subject.id; isSubjectError = false },
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = subject.subjectName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isSubjectError) {
                Text("Please select a subject", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Date & Schedule Presets
            Text("Time & Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = DateUtils.formatLocalDate(date),
                onValueChange = {},
                label = { Text("Session Date") },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, "Select Date") } },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { date = LocalDate.now() },
                    modifier = Modifier.weight(1f)
                ) { Text("Today") }
                OutlinedButton(
                    onClick = { date = LocalDate.now().plusDays(1) },
                    modifier = Modifier.weight(1f)
                ) { Text("Tomorrow") }
                OutlinedButton(
                    onClick = { date = LocalDate.now().plusDays(2) },
                    modifier = Modifier.weight(1f)
                ) { Text("In 2 Days") }
            }

            // Duration
            OutlinedTextField(
                value = durationStr,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        durationStr = it
                        durationError = null
                    }
                },
                isError = durationError != null,
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (durationError != null) {
                Text(
                    text = durationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Duration Target Banner
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${durationStr.toIntOrNull() ?: 60} mins focus target", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Optimal Cognitive Load", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }

            // Focus Method Selector Cards
            Text("Focus Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FocusMethod.entries.forEach { method ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFocusMethod = method },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFocusMethod == method) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFocusMethod == method,
                            onClick = { selectedFocusMethod = method }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(method.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(method.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedSubjectId == null && uiState.subjects.isNotEmpty()) {
                        selectedSubjectId = uiState.subjects.first().id
                    }
                    if (selectedSubjectId == null) { isSubjectError = true; return@Button }
                    val dur = durationStr.toIntOrNull()
                    if (dur == null || dur <= 0 || dur > 1440) {
                        durationError = "Duration must be between 1 and 1440 minutes (end time must be after start time)."
                        return@Button
                    }
                    val session = StudySession(
                        id = sessionId ?: 0,
                        subjectId = selectedSubjectId!!,
                        date = date,
                        startTime = startTime,
                        durationMinutes = dur,
                        title = sessionTitle.ifBlank { "Study Session" },
                        focusMethod = selectedFocusMethod.name
                    )
                    coroutineScope.launch {
                        if (sessionId == null) viewModel.addSession(session) else viewModel.updateSession(session)
                        navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Study Session", modifier = Modifier.padding(6.dp)) }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            date = DateUtils.convertMillisToLocalDate(it)
                            startTime = LocalDateTime.of(date, startTime.toLocalTime())
                        }
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}
