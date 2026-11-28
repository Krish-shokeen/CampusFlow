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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class TaskSortOption { DUE_DATE, PRIORITY, RECENTLY_CREATED }

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow<TaskStatus?>(null)
    val priorityFilter = MutableStateFlow<Priority?>(null)
    val sortOption = MutableStateFlow(TaskSortOption.DUE_DATE)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllTasksStream(),
        searchQuery,
        statusFilter,
        priorityFilter,
        sortOption
    ) { tasks, query, status, priority, sort ->
        var filteredTasks = tasks

        if (query.isNotBlank()) {
            filteredTasks = filteredTasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            }
        }
        if (status != null) {
            filteredTasks = filteredTasks.filter { it.status == status }
        }
        if (priority != null) {
            filteredTasks = filteredTasks.filter { it.priority == priority }
        }

        filteredTasks = when (sort) {
            TaskSortOption.DUE_DATE -> filteredTasks.sortedBy { it.dueDate ?: LocalDateTime.MAX }
            TaskSortOption.PRIORITY -> filteredTasks.sortedByDescending { it.priority.ordinal }
            TaskSortOption.RECENTLY_CREATED -> filteredTasks.sortedByDescending { it.createdAt }
        }

        TasksUiState(tasks = filteredTasks, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState(isLoading = true)
    )

    fun addTask(task: Task) {
        viewModelScope.launch {
            runCatching { taskRepository.insertTask(task) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to create task." }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            runCatching { taskRepository.updateTask(task) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to update task." }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            runCatching { taskRepository.deleteTask(task) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to delete task." }
        }
    }

    fun markTaskCompleted(task: Task, isCompleted: Boolean) {
        val newStatus = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING
        updateTask(task.copy(status = newStatus))
    }

    suspend fun getTask(id: Int): Task? = taskRepository.getTaskStream(id)

    fun clearError() {
        _errorMessage.value = null
    }
}
