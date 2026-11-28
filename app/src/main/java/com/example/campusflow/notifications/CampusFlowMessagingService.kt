package com.example.campusflow.notifications

import com.example.campusflow.CampusFlowApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CampusFlowMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = applicationContext as? CampusFlowApplication ?: return
        serviceScope.launch {
            app.container.userRepository.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "CampusFlow Reminder"
        val body = message.notification?.body ?: message.data["body"] ?: return
        val channelId = when (message.data["type"]) {
            "ASSIGNMENT_REMINDER" -> NotificationHelper.CHANNEL_ASSIGNMENTS
            "TASK_REMINDER" -> NotificationHelper.CHANNEL_TASKS
            "STUDY_REMINDER" -> NotificationHelper.CHANNEL_STUDY_PLANNER
            else -> NotificationHelper.CHANNEL_GENERAL
        }
        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            channelId = channelId
        )
    }
}
