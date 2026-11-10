package com.example.campusflow.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL // Subject can be deleted without deleting the Note
        )
    ],
    indices = [Index("subjectId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int?, // Optional linking to a subject
    val title: String,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)