package com.example.campusflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campusflow.data.entities.QuizQuestion
import com.example.campusflow.data.entities.QuizResult
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuizQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestion>)

    @Query("SELECT * FROM quiz_questions WHERE category = :category")
    suspend fun getQuestionsByCategory(category: String): List<QuizQuestion>

    @Query("SELECT DISTINCT category FROM quiz_questions")
    fun getQuizCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResult)

    @Query("SELECT * FROM quiz_results ORDER BY completedAt DESC")
    fun getAllQuizResults(): Flow<List<QuizResult>>
}