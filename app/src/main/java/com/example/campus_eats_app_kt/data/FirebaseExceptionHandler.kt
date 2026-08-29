package com.example.campus_eats_app_kt.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
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
        // First check for specific exception types that indicate common issues
        return when (throwable)
        {
            is FirebaseAuthInvalidCredentialsException ->
            {
                // This covers ERROR_INVALID_CREDENTIAL, ERROR_INVALID_EMAIL, ERROR_WRONG_PASSWORD
                "The email address or password you entered is incorrect. Please verify your credentials and try again."
            }
            is FirebaseAuthRecentLoginRequiredException ->
            {
                "For security reasons, this action requires a recent login. Please sign out and sign back in to continue."
            }
            is FirebaseAuthUserCollisionException ->
            {
                "An account with this email address already exists. Try signing in instead."
            }
            is FirebaseAuthInvalidUserException ->
            {
                "This account has been disabled or no longer exists. Please contact system support."
            }
            is FirebaseNetworkException ->
            {
                "Network error occurred. Please check your internet connection."
            }
            is FirebaseAuthWebException ->
            {
                if (throwable.message?.contains("CONFIGURATION_NOT_FOUND") == true)
                {
                    "Authentication service is currently unavailable. Please ensure Email/Password provider is enabled in the Firebase Console."
                }
                else
                {
                    "A security configuration error occurred. Please contact the application administrator."
                }
            }
            is FirebaseAuthException ->
            {
                // Handle base FirebaseAuthException by checking error codes
                // This handles errors that don't have dedicated subclasses in the Android SDK
                when (throwable.errorCode)
                {
                    "ERROR_INVALID_CREDENTIAL" -> "The supplied authentication credential has expired or is malformed. Please try signing in again."
                    "ERROR_USER_TOKEN_EXPIRED" -> "Your session has expired. Please sign in again to refresh your credentials."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
                    "ERROR_WRONG_PASSWORD" -> "The password you entered is incorrect."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many unsuccessful attempts. Access to this account has been temporarily disabled due to suspicious activity. Please try again later."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "The authentication method used is currently disabled in the server configuration."
                    else -> throwable.message ?: "An authentication error occurred. Please try again."
                }
            }
            else ->
            {
                // Fallback for non-Auth exceptions (e.g. general Exception from RTDB or API)
                val message = throwable.message ?: ""
                
                // Specifically map the reported "auth credential" string if it leaks through non-Auth exceptions
                if (message.contains("supplied auth credential", ignoreCase = true))
                {
                    "Your session has expired or is invalid. Please sign out and sign back in."
                }
                else
                {
                    throwable.message ?: "An unexpected error occurred. Please try again."
                }
            }
        }
    }
}
