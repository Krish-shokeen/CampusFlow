package com.example.campusflow.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AttendanceViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    recordId: Int?,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isPresent by remember { mutableStateOf(true) }

    var subjectExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSubjectError by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.convertLocalDateToMillis(selectedDate)
    )

    // Load existing record if editing
    LaunchedEffect(recordId) {
        if (recordId != null) {
            viewModel.getRecord(recordId)?.let { record ->
                selectedSubjectId = record.subjectId
                selectedDate = record.date
                isPresent = record.isPresent
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null) "Mark Attendance" else "Edit Attendance") },
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
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { if (recordId == null) subjectExpanded = it } // Disallow changing subject on edit
            ) {
                val subjectName = uiState.subjects.find { it.id == selectedSubjectId }?.subjectName ?: "Select Subject *"
                OutlinedTextField(
                    value = subjectName, 
                    onValueChange = {}, 
                    readOnly = true, 
                    label = { Text("Subject") },
                    isError = isSubjectError,
                    trailingIcon = { if (recordId == null) ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = recordId == null
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

            // Date Selection
            OutlinedTextField(
                value = DateUtils.formatLocalDate(selectedDate),
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = { 
                    IconButton(
                        onClick = { showDatePicker = true },
                        enabled = recordId == null // Disallow changing date on edit to avoid PK/Duplicate conflict complexity
                    ) { Icon(Icons.Default.DateRange, "Select Date") } 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = recordId == null
            )

            // Status Toggle (Present/Absent)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = isPresent,
                    onClick = { isPresent = true },
                    label = { Text("Present") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !isPresent,
                    onClick = { isPresent = false },
                    label = { Text("Absent") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
            
            Text(
                text = "Note: Saving will update any existing attendance record for the chosen subject on this date.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedSubjectId == null) {
                        isSubjectError = true
                        return@Button
                    }
                    if (recordId == null) {
                        // Mark or Update based on Date + Subject uniqueness
                        viewModel.markAttendance(selectedSubjectId!!, selectedDate, isPresent)
                    } else {
                        // Explicit edit of a known record
                        viewModel.uiState.value.allRecords.find { it.record.id == recordId }?.let {
                            viewModel.updateRecord(it.record.copy(isPresent = isPresent))
                        }
                    }
                    navigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Attendance") }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { 
                    TextButton(onClick = { 
                        showDatePicker = false
                        selectedDate = datePickerState.selectedDateMillis?.let { DateUtils.convertMillisToLocalDate(it) } ?: LocalDate.now()
                    }) { Text("OK") } 
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}