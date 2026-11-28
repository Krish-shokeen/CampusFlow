package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow

    val isAuthenticated: StateFlow<Boolean> = authRepository.authStateFlow
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isAuthenticated
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    userRepository.fetchProfile()
                    userRepository.registerCurrentFcmToken()
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to sign in."
                    )
                }
            )
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.register(fullName, email, password, confirmPassword)
            result.fold(
                onSuccess = {
                    userRepository.fetchProfile()
                    userRepository.registerCurrentFcmToken()
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to create account."
                    )
                }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        successMessage = "Password reset link sent to ${email.trim()}. Please check your inbox."
                    )
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to send reset email."
                    )
                }
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            userRepository.removeCurrentFcmToken()
            authRepository.logout()
            userRepository.clearLocalSessionData()
            _uiState.value = AuthUiState()
            onLoggedOut()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
