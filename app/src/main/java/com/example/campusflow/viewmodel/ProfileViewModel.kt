package com.example.campusflow.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.preferences.UserProfile
import com.example.campusflow.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileStatusState(
    val isSaving: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = userRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    private val _statusState = MutableStateFlow(ProfileStatusState())
    val statusState: StateFlow<ProfileStatusState> = _statusState.asStateFlow()

    fun refreshProfile() {
        viewModelScope.launch {
            userRepository.fetchProfile().onFailure { error ->
                _statusState.value = _statusState.value.copy(
                    errorMessage = error.message
                )
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _statusState.value = _statusState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
            userRepository.updateProfile(profile).fold(
                onSuccess = {
                    _statusState.value = _statusState.value.copy(
                        isSaving = false,
                        successMessage = "Profile updated"
                    )
                },
                onFailure = { error ->
                    _statusState.value = _statusState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Failed to update profile."
                    )
                }
            )
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _statusState.value = _statusState.value.copy(
                isUploadingImage = true,
                errorMessage = null,
                successMessage = null
            )
            userRepository.uploadProfileImage(imageUri).fold(
                onSuccess = {
                    _statusState.value = _statusState.value.copy(
                        isUploadingImage = false,
                        successMessage = "Profile photo updated"
                    )
                },
                onFailure = { error ->
                    _statusState.value = _statusState.value.copy(
                        isUploadingImage = false,
                        errorMessage = error.message ?: "Failed to upload profile image."
                    )
                }
            )
        }
    }

    fun clearStatusMessages() {
        _statusState.value = _statusState.value.copy(errorMessage = null, successMessage = null)
    }
}
