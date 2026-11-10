package com.example.campusflow.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.ui.components.StatCard
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.QuizViewModel
import java.util.Locale

@Composable
fun QuizHomeScreen(
    onNavigateToSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeState by viewModel.homeUiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // Header
        Text(
            text = "Test Your Knowledge",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Learn, practice and improve.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(title = "Quizzes", value = homeState.totalAttempted.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "Best Score", value = homeState.bestScore.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "Average", value = String.format(Locale.US, "%.1f%%", homeState.averageScore), modifier = Modifier.weight(1f))
        }

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (homeState.categories.isEmpty()) {
            EmptyState(title = "No Quizzes", message = "Loading quiz database...", icon = Icons.Default.Psychology)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(homeState.categories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToSession(category) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = category, style = MaterialTheme.typography.titleMedium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Start")
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    if (homeState.recentResults.isNotEmpty()) {
                        Text(
                            text = "Recent Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                items(homeState.recentResults) { result ->
                    ListItem(
                        headlineContent = { Text(result.category) },
                        supportingContent = { Text(DateUtils.formatDateTime(result.completedAt)) },
                        trailingContent = {
                            Text(
                                "${result.score}/${result.totalQuestions}", 
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}