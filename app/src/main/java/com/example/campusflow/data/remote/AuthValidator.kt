package com.example.campusflow.data.remote

object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email address is required."
            !EMAIL_REGEX.matches(trimmed) -> "Please enter a valid email address."
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required."
            password.length < 8 -> "Password must be at least 8 characters long."
            !password.any { it.isDigit() } -> "Password must contain at least one number."
            !password.any { it.isLetter() } -> "Password must contain at least one letter."
            else -> null
        }
    }

    fun validateRegistration(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (fullName.trim().length < 2) {
            return "Please enter your full name (at least 2 characters)."
        }
        validateEmail(email)?.let { return it }
        validatePassword(password)?.let { return it }
        if (password != confirmPassword) {
            return "Passwords do not match."
        }
        return null
    }

    fun validateLogin(email: String, password: String): String? {
        validateEmail(email)?.let { return it }
        if (password.isEmpty()) {
            return "Password is required."
        }
        return null
    }
}
