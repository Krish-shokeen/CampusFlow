package com.example.campusflow.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE // If Subject is deleted, assignments are deleted
        )
    ],
    indices = [Index("subjectId")] // Indexes improve query speed based on foreign keys
)
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val description: String,
    val dueDate: LocalDateTime?,
    val priority: Priority,
    val isCompleted: Boolean = false
)