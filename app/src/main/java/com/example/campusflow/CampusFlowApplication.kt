package com.example.campusflow

import android.app.Application
import com.example.campusflow.data.seeder.DatabaseSeeder
import com.example.campusflow.di.AppContainer
import com.example.campusflow.di.DefaultAppContainer
import com.example.campusflow.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Custom Application class to hold our AppContainer for manual Dependency Injection.
class CampusFlowApplication : Application() {

    lateinit var container: AppContainer
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        NotificationHelper.createNotificationChannels(this)

        // Seed only the local quiz question bank
        DatabaseSeeder.seedDatabase(container)

        // Sync FCM token when an authenticated session is restored
        if (container.authRepository.isAuthenticated) {
            appScope.launch {
                container.userRepository.registerCurrentFcmToken()
            }
        }
    }
}
