package com.example.campusflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

// Sealed classes are perfect for defining restricted hierarchies.
// Here we use them for defining possible navigation routes.
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Authentication routes
    object Login : Screen("login", "Login", Icons.Filled.Home)
    object Register : Screen("register", "Register", Icons.Filled.Home)
    object ForgotPassword : Screen("forgot_password", "Forgot Password", Icons.Filled.Home)

    // Objects are singletons in Kotlin, representing screens without arguments
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.AutoMirrored.Filled.List)
    object Assignments : Screen("assignments", "Assignments", Icons.AutoMirrored.Filled.Assignment)
    object Quiz : Screen("quiz", "Quiz", Icons.Filled.Quiz)
    object More : Screen("more", "More", Icons.Filled.Menu)

    // Additional screens accessed from 'More'
    object Attendance : Screen("attendance", "Attendance", Icons.Filled.Menu)
    object Notes : Screen("notes", "Notes", Icons.Filled.Menu)
    object Planner : Screen("planner", "Planner", Icons.Filled.Menu)
    object Statistics : Screen("statistics", "Statistics", Icons.Filled.Menu)
    object Profile : Screen("profile", "Profile", Icons.Filled.Menu)
    object Settings : Screen("settings", "Settings", Icons.Filled.Menu)

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

    // Notes module routes
    object NoteEntry : Screen("note_entry?noteId={noteId}", "Note Entry", Icons.Filled.Menu) {
        fun createRoute(noteId: Int? = null) = if (noteId != null) "note_entry?noteId=$noteId" else "note_entry?noteId=-1"
    }

    // Study Planner module routes
    object SessionEntry : Screen("session_entry?sessionId={sessionId}", "Session Entry", Icons.Filled.Menu) {
        fun createRoute(sessionId: Int? = null) = if (sessionId != null) "session_entry?sessionId=$sessionId" else "session_entry?sessionId=-1"
    }

    companion object {
        // A list of main screens for the Bottom Navigation Bar
        val bottomNavItems = listOf(Home, Tasks, Assignments, Quiz, More)
        val authRoutes = setOf(Login.route, Register.route, ForgotPassword.route)
    }
}
