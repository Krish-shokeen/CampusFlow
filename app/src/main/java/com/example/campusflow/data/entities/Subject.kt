package com.example.campusflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val attendanceThreshold: Float // e.g., 75.0 for 75% required attendance
)