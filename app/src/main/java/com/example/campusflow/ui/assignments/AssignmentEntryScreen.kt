package com.example.campusflow.ui.assignments

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AssignmentViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEntryScreen(
    assignmentId: Int?,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    // Form States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var isCompleted by remember { mutableStateOf(false) }
    
    // Error States
    var isTitleError by remember { mutableStateOf(false) }
    var isSubjectError by remember { mutableStateOf(false) }

    // Dropdown and Dialog States
    var priorityExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate?.let { DateUtils.convertLocalDateTimeToMillis(it) }
    )

    // Load existing assignment if editing
    LaunchedEffect(assignmentId) {
        if (assignmentId != null) {
            viewModel.getAssignment(assignmentId)?.let { existing ->
                title = existing.title
                description = existing.description
                priority = existing.priority
                dueDate = existing.dueDate
                selectedSubjectId = existing.subjectId
                isCompleted = existing.isCompleted
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (assignmentId == null) "New Assignment" else "Edit Assignment") },
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
                            onClick = { 
                                selectedSubjectId = subject.id; subjectExpanded = false; isSubjectError = false 
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("+ Add New Subject", color = MaterialTheme.colorScheme.primary) },
                        onClick = { subjectExpanded = false; showAddSubjectDialog = true }
                    )
                }
            }
            if (isSubjectError) {
                Text("Please select a subject", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; isTitleError = false },
                label = { Text("Assignment Title *") },
                isError = isTitleError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (isTitleError) {
                Text("Title cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            // Due Date
            OutlinedTextField(
                value = DateUtils.formatDate(dueDate),
                onValueChange = {},
                label = { Text("Submission Deadline") },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Select Date") } },
                modifier = Modifier.fillMaxWidth()
            )

            // Priority
            ExposedDropdownMenuBox(expanded = priorityExpanded, onExpandedChange = { priorityExpanded = it }) {
                OutlinedTextField(
                    value = priority.name, onValueChange = {}, readOnly = true, label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                    Priority.entries.forEach { prio ->
                        DropdownMenuItem(text = { Text(prio.name) }, onClick = { priority = prio; priorityExpanded = false })
                    }
                }
            }

            // Completion Toggle (Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mark as completed", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = isCompleted, onCheckedChange = { isCompleted = it })
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    var hasError = false
                    if (title.isBlank()) { isTitleError = true; hasError = true }
                    if (selectedSubjectId == null) { isSubjectError = true; hasError = true }
                    if (hasError) return@Button
                    
                    val assignment = Assignment(
                        id = assignmentId ?: 0,
                        subjectId = selectedSubjectId!!,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        isCompleted = isCompleted
                    )
                    coroutineScope.launch {
                        if (assignmentId == null) viewModel.addAssignment(assignment) else viewModel.updateAssignment(assignment)
                        navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Assignment") }
        }

        // Add Subject Dialog (In-flow helper)
        if (showAddSubjectDialog) {
            AlertDialog(
                onDismissRequest = { showAddSubjectDialog = false; newSubjectName = "" },
                title = { Text("Add Subject") },
                text = {
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("Subject Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newSubjectName.isNotBlank()) {
                            viewModel.addSubject(newSubjectName)
                            showAddSubjectDialog = false
                            newSubjectName = ""
                        }
                    }) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubjectDialog = false; newSubjectName = "" }) { Text("Cancel") }
                }
            )
        }

        // Date Picker
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false; dueDate = datePickerState.selectedDateMillis?.let { DateUtils.convertMillisToLocalDateTime(it) } }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}