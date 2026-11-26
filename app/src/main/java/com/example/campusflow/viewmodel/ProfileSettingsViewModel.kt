package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.repository.UserPreferencesRepository
import com.example.campusflow.data.repository.UserPreferences
import com.example.campusflow.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileSettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val uiState: StateFlow<UserPreferences> = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )

    fun saveProfile(name: String, course: String, semester: String) {
        viewModelScope.launch { preferencesRepository.saveProfile(name, course, semester) }
    }

    fun saveThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.saveThemeMode(themeMode) }
    }
}
