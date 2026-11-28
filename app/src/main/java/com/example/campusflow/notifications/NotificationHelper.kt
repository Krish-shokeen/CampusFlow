package com.example.campusflow.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.campusflow.MainActivity
import com.example.campusflow.R

object NotificationHelper {
    const val CHANNEL_GENERAL = "campusflow_general"
    const val CHANNEL_ASSIGNMENTS = "campusflow_assignments"
    const val CHANNEL_TASKS = "campusflow_tasks"
    const val CHANNEL_STUDY_PLANNER = "campusflow_study_planner"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General CampusFlow updates and reminders"
                },
                NotificationChannel(
                    CHANNEL_ASSIGNMENTS,
                    "Assignment Deadlines",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Upcoming assignment deadline alerts"
                },
                NotificationChannel(
                    CHANNEL_TASKS,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Pending task due date reminders"
                },
                NotificationChannel(
                    CHANNEL_STUDY_PLANNER,
                    "Study Session Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Scheduled study session notifications"
                }
            )
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String = CHANNEL_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
