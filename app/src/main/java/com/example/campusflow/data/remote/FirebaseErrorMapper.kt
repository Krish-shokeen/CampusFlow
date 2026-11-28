package com.example.campusflow.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException

object FirebaseErrorMapper {

    fun map(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthWeakPasswordException ->
                "Password is too weak. Use at least 8 characters with letters and numbers."
            is FirebaseAuthInvalidCredentialsException ->
                "Invalid email or password. Please check your credentials and try again."
            is FirebaseAuthUserCollisionException ->
                "An account with this email address already exists."
            is FirebaseAuthInvalidUserException ->
                "No account found with this email address or the account has been disabled."
            is FirebaseTooManyRequestsException ->
                "Too many attempts. Please wait a few minutes and try again."
            is FirebaseNetworkException ->
                "Network unavailable. Please check your internet connection and try again."
            is FirebaseAuthException -> when (throwable.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                "ERROR_USER_DISABLED" -> "This account has been disabled."
                "ERROR_REQUIRES_RECENT_LOGIN" -> "Your session has expired. Please sign in again."
                else -> "Authentication failed. Please check your details and try again."
            }
            is FirebaseFirestoreException -> when (throwable.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "You do not have permission to access this data."
                FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                    "Your session has expired. Please sign in again."
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    "Cloud database is temporarily unavailable. Please try again shortly."
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    "The requested record could not be found."
                FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                    "A record with this identifier already exists."
                else -> "Unable to sync data with cloud storage. Please try again."
            }
            is StorageException -> when (throwable.errorCode) {
                StorageException.ERROR_NOT_AUTHENTICATED,
                StorageException.ERROR_NOT_AUTHORIZED ->
                    "You are not authorized to upload or access this file."
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                    "Upload timed out. Please check your network and retry."
                StorageException.ERROR_OBJECT_NOT_FOUND ->
                    "File not found in cloud storage."
                else -> "Failed to upload or load file. Please try again."
            }
            is IllegalArgumentException,
            is IllegalStateException -> throwable.message ?: "Invalid request."
            else -> throwable.message?.takeIf { it.isNotBlank() && !it.contains("Exception") }
                ?: "Something went wrong. Please try again."
        }
    }
}
