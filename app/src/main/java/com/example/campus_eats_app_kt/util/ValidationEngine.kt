package com.example.campus_eats_app_kt.util

/**
 * ValidationEngine centralizes the input validation logic for the application.
 */
object ValidationEngine
{
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    /**
     * Requirement: Test email validation
     * Validates if the string follows a standard email format.
     */
    fun isValidEmail(email: String): Boolean
    {
        return email.isNotBlank() && EMAIL_REGEX.matches(email)
    }

    /**
     * Requirement: Test password validation (strength requirements)
     * Validates password strength: Minimum 8 characters.
     */
    fun isStrongPassword(password: String): Boolean
    {
        return password.length >= 8
    }

    /**
     * Requirement: Test price validation (positive values)
     */
    fun isValidPrice(price: Double): Boolean
    {
        return price > 0
    }

    /**
     * Requirement: Test quantity validation (positive integers)
     */
    fun isValidQuantity(quantity: Int): Boolean
    {
        return quantity > 0
    }
}
