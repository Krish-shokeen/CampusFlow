package com.example.campusflow.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusflow.ui.navigation.Screen

// Data class representing a menu item on the More screen
data class MoreMenuItem(val title: String, val route: String)

@Composable
fun MoreScreen(navController: NavController) {
    // listOf creates a read-only List in Kotlin
    val menuItems = listOf(
        MoreMenuItem("Attendance", Screen.Attendance.route),
        MoreMenuItem("Notes", Screen.Notes.route),
        MoreMenuItem("Study Planner", Screen.Planner.route),
        MoreMenuItem("Statistics", Screen.Statistics.route),
        MoreMenuItem("Profile", Screen.Profile.route),
        MoreMenuItem("Settings", Screen.Settings.route)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "More Options",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        // LazyColumn is Compose's equivalent to RecyclerView
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            // items() is a DSL extension function for LazyListScope taking a List
            items(menuItems) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    modifier = Modifier.clickable {
                        navController.navigate(item.route)
                    }
                )
                HorizontalDivider() // Use HorizontalDivider in M3
            }
        }
    }
}