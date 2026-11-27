package com.example.campusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
<<<<<<< Updated upstream
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.ui.navigation.CampusFlowNavGraph
import com.example.campusflow.ui.theme.CampusFlowTheme
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.ProfileSettingsViewModel
=======
import com.example.campusflow.data.preferences.UserProfile
import com.example.campusflow.ui.navigation.CampusFlowNavGraph
import com.example.campusflow.ui.theme.CampusFlowTheme
import com.example.campusflow.ui.theme.ThemeMode
>>>>>>> Stashed changes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as CampusFlowApplication
        
        setContent {
<<<<<<< Updated upstream
            val settingsViewModel: ProfileSettingsViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            val settings by settingsViewModel.uiState.collectAsState()
            CampusFlowTheme(themeMode = settings.themeMode) {
=======
            val userProfile by app.container.userPreferencesRepository.userProfileFlow.collectAsState(
                initial = UserProfile()
            )
            
            val themeMode = if (userProfile.isDarkMode) ThemeMode.DARK else ThemeMode.SYSTEM

            CampusFlowTheme(themeMode = themeMode) {
                // A surface container using the 'background' color from the theme
>>>>>>> Stashed changes
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CampusFlowNavGraph()
                }
            }
        }
    }
}