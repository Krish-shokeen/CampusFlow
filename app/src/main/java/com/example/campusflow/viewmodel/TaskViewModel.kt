package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class TaskSortOption { DUE_DATE, PRIORITY, RECENTLY_CREATED }

// Represents the UI State for the Task List Screen
data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    // MutableStateFlows hold the current state of our filters
    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow<TaskStatus?>(null) // null means "All"
    val priorityFilter = MutableStateFlow<Priority?>(null)
    val sortOption = MutableStateFlow(TaskSortOption.DUE_DATE)

    // The combine operator allows us to reactively recalculate the displayed tasks 
    // whenever the database changes OR any filter changes.
    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllTasksStream(),
        searchQuery,
        statusFilter,
        priorityFilter,
        sortOption
    ) { tasks, query, status, priority, sort ->
        // Start with all tasks
        var filteredTasks = tasks
        
        // 1. Apply Search Filter
        if (query.isNotBlank()) {
            filteredTasks = filteredTasks.filter { it.title.contains(query, ignoreCase = true) }
        }
        // 2. Apply Status Filter
        if (status != null) {
            filteredTasks = filteredTasks.filter { it.status == status }
        }
        // 3. Apply Priority Filter
        if (priority != null) {
            filteredTasks = filteredTasks.filter { it.priority == priority }
        }
        
        // 4. Apply Sort Option
        filteredTasks = when (sort) {
            TaskSortOption.DUE_DATE -> {
                // Tasks with no due date go to the end
                filteredTasks.sortedBy { it.dueDate ?: LocalDateTime.MAX }
            }
            TaskSortOption.PRIORITY -> {
                // Priority enum: LOW=0, MEDIUM=1, HIGH=2. Descending sorts High first.
                filteredTasks.sortedByDescending { it.priority.ordinal }
            }
            TaskSortOption.RECENTLY_CREATED -> {
                filteredTasks.sortedByDescending { it.createdAt }
            }
        }
        
        TasksUiState(tasks = filteredTasks, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after UI unbinds
        initialValue = TasksUiState(isLoading = true)
    )

    // Database Actions (wrapped in coroutines)
    fun addTask(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun markTaskCompleted(task: Task, isCompleted: Boolean) {
        val newStatus = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING
        updateTask(task.copy(status = newStatus))
    }

    // Helper to get a single task for the Edit Screen
    suspend fun getTask(id: Int): Task? {
        return taskRepository.getTaskStream(id)
    }
}