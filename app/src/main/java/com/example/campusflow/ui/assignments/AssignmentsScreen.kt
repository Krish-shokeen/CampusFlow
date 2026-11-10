package com.example.campusflow.ui.assignments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.ui.components.StatCard
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AssignmentFilter
import com.example.campusflow.viewmodel.AssignmentSort
import com.example.campusflow.viewmodel.AssignmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    onNavigateToAssignmentEntry: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AssignmentViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var assignmentToDelete by remember { mutableStateOf<Assignment?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAssignmentEntry(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val total = uiState.assignments.size
                val pending = uiState.assignments.count { !it.assignment.isCompleted }
                val completed = uiState.assignments.count { it.assignment.isCompleted }
                val overdue = uiState.assignments.count { 
                    !it.assignment.isCompleted && DateUtils.isOverdue(it.assignment.dueDate) 
                }

                StatCard(title = "Total", value = total.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Pending", value = pending.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Overdue", value = overdue.toString(), modifier = Modifier.weight(1f))
            }

            // Search Bar and Sort Icon
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search assignments or subjects...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        AssignmentSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.sortOption.value = sort
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == sort) {
                                        Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AssignmentFilter.entries) { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { viewModel.statusFilter.value = filter },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Assignment List
            if (uiState.assignments.isEmpty()) {
                EmptyState(
                    title = "No Assignments Found",
                    message = "You're all caught up! Add a new assignment to keep track.",
                    icon = Icons.Default.Assignment
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = uiState.assignments, key = { it.assignment.id }) { details ->
                        AssignmentCard(
                            details = details,
                            onAssignmentClick = { onNavigateToAssignmentEntry(details.assignment.id) },
                            onCheckedChange = { isChecked -> viewModel.markAssignmentCompleted(details.assignment, isChecked) },
                            onDeleteClick = { assignmentToDelete = details.assignment }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (assignmentToDelete != null) {
            AlertDialog(
                onDismissRequest = { assignmentToDelete = null },
                title = { Text("Delete Assignment") },
                text = { Text("Are you sure you want to delete '${assignmentToDelete?.title}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        assignmentToDelete?.let { viewModel.deleteAssignment(it) }
                        assignmentToDelete = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { assignmentToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}