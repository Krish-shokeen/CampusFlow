package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.QuizDao
import com.example.campusflow.data.entities.QuizQuestion
import com.example.campusflow.data.entities.QuizResult
import com.example.campusflow.data.remote.FirebaseErrorMapper
import com.example.campusflow.data.remote.FirestoreMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

data class QuizAttemptSummary(
    val correctCount: Int,
    val incorrectCount: Int,
    val totalQuestions: Int,
    val percentage: Float
)

@OptIn(ExperimentalCoroutinesApi::class)
class QuizRepository(
    private val quizDao: QuizDao,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {
    suspend fun getQuestionsByCategory(category: String): List<QuizQuestion> =
        quizDao.getQuestionsByCategory(category)

    fun getQuizCategoriesStream(): Flow<List<String>> = quizDao.getQuizCategories()

    suspend fun insertQuestion(question: QuizQuestion) = quizDao.insertQuestion(question)

    suspend fun insertQuestions(questions: List<QuizQuestion>) = quizDao.insertQuestions(questions)

    fun evaluateQuizAttempt(
        questions: List<QuizQuestion>,
        userAnswers: Map<Int, String>
    ): QuizAttemptSummary {
        val total = questions.size
        val correct = questions.mapIndexed { index, question ->
            if (userAnswers[index] == question.correctAnswer) 1 else 0
        }.sum()
        val incorrect = (total - correct).coerceAtLeast(0)
        val percentage = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        return QuizAttemptSummary(
            correctCount = correct,
            incorrectCount = incorrect,
            totalQuestions = total,
            percentage = percentage
        )
    }

    suspend fun insertQuizResult(result: QuizResult) {
        val uid = authRepository.currentUid
            ?: throw IllegalStateException("Please sign in to save quiz results.")
        if (result.totalQuestions <= 0) {
            throw IllegalArgumentException("Quiz must contain at least one question.")
        }

        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("quizResults").document()
            val completedMillis = FirestoreMapper.localDateTimeToMillis(result.completedAt)
            val data = hashMapOf<String, Any>(
                "ownerUid" to uid,
                "category" to result.category,
                "score" to result.score,
                "correctCount" to result.correctCount,
                "incorrectCount" to result.incorrectCount,
                "totalQuestions" to result.totalQuestions,
                "percentage" to result.percentage,
                "timeTakenSeconds" to result.timeTakenSeconds,
                "completedAtMillis" to completedMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            throw IllegalStateException(FirebaseErrorMapper.map(e), e)
        }
    }

    fun getAllQuizResultsStream(): Flow<List<QuizResult>> =
        authRepository.authStateFlow.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val listener = firestore.collection("users").document(user.uid)
                        .collection("quizResults")
                        .orderBy("completedAtMillis", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val results = snapshot?.documents?.mapNotNull { doc ->
                                mapDocumentToResult(doc)
                            } ?: emptyList()
                            trySend(results)
                        }
                    awaitClose { listener.remove() }
                }
            }
        }

    private fun mapDocumentToResult(doc: DocumentSnapshot): QuizResult? {
        val category = doc.getString("category") ?: return null
        val score = (doc.getLong("score") ?: 0L).toInt()
        val total = (doc.getLong("totalQuestions") ?: 0L).toInt()
        if (total <= 0) return null
        val completedMillis = doc.getLong("completedAtMillis")
        val timeTaken = (doc.getLong("timeTakenSeconds") ?: 0L).toInt()

        return QuizResult(
            id = FirestoreMapper.stableIntId(doc.id),
            category = category,
            score = score,
            totalQuestions = total,
            completedAt = completedMillis?.let { FirestoreMapper.millisToLocalDateTime(it) }
                ?: LocalDateTime.now(),
            timeTakenSeconds = timeTaken
        )
    }
}
