package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * LoginState represents the various states of the login process.
 */
sealed interface LoginState
{
    data object Idle : LoginState
    data object Loading : LoginState
    data class Success(val user: UserEntity) : LoginState
    data class Error(val message: String) : LoginState
}

/**
 * LoginViewModel manages the state and logic for the Login screen.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel()
{
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    /**
     * Attempts to log in the user with the provided credentials.
     * Performs validation and updates the loginState accordingly.
     */
    fun login(email: String, password: String)
    {
        // Simple input validation to prevent unnecessary processing
        if (email.isBlank() || password.isBlank())
        {
            _loginState.value = LoginState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try
            {
                // Call the repository to perform the login operation
                val result = authRepository.login(email, password)

                result.onSuccess { user ->
                    // On success, update the state with the authenticated user
                    _loginState.value = LoginState.Success(user)
                }.onFailure { exception ->
                    // On failure, capture the error message for user feedback
                    _loginState.value = LoginState.Error(exception.message ?: "Login failed")
                }
            }
            catch (e: Exception)
            {
                // Catch any unexpected exceptions to prevent application termination
                _loginState.value = LoginState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /**
     * Resets the login state to Idle.
     */
    fun resetState()
    {
        _loginState.value = LoginState.Idle
    }
}
