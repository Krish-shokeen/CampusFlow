package com.example.campusflow

import android.app.Application
import com.example.campusflow.data.seeder.DatabaseSeeder
import com.example.campusflow.di.AppContainer
import com.example.campusflow.di.DefaultAppContainer

// Custom Application class to hold our AppContainer for manual Dependency Injection.
class CampusFlowApplication : Application() {
    
    // Using lateinit as we need to initialize it in onCreate once Context is ready
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        
        // Optional: Seed the database with sample data on first run
        DatabaseSeeder.seedDatabase(container)
    }
}