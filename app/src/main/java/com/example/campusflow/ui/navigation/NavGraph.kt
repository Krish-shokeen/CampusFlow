package com.example.campusflow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campusflow.ui.dashboard.DashboardScreen
import com.example.campusflow.ui.tasks.TasksScreen
import com.example.campusflow.ui.assignments.AssignmentsScreen
import com.example.campusflow.ui.quiz.QuizScreen
import com.example.campusflow.ui.more.MoreScreen

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
            composable(Screen.Home.route) { DashboardScreen() }
            composable(Screen.Tasks.route) { TasksScreen() }
            composable(Screen.Assignments.route) { AssignmentsScreen() }
            composable(Screen.Quiz.route) { QuizScreen() }
            composable(Screen.More.route) { MoreScreen(navController) }
            
            // Sub-screens for 'More' tab
            composable(Screen.Attendance.route) { PlaceholderScreen(Screen.Attendance.title) }
            composable(Screen.Notes.route) { PlaceholderScreen(Screen.Notes.title) }
            composable(Screen.Planner.route) { PlaceholderScreen(Screen.Planner.title) }
            composable(Screen.Statistics.route) { PlaceholderScreen(Screen.Statistics.title) }
            composable(Screen.Profile.route) { PlaceholderScreen(Screen.Profile.title) }
            composable(Screen.Settings.route) { PlaceholderScreen(Screen.Settings.title) }
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

// A simple temporary screen using Kotlin's default parameters
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title Screen - Coming Soon")
    }
}