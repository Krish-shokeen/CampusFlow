package com.example.campusflow.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusflow.ui.components.ProgressRing
import com.example.campusflow.viewmodel.AttendanceStatus
import com.example.campusflow.viewmodel.SubjectAttendanceStats
import java.util.Locale

@Composable
fun SubjectAttendanceCard(
    stats: SubjectAttendanceStats,
    onEditSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (stats.status) {
        AttendanceStatus.SAFE -> MaterialTheme.colorScheme.primary // Green/Primary
        AttendanceStatus.WARNING -> Color(0xFFF59E0B) // Amber
        AttendanceStatus.LOW -> MaterialTheme.colorScheme.error // Red
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stats.subject.subjectName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onEditSubject, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Subject", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Present: ${stats.present} | Absent: ${stats.absent} | Total: ${stats.total}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Threshold: ${String.format(Locale.US, "%.0f", stats.subject.attendanceThreshold)}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Status Warning Badges
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    val statusText = when (stats.status) {
                        AttendanceStatus.SAFE -> "Comfortably above threshold"
                        AttendanceStatus.WARNING -> "Near threshold limit"
                        AttendanceStatus.LOW -> "Below threshold!"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // If low, show how many classes needed
                if (stats.status == AttendanceStatus.LOW && stats.classesToReachThreshold > 0) {
                    Text(
                        text = "Attend next ${stats.classesToReachThreshold} classes to reach threshold.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (stats.status == AttendanceStatus.LOW && stats.classesToReachThreshold == -1) {
                    Text(
                        text = "Impossible to reach threshold.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Progress Ring
            ProgressRing(
                progress = stats.percentage / 100f,
                size = 72.dp,
                strokeWidth = 6.dp,
                progressColor = statusColor
            )
        }
    }
}