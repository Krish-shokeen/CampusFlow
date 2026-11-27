package com.example.campusflow.ui.profile

<<<<<<< Updated upstream
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
=======
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.preferences.UserProfile
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val profile by viewModel.userProfile.collectAsState()
    
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(profile.name) }
    var university by remember { mutableStateOf(profile.university) }
    var course by remember { mutableStateOf(profile.course) }
    var semester by remember { mutableStateOf(profile.semester) }
    
    LaunchedEffect(profile) {
        if (!isEditing) {
            name = profile.name
            university = profile.university
            course = profile.course
            semester = profile.semester
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
            
            if (isEditing) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = university, onValueChange = { university = it }, label = { Text("University") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            viewModel.updateProfile(UserProfile(name, university, course, semester, profile.isDarkMode))
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save") }
                }
            } else {
                Text(text = profile.name, style = MaterialTheme.typography.headlineMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("University: ${profile.university}", style = MaterialTheme.typography.bodyLarge)
                        Text("Course: ${profile.course}", style = MaterialTheme.typography.bodyLarge)
                        Text("Semester: ${profile.semester}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { isEditing = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit Profile")
                }
            }
        }
    }
}
>>>>>>> Stashed changes
