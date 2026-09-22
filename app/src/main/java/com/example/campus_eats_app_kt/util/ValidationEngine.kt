package com.example.campus_eats_app_kt.util

/**
 * ValidationEngine centralizes the input validation logic for the application.
 */
object ValidationEngine
{
    private val EMAIL_REGEX = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()

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
     * SPEC v3.0.1 Section 2.2 - 2.6 Recovery Password Validation:
     * - Minimum 8 characters long
     * - Contains lowercase letters
     * - Contains uppercase letters
     * - Contains at least one digit
     * - Contains at least one special character
     */
    fun isValidRecoveryPassword(password: String): Boolean
    {
        return password.length >= 8 &&
                password.any { it.isLowerCase() } &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
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
