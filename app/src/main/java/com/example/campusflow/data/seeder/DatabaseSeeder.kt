package com.example.campusflow.data.seeder

import com.example.campusflow.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Seeds only the local sample Quiz question bank into Room.
 * Student-specific data (tasks, assignments, subjects, attendance, notes, study sessions)
 * is stored exclusively per-user in Cloud Firestore and is never hardcoded.
 */
object DatabaseSeeder {

    fun seedDatabase(container: AppContainer) {
        CoroutineScope(Dispatchers.IO).launch {
            val existingCategories = container.quizRepository.getQuizCategoriesStream().first().toSet()
            val missingQuestions = QuizData.questions.filter { it.category !in existingCategories }
            if (missingQuestions.isNotEmpty()) {
                container.quizRepository.insertQuestions(missingQuestions)
            }
        }
    }
}
