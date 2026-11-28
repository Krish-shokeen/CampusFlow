package com.example.campusflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusflow.ui.assignments.AssignmentEntryScreen
import com.example.campusflow.ui.assignments.AssignmentsScreen
import com.example.campusflow.ui.attendance.AttendanceHistoryScreen
import com.example.campusflow.ui.attendance.AttendanceScreen
import com.example.campusflow.ui.attendance.MarkAttendanceScreen
import com.example.campusflow.ui.attendance.SubjectEntryScreen
import com.example.campusflow.ui.auth.ForgotPasswordScreen
import com.example.campusflow.ui.auth.LoginScreen
import com.example.campusflow.ui.auth.RegisterScreen
import com.example.campusflow.ui.dashboard.DashboardScreen
import com.example.campusflow.ui.more.MoreScreen
import com.example.campusflow.ui.notes.NoteEntryScreen
import com.example.campusflow.ui.notes.NotesScreen
import com.example.campusflow.ui.planner.PlannerScreen
import com.example.campusflow.ui.planner.SessionEntryScreen
import com.example.campusflow.ui.profile.ProfileScreen
import com.example.campusflow.ui.profile.SettingsScreen
import com.example.campusflow.ui.quiz.QuizHomeScreen
import com.example.campusflow.ui.quiz.QuizSessionScreen
import com.example.campusflow.ui.statistics.StatisticsScreen
import com.example.campusflow.ui.tasks.TaskEntryScreen
import com.example.campusflow.ui.tasks.TasksScreen
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.AuthViewModel

@Composable
fun CampusFlowNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isOnAuthRoute = currentRoute in Screen.authRoutes

    // Central auth gate: prevent unauthenticated access to protected screens
    LaunchedEffect(isAuthenticated, currentRoute) {
        if (!isAuthenticated && currentRoute != null && !isOnAuthRoute) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else if (isAuthenticated && isOnAuthRoute) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (isAuthenticated && !isOnAuthRoute) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Screen.Home.route else Screen.Login.route,
            modifier = modifier.padding(innerPadding)
        ) {
            // Authentication Flow
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Protected Main Screens
            composable(Screen.Home.route) {
                DashboardScreen(
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToAssignments = { navController.navigate(Screen.Assignments.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                    onNavigateToQuizzes = { navController.navigate(Screen.Quiz.route) },
                    onNavigateToPlanner = { navController.navigate(Screen.Planner.route) }
                )
            }
            composable(Screen.Tasks.route) {
                TasksScreen(
                    onNavigateToTaskEntry = { taskId ->
                        navController.navigate(Screen.TaskEntry.createRoute(taskId))
                    }
                )
            }
            composable(Screen.Assignments.route) {
                AssignmentsScreen(
                    onNavigateToAssignmentEntry = { assignmentId ->
                        navController.navigate(Screen.AssignmentEntry.createRoute(assignmentId))
                    }
                )
            }
            composable(Screen.Quiz.route) {
                QuizHomeScreen(
                    onNavigateToSession = { category -> navController.navigate(Screen.QuizSession.createRoute(category)) }
                )
            }
            composable(Screen.More.route) { MoreScreen(navController) }

            // Sub-screen for Task Entry (Add/Edit)
            composable(
                route = Screen.TaskEntry.route,
                arguments = listOf(navArgument("taskId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                TaskEntryScreen(
                    taskId = if (taskId == -1) null else taskId,
                    navigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen for Assignment Entry (Add/Edit)
            composable(
                route = Screen.AssignmentEntry.route,
                arguments = listOf(navArgument("assignmentId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val assignmentId = backStackEntry.arguments?.getInt("assignmentId") ?: -1
                AssignmentEntryScreen(
                    assignmentId = if (assignmentId == -1) null else assignmentId,
                    navigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screens for 'More' tab
            composable(Screen.Attendance.route) {
                AttendanceScreen(
                    onNavigateToSubjectEntry = { subjectId -> navController.navigate(Screen.SubjectEntry.createRoute(subjectId)) },
                    onNavigateToMarkAttendance = { navController.navigate(Screen.MarkAttendance.createRoute(null)) },
                    onNavigateToHistory = { navController.navigate(Screen.AttendanceHistory.route) }
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    onNavigateToNoteEntry = { noteId -> navController.navigate(Screen.NoteEntry.createRoute(noteId)) }
                )
            }
            composable(Screen.Planner.route) {
                PlannerScreen(
                    onNavigateToSessionEntry = { sessionId -> navController.navigate(Screen.SessionEntry.createRoute(sessionId)) }
                )
            }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onSignOut = {
                        authViewModel.logout {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navigateBack = { navController.popBackStack() },
                    onSignOut = {
                        authViewModel.logout {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // Dynamic Routes for Attendance Module
            composable(
                route = Screen.SubjectEntry.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
                SubjectEntryScreen(
                    subjectId = if (subjectId == -1) null else subjectId,
                    navigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MarkAttendance.route,
                arguments = listOf(navArgument("recordId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getInt("recordId") ?: -1
                MarkAttendanceScreen(
                    recordId = if (recordId == -1) null else recordId,
                    navigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AttendanceHistory.route) {
                AttendanceHistoryScreen(
                    onNavigateToEditRecord = { recordId -> navController.navigate(Screen.MarkAttendance.createRoute(recordId)) },
                    navigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.QuizSession.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                QuizSessionScreen(
                    category = category,
                    navigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.NoteEntry.route,
                arguments = listOf(navArgument("noteId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                NoteEntryScreen(
                    noteId = if (noteId == -1) null else noteId,
                    navigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SessionEntry.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: -1
                SessionEntryScreen(
                    sessionId = if (sessionId == -1) null else sessionId,
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Screen.bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
