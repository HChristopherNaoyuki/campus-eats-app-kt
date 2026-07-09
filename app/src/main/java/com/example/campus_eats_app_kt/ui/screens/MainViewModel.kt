package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import com.example.campus_eats_app_kt.data.AuthRepository

/**
 * MainViewModel manages the state for the main role-based navigation container.
 */
class MainViewModel(
    private val authRepository: AuthRepository,
    val userId: String,
    val role: String
) : ViewModel()
{
    // Implementation can be expanded for shared state between tabs if necessary.
}
