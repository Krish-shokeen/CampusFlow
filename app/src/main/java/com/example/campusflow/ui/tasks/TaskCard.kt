package com.example.campusflow.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.utils.DateUtils

@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isOverdue = !isCompleted && DateUtils.isOverdue(task.dueDate)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for quick completion
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onCheckedChange
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Task Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Badges Row (Due date & Priority)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Due Date Badge
                    if (task.dueDate != null) {
                        Surface(
                            color = if (isOverdue) MaterialTheme.colorScheme.errorContainer 
                                    else MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                if (isOverdue) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Overdue",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = DateUtils.formatDate(task.dueDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) MaterialTheme.colorScheme.onErrorContainer 
                                            else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    // Priority Badge
                    val (priorityColor, priorityText) = when (task.priority) {
                        Priority.HIGH -> Color(0xFFE11D48) to "High" // Rose
                        Priority.MEDIUM -> Color(0xFFF59E0B) to "Med" // Amber
                        Priority.LOW -> Color(0xFF16A34A) to "Low" // Green
                    }
                    
                    Surface(
                        color = priorityColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = priorityText,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Delete Button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}