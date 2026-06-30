package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface RegistrationState {
    data object Idle : RegistrationState
    data object Loading : RegistrationState
    data class Success(val user: UserEntity) : RegistrationState
    data class Error(val message: String) : RegistrationState
}

class RegistrationViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun register(fullName: String, email: String, password: String, role: UserRole) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _registrationState.value = RegistrationState.Error("Please fill in all fields")
            return
        }
        
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            val result = authRepository.register(fullName, email, password, role)
            result.onSuccess {
                _registrationState.value = RegistrationState.Success(it)
            }.onFailure {
                _registrationState.value = RegistrationState.Error(it.message ?: "Registration failed")
            }
        }
    }
}
