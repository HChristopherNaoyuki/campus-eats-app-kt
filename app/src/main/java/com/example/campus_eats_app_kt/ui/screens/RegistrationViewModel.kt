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

    fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null
    )
    {
        if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank())
        {
            _registrationState.value = RegistrationState.Error("Please fill in all fields")
            return
        }

        if (role == UserRole.VENDOR && shopName.isNullOrBlank())
        {
            _registrationState.value = RegistrationState.Error("Shop name is required for vendors")
            return
        }
        
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            val result =
                authRepository.register(fullName, username, email, password, role, shopName)
            result.onSuccess {
                _registrationState.value = RegistrationState.Success(it)
            }.onFailure {
                _registrationState.value = RegistrationState.Error(it.message ?: "Registration failed")
            }
        }
    }
}
