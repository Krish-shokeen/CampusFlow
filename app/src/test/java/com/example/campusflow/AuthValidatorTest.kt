package com.example.campusflow

import com.example.campusflow.data.remote.AuthValidator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validEmail_returnsNull() {
        assertNull(AuthValidator.validateEmail("student@krmu.edu.in"))
        assertNull(AuthValidator.validateEmail("  alice.smith+cs@example.com  "))
    }

    @Test
    fun invalidEmail_returnsError() {
        assertNotNull(AuthValidator.validateEmail(""))
        assertNotNull(AuthValidator.validateEmail("not-an-email"))
        assertNotNull(AuthValidator.validateEmail("user@domain"))
    }

    @Test
    fun strongPassword_returnsNull() {
        assertNull(AuthValidator.validatePassword("Campus2026"))
        assertNull(AuthValidator.validatePassword("Pass1234!"))
    }

    @Test
    fun weakPassword_returnsError() {
        assertNotNull(AuthValidator.validatePassword(""))
        assertNotNull(AuthValidator.validatePassword("short1"))
        assertNotNull(AuthValidator.validatePassword("onlyletters"))
        assertNotNull(AuthValidator.validatePassword("12345678"))
    }

    @Test
    fun registrationValidation_checksAllFieldsAndPasswordMatch() {
        assertNull(
            AuthValidator.validateRegistration(
                fullName = "Aarav Sharma",
                email = "aarav@university.edu",
                password = "Password123",
                confirmPassword = "Password123"
            )
        )
        assertNotNull(
            AuthValidator.validateRegistration(
                fullName = "A",
                email = "aarav@university.edu",
                password = "Password123",
                confirmPassword = "Password123"
            )
        )
        assertNotNull(
            AuthValidator.validateRegistration(
                fullName = "Aarav Sharma",
                email = "aarav@university.edu",
                password = "Password123",
                confirmPassword = "DifferentPassword123"
            )
        )
    }

    @Test
    fun loginValidation_checksEmailAndNonEmptyPassword() {
        assertNull(AuthValidator.validateLogin("user@example.com", "anypass"))
        assertNotNull(AuthValidator.validateLogin("bad", "anypass"))
        assertNotNull(AuthValidator.validateLogin("user@example.com", ""))
    }
}
