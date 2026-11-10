package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.TaskDao
import com.example.campusflow.data.entities.Task
import kotlinx.coroutines.flow.Flow

// A repository class abstracts access to multiple data sources (like DB and network).
// It provides a clean API for data access to the rest of the application.
class TaskRepository(private val taskDao: TaskDao) {
    fun getAllTasksStream(): Flow<List<Task>> = taskDao.getAllTasks()
    
    fun getTasksByStatusStream(status: String): Flow<List<Task>> = taskDao.getTasksByStatus(status)
    
    suspend fun getTaskStream(id: Int): Task? = taskDao.getTaskById(id)
    
    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
}