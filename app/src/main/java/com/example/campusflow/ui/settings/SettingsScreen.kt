package com.example.campusflow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.ui.theme.ThemeMode
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.ProfileSettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: ProfileSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val preferences by viewModel.uiState.collectAsState()

    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Appearance",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        ThemeMode.entries.forEach { mode ->
            Row(
                Modifier.fillMaxWidth().clickable { viewModel.saveThemeMode(mode) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = preferences.themeMode == mode,
                    onClick = { viewModel.saveThemeMode(mode) }
                )
                Column(Modifier.padding(start = 8.dp)) {
                    Text(
                        when (mode) {
                            ThemeMode.SYSTEM -> "System default"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (mode == ThemeMode.SYSTEM) {
                        Text("Follow your device appearance", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
