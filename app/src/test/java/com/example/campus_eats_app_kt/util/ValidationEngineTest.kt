package com.example.campus_eats_app_kt.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ValidationEngineTest verifies input validation rules.
 */
class ValidationEngineTest
{
    /**
     * Requirement: Test email validation
     */
    @Test
    fun isValidEmail_verifiesFormat()
    {
        assertTrue(ValidationEngine.isValidEmail("test@example.com"))
        assertTrue(ValidationEngine.isValidEmail("user.name+tag@domain.co.za"))
        assertFalse(ValidationEngine.isValidEmail("invalid-email"))
        assertFalse(ValidationEngine.isValidEmail("missing@domain"))
        assertFalse(ValidationEngine.isValidEmail(""))
    }

    /**
     * Requirement: Test password validation (strength requirements)
     */
    @Test
    fun isStrongPassword_verifiesLength()
    {
        assertTrue(ValidationEngine.isStrongPassword("password123"))
        assertTrue(ValidationEngine.isStrongPassword("12345678"))
        assertFalse(ValidationEngine.isStrongPassword("short"))
        assertFalse(ValidationEngine.isStrongPassword(""))
    }

    /**
     * Requirement: Test price validation (positive values)
     */
    @Test
    fun isValidPrice_verifiesPositive()
    {
        assertTrue(ValidationEngine.isValidPrice(10.0))
        assertTrue(ValidationEngine.isValidPrice(0.01))
        assertFalse(ValidationEngine.isValidPrice(0.0))
        assertFalse(ValidationEngine.isValidPrice(-5.0))
    }

    /**
     * Requirement: Test quantity validation (positive integers)
     */
    @Test
    fun isValidQuantity_verifiesPositive()
    {
        assertTrue(ValidationEngine.isValidQuantity(1))
        assertTrue(ValidationEngine.isValidQuantity(100))
        assertFalse(ValidationEngine.isValidQuantity(0))
        assertFalse(ValidationEngine.isValidQuantity(-1))
    }
}
