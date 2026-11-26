package com.example.campusflow.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.ui.components.StatCard
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.TaskSortOption
import com.example.campusflow.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToTaskEntry: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Collecting StateFlows from ViewModel to drive the UI
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val priorityFilter by viewModel.priorityFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    // Dialog state for deletion
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToTaskEntry(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header: Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // map/count higher order functions
                val total = uiState.tasks.size
                val pending = uiState.tasks.count { it.status != TaskStatus.COMPLETED }
                val completed = uiState.tasks.count { it.status == TaskStatus.COMPLETED }

                StatCard(title = "Total", value = total.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Pending", value = pending.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Done", value = completed.toString(), modifier = Modifier.weight(1f))
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Filter Chips Row (Horizontal Scroll)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.statusFilter.value = null },
                        label = { Text("All") }
                    )
                }
                items(TaskStatus.values()) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.statusFilter.value = status },
                        label = { Text(status.name.replace("_", " ")) }
                    )
                }
                
                item { Divider(modifier = Modifier.width(1.dp).height(32.dp)) }
                
                items(Priority.values()) { priority ->
                    FilterChip(
                        selected = priorityFilter == priority,
                        onClick = {
                            viewModel.priorityFilter.value = if (priorityFilter == priority) null else priority
                        },
                        label = { Text(priority.name) }
                    )
                }
            }

            // Task List or Empty State
            if (uiState.tasks.isEmpty()) {
                EmptyState(
                    title = "No Tasks Found",
                    message = "You don't have any tasks matching this criteria. Add a new task to get started!",
                    icon = Icons.Default.Assignment
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.tasks,
                        key = { it.id } // Optimizes Recomposition
                    ) { task ->
                        TaskCard(
                            task = task,
                            onTaskClick = { onNavigateToTaskEntry(task.id) },
                            onCheckedChange = { isChecked -> viewModel.markTaskCompleted(task, isChecked) },
                            onDeleteClick = { taskToDelete = task }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Delete Task") },
                text = { Text("Are you sure you want to delete '${taskToDelete?.title}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it) }
                        taskToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}