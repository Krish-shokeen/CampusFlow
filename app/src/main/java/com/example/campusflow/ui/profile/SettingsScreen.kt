package com.example.campusflow.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = profile.isDarkMode,
                    onCheckedChange = { isDark ->
                        viewModel.updateProfile(profile.copy(isDarkMode = isDark))
                    }
                )
            }
            
            HorizontalDivider()
            
            Text("About", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("CampusFlow - Smart Student Management System", style = MaterialTheme.typography.bodyLarge)
            Text("Version 1.0 (Capstone Project)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Developed for SEC042 - New Age Programming Languages Lab", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}