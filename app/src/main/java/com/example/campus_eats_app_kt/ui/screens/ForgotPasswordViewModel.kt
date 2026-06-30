package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ResetState {
    data object Idle : ResetState
    data object Loading : ResetState
    data object Success : ResetState
    data class Error(val message: String) : ResetState
}

class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState: StateFlow<ResetState> = _resetState

    fun resetPassword(userId: String, newPassword: String) {
        if (userId.isBlank() || newPassword.isBlank()) {
            _resetState.value = ResetState.Error("Please fill in all fields")
            return
        }
        
        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            val result = authRepository.resetPassword(userId, newPassword)
            result.onSuccess {
                _resetState.value = ResetState.Success
            }.onFailure {
                _resetState.value = ResetState.Error(it.message ?: "Reset failed")
            }
        }
    }
}
