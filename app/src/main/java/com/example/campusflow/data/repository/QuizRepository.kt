package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.QuizDao
import com.example.campusflow.data.entities.QuizQuestion
import com.example.campusflow.data.entities.QuizResult
import kotlinx.coroutines.flow.Flow

class QuizRepository(private val quizDao: QuizDao) {
    suspend fun getQuestionsByCategory(category: String): List<QuizQuestion> = 
        quizDao.getQuestionsByCategory(category)
        
    fun getQuizCategoriesStream(): Flow<List<String>> = quizDao.getQuizCategories()
    
    suspend fun insertQuestion(question: QuizQuestion) = quizDao.insertQuestion(question)
    
    suspend fun insertQuestions(questions: List<QuizQuestion>) = quizDao.insertQuestions(questions)
    
    suspend fun insertQuizResult(result: QuizResult) = quizDao.insertQuizResult(result)
    
    fun getAllQuizResultsStream(): Flow<List<QuizResult>> = quizDao.getAllQuizResults()
}