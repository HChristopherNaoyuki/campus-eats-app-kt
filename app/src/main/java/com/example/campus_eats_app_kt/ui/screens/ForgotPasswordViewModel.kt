package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.util.ValidationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ResetState represents the state of a password recovery attempt.
 */
sealed interface ResetState
{
    data object Idle : ResetState
    data object Loading : ResetState
    data object Success : ResetState
    data class Error(val message: String) : ResetState
}

/**
 * ForgotPasswordViewModel manages account recovery logic via Firebase Auth email verification.
 */
class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel()
{
    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState: StateFlow<ResetState> = _resetState

    /**
     * Password recovery logic with validation and repository routing.
     */
    fun resetPassword(userId: String, newPassword: String, confirmPassword: String)
    {
        if (userId.isBlank())
        {
            _resetState.value = ResetState.Error("User ID is required.")
            return
        }

        if (newPassword.isBlank() || confirmPassword.isBlank())
        {
            _resetState.value = ResetState.Error("Please enter and confirm your new password.")
            return
        }

        if (newPassword != confirmPassword)
        {
            _resetState.value = ResetState.Error("New password and confirm password must match.")
            return
        }

        if (!ValidationEngine.isValidRecoveryPassword(newPassword))
        {
            _resetState.value = ResetState.Error(
                "Password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character."
            )
            return
        }

        viewModelScope.launch {
            _resetState.value = ResetState.Loading

            try
            {
                val result = if (userId.contains("@"))
                {
                    authRepository.sendRecoveryEmail(userId)
                }
                else
                {
                    authRepository.resetPasswordByUserId(userId, newPassword)
                }

                result.onSuccess {
                    _resetState.value = ResetState.Success
                }.onFailure {
                    _resetState.value = ResetState.Error(it.message ?: "Account recovery failed.")
                }
            }
            catch (e: Exception)
            {
                _resetState.value = ResetState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /**
     * Sends password recovery email via Firebase Auth.
     */
    fun sendRecoveryEmail(email: String)
    {
        if (email.isBlank() || !ValidationEngine.isValidEmail(email))
        {
            _resetState.value = ResetState.Error("Please enter a valid registered email address.")
            return
        }

        viewModelScope.launch {
            _resetState.value = ResetState.Loading

            try
            {
                val result = authRepository.sendRecoveryEmail(email)
                result.onSuccess {
                    _resetState.value = ResetState.Success
                }.onFailure {
                    _resetState.value = ResetState.Error(it.message ?: "Recovery email failed to send.")
                }
            }
            catch (e: Exception)
            {
                _resetState.value = ResetState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
}
