package com.example.campusflow.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.ui.components.ProgressRing
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.QuizSessionState
import com.example.campusflow.viewmodel.QuizViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSessionScreen(
    category: String,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessionState by viewModel.sessionState.collectAsState()
    
    // Initialize the flow based on the route category
    LaunchedEffect(category) {
        if (sessionState.state == QuizSessionState.IDLE) {
            viewModel.openConfig(category)
        }
    }
    
    // Clean up when leaving screen
    DisposableEffect(Unit) {
        onDispose { viewModel.resetSession() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
            when (sessionState.state) {
                QuizSessionState.IDLE, QuizSessionState.CONFIG -> QuizConfigView(viewModel)
                QuizSessionState.ACTIVE -> ActiveQuizView(viewModel)
                QuizSessionState.RESULT -> QuizResultView(viewModel, navigateBack)
                QuizSessionState.REVIEW -> QuizReviewView(viewModel)
            }
        }
    }
}

@Composable
fun QuizConfigView(viewModel: QuizViewModel) {
    var selectedCount by remember { mutableStateOf(5) }
    var selectedTime by remember { mutableStateOf(30) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configure Quiz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Number of Questions", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            listOf(5, 10, 15).forEach { count ->
                FilterChip(
                    selected = selectedCount == count,
                    onClick = { selectedCount = count },
                    label = { Text("$count") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Time per Question (seconds)", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            listOf(15, 30, 60).forEach { time ->
                FilterChip(
                    selected = selectedTime == time,
                    onClick = { selectedTime = time },
                    label = { Text("${time}s") }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.startQuiz(selectedCount, selectedTime) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Quiz", modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ActiveQuizView(viewModel: QuizViewModel) {
    val state by viewModel.sessionState.collectAsState()
    if (state.questions.isEmpty()) return

    val currentQ = state.questions[state.currentQuestionIndex]
    val selectedAnswer = state.userAnswers[state.currentQuestionIndex]
    
    var showSubmitDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Info Row (Progress & Timer)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Question ${state.currentQuestionIndex + 1} of ${state.questions.size}", style = MaterialTheme.typography.titleMedium)
            
            Surface(
                color = if (state.timeLeftSeconds <= 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${state.timeLeftSeconds}s", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        LinearProgressIndicator(
            progress = (state.currentQuestionIndex + 1).toFloat() / state.questions.size,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        )

        // Question Text
        Text(
            text = currentQ.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Options
        val options = listOf("A" to currentQ.optionA, "B" to currentQ.optionB, "C" to currentQ.optionC, "D" to currentQ.optionD)
        
        options.forEach { (key, text) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedAnswer == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { viewModel.selectAnswer(key) }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$key.", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                    Text(text)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { viewModel.prevQuestion() },
                enabled = state.currentQuestionIndex > 0
            ) { Text("Previous") }
            
            if (state.currentQuestionIndex == state.questions.size - 1) {
                Button(onClick = { showSubmitDialog = true }) { Text("Submit Quiz") }
            } else {
                Button(onClick = { viewModel.nextQuestion() }) { Text("Next") }
            }
        }

        if (showSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Submit Quiz") },
                text = { Text("Are you sure you want to submit? You cannot change your answers after submission.") },
                confirmButton = { TextButton(onClick = { showSubmitDialog = false; viewModel.submitQuiz() }) { Text("Submit") } },
                dismissButton = { TextButton(onClick = { showSubmitDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
fun QuizResultView(viewModel: QuizViewModel, navigateBack: () -> Unit) {
    val state by viewModel.sessionState.collectAsState()
    val total = state.questions.size
    val score = state.score
    val percentage = if (total > 0) (score.toFloat() / total) * 100f else 0f
    
    val message = when {
        percentage >= 90 -> "Outstanding!"
        percentage >= 70 -> "Great Job!"
        percentage >= 50 -> "Good Effort!"
        else -> "Keep Practicing!"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Quiz Complete", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ProgressRing(
            progress = percentage / 100f,
            size = 150.dp,
            strokeWidth = 12.dp,
            progressColor = if (percentage >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelMedium)
                    Text("$total", style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Correct", style = MaterialTheme.typography.labelMedium)
                    Text("$score", style = MaterialTheme.typography.titleLarge, color = Color(0xFF16A34A))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Incorrect", style = MaterialTheme.typography.labelMedium)
                    Text("${total - score}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { viewModel.reviewAnswers() }, modifier = Modifier.fillMaxWidth()) {
            Text("Review Answers")
        }
        OutlinedButton(onClick = { viewModel.openConfig(state.category) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Retake Quiz")
        }
        TextButton(onClick = navigateBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Back to Home")
        }
    }
}

@Composable
fun QuizReviewView(viewModel: QuizViewModel) {
    val state by viewModel.sessionState.collectAsState()
    val currentQ = state.questions[state.currentQuestionIndex]
    val userAnswer = state.userAnswers[state.currentQuestionIndex]
    val isCorrect = userAnswer == currentQ.correctAnswer

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Question ${state.currentQuestionIndex + 1} of ${state.questions.size}", style = MaterialTheme.typography.titleMedium)
        
        Surface(
            color = if (isCorrect) Color(0xFF16A34A).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close, null, 
                    tint = if (isCorrect) Color(0xFF16A34A) else MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCorrect) "Correct" else "Incorrect", fontWeight = FontWeight.Bold, 
                    color = if (isCorrect) Color(0xFF16A34A) else MaterialTheme.colorScheme.error)
            }
        }

        Text(text = currentQ.question, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        val options = listOf("A" to currentQ.optionA, "B" to currentQ.optionB, "C" to currentQ.optionC, "D" to currentQ.optionD)
        
        options.forEach { (key, text) ->
            val isThisCorrect = key == currentQ.correctAnswer
            val isThisSelected = key == userAnswer
            
            val containerColor = when {
                isThisCorrect -> Color(0xFF16A34A).copy(alpha = 0.2f)
                isThisSelected && !isThisCorrect -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$key.", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                    Text(text)
                }
            }
        }

        // Explanation
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Explanation:", fontWeight = FontWeight.Bold)
                Text(currentQ.explanation, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { viewModel.prevReviewQuestion() }, enabled = state.currentQuestionIndex > 0) { Text("Previous") }
            Button(onClick = { viewModel.nextReviewQuestion() }, enabled = state.currentQuestionIndex < state.questions.size - 1) { Text("Next") }
        }
    }
}