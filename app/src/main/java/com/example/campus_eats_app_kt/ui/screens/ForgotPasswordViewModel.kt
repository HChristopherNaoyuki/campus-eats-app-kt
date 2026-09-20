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
 * ForgotPasswordViewModel manages the logic for password recovery via Firebase Email.
 * Finding 7: Hardened to handle signed-out account recovery.
 */
class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel()
{
    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState: StateFlow<ResetState> = _resetState

    /**
     * Triggers a Firebase password reset email.
     */
    fun sendRecoveryEmail(email: String)
    {
        if (email.isBlank())
        {
            _resetState.value = ResetState.Error("Please enter your registered email address.")
            return
        }

        viewModelScope.launch()
        {
            _resetState.value = ResetState.Loading

            try
            {
                val result = authRepository.sendRecoveryEmail(email)
                result.onSuccess()
                {
                    _resetState.value = ResetState.Success
                }.onFailure()
                {
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
