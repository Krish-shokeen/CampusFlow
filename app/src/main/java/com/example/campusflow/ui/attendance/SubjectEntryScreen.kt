package com.example.campusflow.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectEntryScreen(
    subjectId: Int?,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    
    var subjectName by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf(75f) }
    var isNameError by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load existing subject
    LaunchedEffect(subjectId) {
        if (subjectId != null) {
            viewModel.getSubject(subjectId)?.let { existing ->
                subjectName = existing.subjectName
                threshold = existing.attendanceThreshold
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subjectId == null) "Add Subject" else "Edit Subject") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (subjectId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Subject")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it; isNameError = false },
                label = { Text("Subject Name *") },
                isError = isNameError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (isNameError) {
                Text("Subject name cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Minimum Attendance Threshold: ${String.format(Locale.US, "%.0f", threshold)}%")
            Slider(
                value = threshold,
                onValueChange = { threshold = it },
                valueRange = 50f..100f,
                steps = 49 // 50 to 100 with steps of 1
            )
            Text(
                "Warning: If a subject is deleted, all its assignments and attendance records will be removed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (subjectName.isBlank()) {
                        isNameError = true
                        return@Button
                    }
                    val subject = Subject(id = subjectId ?: 0, subjectName = subjectName, attendanceThreshold = threshold)
                    coroutineScope.launch {
                        if (subjectId == null) viewModel.addSubject(subject) else viewModel.updateSubject(subject)
                        navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Subject")
            }
        }

        if (showDeleteDialog && subjectId != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Subject") },
                text = { Text("Are you sure you want to delete '$subjectName'? This will permanently delete all attendance records and assignments for this subject.") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            viewModel.deleteSubject(Subject(id = subjectId, subjectName = "", attendanceThreshold = 0f))
                            showDeleteDialog = false
                            navigateBack()
                        }
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
            )
        }
    }
}