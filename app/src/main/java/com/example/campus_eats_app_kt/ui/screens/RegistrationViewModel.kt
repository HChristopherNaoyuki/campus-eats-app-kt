package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * RegistrationState represents the lifecycle of the user registration process.
 */
sealed interface RegistrationState
{
    data object Idle : RegistrationState
    data object Loading : RegistrationState
    data class Success(val user: UserEntity) : RegistrationState
    data class Error(val message: String) : RegistrationState
}

/**
 * RegistrationViewModel manages the state and business logic for the Registration screen.
 */
class RegistrationViewModel(private val authRepository: AuthRepository) : ViewModel()
{
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    /**
     * Attempts to register a new user in the system.
     * Validates input fields and handles repository interaction.
     */
    fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null
    )
    {
        // Preliminary input validation
        if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank())
        {
            _registrationState.value = RegistrationState.Error("Please fill in all fields")
            return
        }

        // Conditional validation for vendor roles
        if (role == UserRole.VENDOR && shopName.isNullOrBlank())
        {
            _registrationState.value = RegistrationState.Error("Shop name is required for vendors")
            return
        }

        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading

            try
            {
                // Call the repository to perform the registration
                val result = authRepository.register(
                    fullName = fullName,
                    username = username,
                    email = email,
                    password = password,
                    role = role,
                    shopName = shopName
                )

                result.onSuccess { user ->
                    // On successful registration, update state with the new user record
                    _registrationState.value = RegistrationState.Success(user)
                }.onFailure { exception ->
                    // Handle logical failures (e.g., email already registered)
                    _registrationState.value =
                        RegistrationState.Error(exception.message ?: "Registration failed")
                }
            }
            catch (e: Exception)
            {
                // Catch any unexpected runtime exceptions during registration
                _registrationState.value =
                    RegistrationState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /**
     * Resets the registration state to Idle.
     */
    fun resetState()
    {
        _registrationState.value = RegistrationState.Idle
    }
}
