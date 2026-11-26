package com.example.campusflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campusflow.ui.dashboard.DashboardScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.campusflow.ui.tasks.TaskEntryScreen
import com.example.campusflow.ui.tasks.TasksScreen
import com.example.campusflow.ui.assignments.AssignmentEntryScreen
import com.example.campusflow.ui.assignments.AssignmentsScreen
import com.example.campusflow.ui.attendance.AttendanceHistoryScreen
import com.example.campusflow.ui.attendance.AttendanceScreen
import com.example.campusflow.ui.attendance.MarkAttendanceScreen
import com.example.campusflow.ui.attendance.SubjectEntryScreen
import com.example.campusflow.ui.quiz.QuizHomeScreen
import com.example.campusflow.ui.quiz.QuizSessionScreen
import com.example.campusflow.ui.more.MoreScreen
import com.example.campusflow.ui.notes.NotesScreen
import com.example.campusflow.ui.planner.StudyPlannerScreen
import com.example.campusflow.ui.profile.ProfileScreen
import com.example.campusflow.ui.settings.SettingsScreen
import com.example.campusflow.ui.statistics.StatisticsScreen

@Composable
fun CampusFlowNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        // NavHost links the NavController to a navigation graph where composable destinations are defined
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = modifier.padding(innerPadding)
        ) {
            // Extension function composable() from navigation-compose defines a route
            composable(Screen.Home.route) {
                DashboardScreen(
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToAssignments = { navController.navigate(Screen.Assignments.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
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
            composable(Screen.Notes.route) { NotesScreen() }
            composable(Screen.Planner.route) { StudyPlannerScreen() }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            
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
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        // By using 'by', we delegate property get to the State's value. 
        // Whenever the back stack changes, this will re-compose.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // iterate using forEach (higher order function) or just a simple loop
        Screen.bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                // Check if the current destination is part of the screen's hierarchy
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
