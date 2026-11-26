package com.example.campusflow.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.ProfileSettingsViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val profile by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        name = profile.name
        course = profile.course
        semester = profile.semester
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Your profile", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Personalize CampusFlow with your academic details.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(name, { name = it; saved = false }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(course, { course = it; saved = false }, label = { Text("Course") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(semester, { semester = it; saved = false }, label = { Text("Semester") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { viewModel.saveProfile(name, course, semester); saved = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (saved) "Saved" else "Save profile") }
    }
}
