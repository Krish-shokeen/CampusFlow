package com.example.campusflow.ui.assignments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AssignmentDetails

@Composable
fun AssignmentCard(
    details: AssignmentDetails,
    onAssignmentClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assignment = details.assignment
    val subject = details.subject
    val isCompleted = assignment.isCompleted
    val isOverdue = !isCompleted && DateUtils.isOverdue(assignment.dueDate)
    val daysRemaining = DateUtils.getDaysRemaining(assignment.dueDate)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAssignmentClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Subject Badge and Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Subject Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = subject?.subjectName ?: "Unknown Subject",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = onCheckedChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title and Description
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleLarge,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (assignment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = assignment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row: Due Date, Priority, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due Date & Days Remaining
                if (assignment.dueDate != null) {
                    val statusColor = when {
                        isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                        isOverdue -> MaterialTheme.colorScheme.error
                        daysRemaining != null && daysRemaining <= 2 -> Color(0xFFF59E0B) // Amber
                        else -> MaterialTheme.colorScheme.primary
                    }

                    val statusText = when {
                        isCompleted -> DateUtils.formatDate(assignment.dueDate)
                        isOverdue -> "Overdue by ${-daysRemaining!!} days"
                        daysRemaining == 0L -> "Due Today"
                        daysRemaining == 1L -> "Due Tomorrow"
                        else -> "Due in $daysRemaining days"
                    }

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Deadline",
                        modifier = Modifier.size(16.dp),
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Priority Badge
                val (priorityColor, priorityText) = when (assignment.priority) {
                    Priority.HIGH -> Color(0xFFE11D48) to "High Priority"
                    Priority.MEDIUM -> Color(0xFFF59E0B) to "Med Priority"
                    Priority.LOW -> Color(0xFF16A34A) to "Low Priority"
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

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Assignment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}