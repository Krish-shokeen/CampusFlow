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
    
    // Dynamic route for Task Add/Edit
    object TaskEntry : Screen("task_entry?taskId={taskId}", "Task Entry", Icons.Filled.Menu) {
        fun createRoute(taskId: Int? = null) = if (taskId != null) "task_entry?taskId=$taskId" else "task_entry?taskId=-1"
    }

    // Dynamic route for Assignment Add/Edit
    object AssignmentEntry : Screen("assignment_entry?assignmentId={assignmentId}", "Assignment Entry", Icons.Filled.Menu) {
        fun createRoute(assignmentId: Int? = null) = if (assignmentId != null) "assignment_entry?assignmentId=$assignmentId" else "assignment_entry?assignmentId=-1"
    }

    // Dynamic routes for Attendance module
    object SubjectEntry : Screen("subject_entry?subjectId={subjectId}", "Subject Entry", Icons.Filled.Menu) {
        fun createRoute(subjectId: Int? = null) = if (subjectId != null) "subject_entry?subjectId=$subjectId" else "subject_entry?subjectId=-1"
    }
    
    object MarkAttendance : Screen("mark_attendance?recordId={recordId}", "Mark Attendance", Icons.Filled.Menu) {
        fun createRoute(recordId: Int? = null) = if (recordId != null) "mark_attendance?recordId=$recordId" else "mark_attendance?recordId=-1"
    }

    object AttendanceHistory : Screen("attendance_history", "Attendance History", Icons.Filled.Menu)
    
    // Dynamic route for Quiz active session
    object QuizSession : Screen("quiz_session/{category}", "Quiz", Icons.Filled.Menu) {
        fun createRoute(category: String) = "quiz_session/$category"
    }
    
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