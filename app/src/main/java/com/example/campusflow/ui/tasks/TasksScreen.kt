package com.example.campusflow.ui.tasks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.ui.components.StatCard
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToTaskEntry: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val priorityFilter by viewModel.priorityFilter.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // Stats Row with Animated Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val total = uiState.tasks.size
                val pending = uiState.tasks.count { it.status != TaskStatus.COMPLETED }
                val completed = uiState.tasks.count { it.status == TaskStatus.COMPLETED }

                AnimatedStatCard(title = "Total", value = total.toString(), modifier = Modifier.weight(1f))
                AnimatedStatCard(title = "Pending", value = pending.toString(), modifier = Modifier.weight(1f))
                AnimatedStatCard(title = "Done", value = completed.toString(), modifier = Modifier.weight(1f))
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

            // Filter Chips Row
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
                items(TaskStatus.entries.toTypedArray()) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.statusFilter.value = status },
                        label = { Text(status.name.replace("_", " ")) }
                    )
                }
                
                item { HorizontalDivider(modifier = Modifier.width(1.dp).height(32.dp)) }
                
                items(Priority.entries.toTypedArray()) { priority ->
                    FilterChip(
                        selected = priorityFilter == priority,
                        onClick = {
                            viewModel.priorityFilter.value = if (priorityFilter == priority) null else priority
                        },
                        label = { Text(priority.name) }
                    )
                }
            }

            // Task List or Empty State with Animated Visibility
            AnimatedVisibility(
                visible = uiState.tasks.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState(
                    title = "No Tasks Found",
                    message = "You don't have any tasks matching this criteria. Add a new task to get started!",
                    icon = Icons.Default.Assignment
                )
            }

            AnimatedVisibility(
                visible = uiState.tasks.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.tasks,
                        key = { it.id }
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

@Composable
private fun AnimatedStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "statAnim",
        modifier = modifier
    ) { targetVal ->
        StatCard(title = title, value = targetVal)
    }
}
