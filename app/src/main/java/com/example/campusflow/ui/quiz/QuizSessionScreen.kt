package com.example.campusflow.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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

    LaunchedEffect(category) {
        if (sessionState.state == QuizSessionState.IDLE) {
            viewModel.openConfig(category)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetSession() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
    var selectedCount by remember { mutableIntStateOf(5) }
    var selectedTime by remember { mutableIntStateOf(30) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Configure Quiz Session", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Number of Questions", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            listOf(5, 10, 15).forEach { count ->
                FilterChip(
                    selected = selectedCount == count,
                    onClick = { selectedCount = count },
                    label = { Text("$count Questions") }
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
            Text("Start Quiz Session", modifier = Modifier.padding(8.dp))
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
    var isFlagged by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Live HUD Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Question ${state.currentQuestionIndex + 1} of ${state.questions.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

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
                    progress = { (state.currentQuestionIndex + 1).toFloat() / state.questions.size },
                    modifier = Modifier.fillMaxWidth()
                )

                // Question Jump Carousel
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(state.questions) { idx, _ ->
                        val isCurrent = idx == state.currentQuestionIndex
                        val isAnswered = state.userAnswers[idx] != null
                        Surface(
                            onClick = {
                                if (idx > state.currentQuestionIndex) {
                                    while (state.currentQuestionIndex < idx) viewModel.nextQuestion()
                                } else if (idx < state.currentQuestionIndex) {
                                    while (state.currentQuestionIndex > idx) viewModel.prevQuestion()
                                }
                            },
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isAnswered -> Color(0xFF16A34A)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Tool Bar (Flag & Hint)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { isFlagged = !isFlagged },
                color = if (isFlagged) Color(0xFFF59E0B).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = if (isFlagged) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFlagged) "Flagged" else "Flag", style = MaterialTheme.typography.labelSmall)
                }
            }

            TextButton(onClick = { showHint = !showHint }) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Formula Hint")
            }
        }

        if (showHint) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Peterson's Synchronization Criteria", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Guarantees mutual exclusion, progress, and bounded waiting for 2 processes via shared 'turn' variable.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Text
        Text(
            text = "Q${state.currentQuestionIndex + 1}. ${currentQ.question}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Code block preview if question contains code
        if (currentQ.question.contains("turn") || currentQ.question.contains("flag")) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "flag[0] = true;\nturn = 1;\nwhile (flag[1] && turn == 1); // spin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

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
                    Text("Option $key:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                    Text(text)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                Button(onClick = { viewModel.nextQuestion() }) { Text("Save & Next") }
            }
        }

        if (showSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Submit Quiz") },
                text = { Text("Are you sure you want to submit your responses?") },
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

    val gradeText = when {
        percentage >= 90 -> "Grade A+ • Top 2% of Class"
        percentage >= 80 -> "Grade A • Top 8% of Class"
        percentage >= 70 -> "Grade B+ • Above Average"
        else -> "Grade C • Keep Practicing"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Celebratory Score Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(gradeText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }

                ProgressRing(
                    progress = percentage / 100f,
                    size = 110.dp,
                    strokeWidth = 10.dp,
                    progressColor = if (percentage >= 70) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                )

                Text("$score / $total Correct (${percentage.toInt()}%)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+180 FlowPoints", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("7-Day Streak", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4-Card Performance Stats Grid
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Correct", style = MaterialTheme.typography.labelSmall)
                    Text("$score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Incorrect", style = MaterialTheme.typography.labelSmall)
                    Text("${total - score}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Accuracy", style = MaterialTheme.typography.labelSmall)
                    Text("${percentage.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Time Taken", style = MaterialTheme.typography.labelSmall)
                    val mins = state.timeTakenSeconds / 60
                    val secs = state.timeTakenSeconds % 60
                    val timeLabel = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                    Text(timeLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                }
            }
        }

        // AI Insight Banner
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Study Insight", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Review Virtual Memory & Thrashing before Friday's OS Midterm.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Action Buttons
        Button(onClick = { viewModel.reviewAnswers() }, modifier = Modifier.fillMaxWidth()) {
            Text("Review Answers")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { viewModel.openConfig(state.category) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retake")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Result")
            }
        }
        TextButton(onClick = navigateBack, modifier = Modifier.fillMaxWidth()) {
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
                Icon(
                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    null,
                    tint = if (isCorrect) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isCorrect) "Correct Response" else "Incorrect Response",
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                )
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

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Concept Explanation:", fontWeight = FontWeight.Bold)
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
