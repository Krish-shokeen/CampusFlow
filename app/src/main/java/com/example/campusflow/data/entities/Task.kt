package com.example.campusflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: Priority,
    val dueDate: LocalDateTime?, // Nullable type: nullable fields use ? in Kotlin
    val status: TaskStatus,
    val createdAt: LocalDateTime = LocalDateTime.now()
)