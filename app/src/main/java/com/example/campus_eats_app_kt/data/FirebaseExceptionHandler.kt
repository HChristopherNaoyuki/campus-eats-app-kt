package com.example.campus_eats_app_kt.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWebException

/**
 * FirebaseExceptionHandler provides a centralized mechanism for translating technical 
 * Firebase exceptions into user-friendly diagnostic messages.
 */
object FirebaseExceptionHandler
{
    /**
     * Maps a Throwable to a descriptive error message.
     * 
     * @param throwable The exception encountered during a Firebase operation.
     * @return A localized string suitable for display in the UI.
     */
    fun parse(throwable: Throwable): String
    {
        return when (throwable)
        {
            is FirebaseAuthInvalidCredentialsException ->
            {
                // Root Cause: Incorrect password, malformed email, or mismatched project configuration.
                "The email address or password you entered is incorrect. Please verify your credentials and try again."
            }
            is FirebaseAuthInvalidUserException ->
            {
                // Root Cause: Account has been disabled by an administrator or deleted.
                "This account has been disabled or no longer exists. Please contact system support."
            }
            is FirebaseAuthUserCollisionException ->
            {
                // Root Cause: User is trying to register with an email that is already registered.
                "An account with this email address already exists. Try signing in instead."
            }
            is FirebaseAuthRecentLoginRequiredException ->
            {
                // Root Cause: Sensitive operation (e.g., password change) requires a fresh session.
                "For security reasons, this action requires a recent login. Please sign out and sign back in to continue."
            }
            is FirebaseNetworkException ->
            {
                // Root Cause: Device is offline or Firebase servers are unreachable.
                "Network error occurred. Please check your internet connection."
            }
            is FirebaseAuthWebException ->
            {
                // Root Cause: Configuration mismatch (e.g., API Key restriction) or unauthorized domain.
                if (throwable.message?.contains("CONFIGURATION_NOT_FOUND") == true)
                {
                    "Authentication service is currently unavailable. Please ensure Email/Password provider is enabled in the Firebase Console."
                }
                else
                {
                    "A security configuration error occurred. Please contact the application administrator."
                }
            }
            else ->
            {
                throwable.message ?: "An unexpected authentication error occurred. Please try again."
            }
        }
    }
}
