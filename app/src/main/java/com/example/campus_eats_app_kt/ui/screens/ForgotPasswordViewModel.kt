package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
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
 * ForgotPasswordViewModel manages the logic for resetting a user's password using their unique User ID.
 */
class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel()
{
    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState: StateFlow<ResetState> = _resetState

    /**
     * Attempts to reset the user's password.
     * Performs basic validation before calling the repository.
     */
    fun resetPassword(userId: String, newPassword: String)
    {
        if (userId.isBlank() || newPassword.isBlank())
        {
            _resetState.value = ResetState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _resetState.value = ResetState.Loading

            try
            {
                val result = authRepository.resetPassword(userId, newPassword)
                result.onSuccess {
                    _resetState.value = ResetState.Success
                }.onFailure {
                    _resetState.value = ResetState.Error(it.message ?: "Reset failed")
                }
            }
            catch (e: Exception)
            {
                _resetState.value = ResetState.Error("An error occurred: ${e.message}")
            }
        }
    }
}
