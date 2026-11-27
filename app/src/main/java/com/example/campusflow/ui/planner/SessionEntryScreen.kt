package com.example.campusflow.ui.planner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.StudyPlannerViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
    
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var durationStr by remember { mutableStateOf("60") }
    
    var subjectExpanded by remember { mutableStateOf(false) }
    var isSubjectError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.convertLocalDateToMillis(date)
    )

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            viewModel.getSession(sessionId)?.let { session ->
                selectedSubjectId = session.subjectId
                date = session.date
                startTime = session.startTime
                durationStr = session.durationMinutes.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sessionId == null) "New Session" else "Edit Session") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Dropdown
            ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = it }) {
                val subjectName = uiState.subjects.find { it.id == selectedSubjectId }?.subjectName ?: "Select Subject *"
                OutlinedTextField(
                    value = subjectName, 
                    onValueChange = {}, 
                    readOnly = true, 
                    label = { Text("Subject") },
                    isError = isSubjectError,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                    uiState.subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.subjectName) },
                            onClick = { selectedSubjectId = subject.id; subjectExpanded = false; isSubjectError = false }
                        )
                    }
                }
            }
            if (isSubjectError) {
                Text("Please select a subject", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Date Picker
            OutlinedTextField(
                value = DateUtils.formatLocalDate(date),
                onValueChange = {},
                label = { Text("Session Date") },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Select Date") } },
                modifier = Modifier.fillMaxWidth()
            )

            // Duration
            OutlinedTextField(
                value = durationStr,
                onValueChange = { if (it.all { char -> char.isDigit() }) durationStr = it },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedSubjectId == null) { isSubjectError = true; return@Button }
                    val dur = durationStr.toIntOrNull() ?: 60
                    val session = StudySession(
                        id = sessionId ?: 0,
                        subjectId = selectedSubjectId!!,
                        date = date,
                        startTime = startTime,
                        durationMinutes = dur
                    )
                    coroutineScope.launch {
                        if (sessionId == null) viewModel.addSession(session) else viewModel.updateSession(session)
                        navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Session") }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { 
                    TextButton(onClick = { 
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { 
                            date = DateUtils.convertMillisToLocalDate(it)
                            // Keep the time but update the date part
                            startTime = LocalDateTime.of(date, startTime.toLocalTime())
                        }
                    }) { Text("OK") } 
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}