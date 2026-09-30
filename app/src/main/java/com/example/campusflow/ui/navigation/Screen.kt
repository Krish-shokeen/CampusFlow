package com.example.campusflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

// Sealed classes are perfect for defining restricted hierarchies.
// Here we use them for defining possible navigation routes.
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Objects are singletons in Kotlin, representing screens without arguments
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.List)
    object Assignments : Screen("assignments", "Assignments", Icons.Filled.Assignment)
    object Quiz : Screen("quiz", "Quiz", Icons.Filled.Quiz)
    object More : Screen("more", "More", Icons.Filled.Menu)
    
    // Additional screens accessed from 'More'
    object Attendance : Screen("attendance", "Attendance", Icons.Filled.Menu)
    object Notes : Screen("notes", "Notes", Icons.Filled.Menu)
    object Planner : Screen("planner", "Planner", Icons.Filled.Menu)
    object Statistics : Screen("statistics", "Statistics", Icons.Filled.Menu)
    object Profile : Screen("profile", "Profile", Icons.Filled.Menu)
    object Settings : Screen("settings", "Settings", Icons.Filled.Menu)

    companion object {
        // A list of main screens for the Bottom Navigation Bar
        val bottomNavItems = listOf(Home, Tasks, Assignments, Quiz, More)
    }
}