package com.example.campusflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val completedAt: LocalDateTime = LocalDateTime.now(),
    val timeTakenSeconds: Int = 0
) {
    val correctCount: Int get() = score
    val incorrectCount: Int get() = (totalQuestions - score).coerceAtLeast(0)
    val percentage: Float get() = if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100f else 0f
}