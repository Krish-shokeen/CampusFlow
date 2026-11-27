package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.preferences.UserProfile
import com.example.campusflow.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {
    val userProfile = userPreferencesRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            userPreferencesRepository.updateProfile(profile)
        }
    }
}