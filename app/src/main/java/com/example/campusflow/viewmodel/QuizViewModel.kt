package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.QuizQuestion
import com.example.campusflow.data.entities.QuizResult
import com.example.campusflow.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class QuizSessionState { IDLE, CONFIG, ACTIVE, RESULT, REVIEW }

data class QuizHomeUiState(
    val categories: List<String> = emptyList(),
    val totalAttempted: Int = 0,
    val bestScore: Int = 0,
    val averageScore: Float = 0f,
    val recentResults: List<QuizResult> = emptyList(),
    val isLoading: Boolean = false
)

data class ActiveQuizUiState(
    val category: String = "",
    val state: QuizSessionState = QuizSessionState.IDLE,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(), // Question Index -> Option (A, B, C, D)
    val timeLeftSeconds: Int = 0,
    val score: Int = 0,
    val currentResultId: Int? = null
)

class QuizViewModel(private val quizRepository: QuizRepository) : ViewModel() {

    // --- HOME DASHBOARD STATE ---
    val homeUiState: StateFlow<QuizHomeUiState> = combine(
        quizRepository.getQuizCategoriesStream(),
        quizRepository.getAllQuizResultsStream()
    ) { categories, results ->
        val total = results.size
        val best = results.maxOfOrNull { it.score } ?: 0
        val avg = if (total > 0) results.map { it.score.toFloat() / it.totalQuestions }.average().toFloat() * 100f else 0f
        
        QuizHomeUiState(
            categories = categories,
            totalAttempted = total,
            bestScore = best,
            averageScore = avg,
            recentResults = results.take(10), // Show latest 10
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuizHomeUiState(isLoading = true))


    // --- ACTIVE QUIZ SESSION STATE ---
    private val _sessionState = MutableStateFlow(ActiveQuizUiState())
    val sessionState: StateFlow<ActiveQuizUiState> = _sessionState.asStateFlow()

    private var timerJob: Job? = null
    private var timePerQuestionConfig: Int = 30

    fun openConfig(category: String) {
        _sessionState.value = ActiveQuizUiState(category = category, state = QuizSessionState.CONFIG)
    }

    fun startQuiz(numQuestions: Int, timePerQuestion: Int) {
        val category = _sessionState.value.category
        timePerQuestionConfig = timePerQuestion
        
        viewModelScope.launch {
            // Load questions, shuffle them, and take requested amount
            val allQuestions = quizRepository.getQuestionsByCategory(category)
            val selectedQuestions = allQuestions.shuffled().take(numQuestions)
            
            _sessionState.value = _sessionState.value.copy(
                state = QuizSessionState.ACTIVE,
                questions = selectedQuestions,
                currentQuestionIndex = 0,
                userAnswers = emptyMap(),
                timeLeftSeconds = timePerQuestionConfig,
                score = 0
            )
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_sessionState.value.timeLeftSeconds > 0) {
                delay(1000)
                _sessionState.value = _sessionState.value.copy(
                    timeLeftSeconds = _sessionState.value.timeLeftSeconds - 1
                )
            }
            // Time is up for this question
            handleTimeUp()
        }
    }

    private fun handleTimeUp() {
        val currentIndex = _sessionState.value.currentQuestionIndex
        val totalQuestions = _sessionState.value.questions.size
        
        if (currentIndex < totalQuestions - 1) {
            nextQuestion()
        } else {
            submitQuiz()
        }
    }

    fun selectAnswer(answer: String) {
        val currentIndex = _sessionState.value.currentQuestionIndex
        val newAnswers = _sessionState.value.userAnswers.toMutableMap()
        newAnswers[currentIndex] = answer
        _sessionState.value = _sessionState.value.copy(userAnswers = newAnswers)
    }

    fun nextQuestion() {
        val currentIndex = _sessionState.value.currentQuestionIndex
        if (currentIndex < _sessionState.value.questions.size - 1) {
            _sessionState.value = _sessionState.value.copy(
                currentQuestionIndex = currentIndex + 1,
                timeLeftSeconds = timePerQuestionConfig
            )
            startTimer()
        }
    }

    fun prevQuestion() {
        val currentIndex = _sessionState.value.currentQuestionIndex
        if (currentIndex > 0) {
            _sessionState.value = _sessionState.value.copy(
                currentQuestionIndex = currentIndex - 1,
                timeLeftSeconds = timePerQuestionConfig // Reset timer for consistency when navigating back
            )
            startTimer()
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        
        val questions = _sessionState.value.questions
        val answers = _sessionState.value.userAnswers
        
        // Calculate score naturally using Kotlin collections
        val score = questions.mapIndexed { index, question -> 
            if (answers[index] == question.correctAnswer) 1 else 0 
        }.sum()

        val result = QuizResult(
            category = _sessionState.value.category,
            score = score,
            totalQuestions = questions.size,
            completedAt = LocalDateTime.now()
        )

        viewModelScope.launch {
            quizRepository.insertQuizResult(result)
            
            _sessionState.value = _sessionState.value.copy(
                state = QuizSessionState.RESULT,
                score = score
            )
        }
    }

    fun reviewAnswers() {
        _sessionState.value = _sessionState.value.copy(
            state = QuizSessionState.REVIEW,
            currentQuestionIndex = 0
        )
    }
    
    fun nextReviewQuestion() {
        val currentIndex = _sessionState.value.currentQuestionIndex
        if (currentIndex < _sessionState.value.questions.size - 1) {
            _sessionState.value = _sessionState.value.copy(currentQuestionIndex = currentIndex + 1)
        }
    }

    fun prevReviewQuestion() {
        val currentIndex = _sessionState.value.currentQuestionIndex
        if (currentIndex > 0) {
            _sessionState.value = _sessionState.value.copy(currentQuestionIndex = currentIndex - 1)
        }
    }

    fun resetSession() {
        timerJob?.cancel()
        _sessionState.value = ActiveQuizUiState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}