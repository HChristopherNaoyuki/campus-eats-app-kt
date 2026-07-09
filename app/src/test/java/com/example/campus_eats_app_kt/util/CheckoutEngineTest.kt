package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * CheckoutEngineTest verifies the financial calculation logic for orders.
 * It covers tiered service fees, taxes, student discounts, and mandatory R5 rounding.
 */
class CheckoutEngineTest
{
    /**
     * Requirement: Test service fee for orders under R500 (10%)
     */
    @Test
    fun calculateSummary_under500_applies10PercentFee()
    {
        // Given: A subtotal of R200 and a Standard User
        val subtotal = 200.0
        val role = UserRole.STANDARD

        // When: Calculating the summary
        val result = CheckoutEngine.calculateSummary(subtotal, role)

        // Then:
        // Subtotal = 200.0
        // Tax (20%) = 40.0
        // Service Fee (10%) = 20.0
        // Discount (0%) = 0.0
        // Raw Total = 260.0
        // Rounding to R5 = 260.0
        assertEquals(200.0, result.subtotal, 0.001)
        assertEquals(40.0, result.tax, 0.001)
        assertEquals(20.0, result.serviceFee, 0.001)
        assertEquals(0.0, result.studentDiscount, 0.001)
        assertEquals(260.0, result.total, 0.001)
    }

    /**
     * Requirement: Test service fee for orders between R500 and R1000 (6.5%)
     */
    @Test
    fun calculateSummary_between500And1000_applies6Point5PercentFee()
    {
        // Given: A subtotal of R600 and a Standard User
        val subtotal = 600.0
        val role = UserRole.STANDARD

        // When: Calculating the summary
        val result = CheckoutEngine.calculateSummary(subtotal, role)

        // Then:
        // Subtotal = 600.0
        // Tax (20%) = 120.0
        // Service Fee (6.5%) = 39.0
        // Discount (0%) = 0.0
        // Raw Total = 759.0
        // Rounding to next R5 = 760.0
        assertEquals(600.0, result.subtotal, 0.001)
        assertEquals(120.0, result.tax, 0.001)
        assertEquals(39.0, result.serviceFee, 0.001)
        assertEquals(760.0, result.total, 0.001)
    }

    /**
     * Requirement: Test service fee for orders above R1000 (0%)
     */
    @Test
    fun calculateSummary_above1000_appliesNoFee()
    {
        // Given: A subtotal of R1200 and a Standard User
        val subtotal = 1200.0
        val role = UserRole.STANDARD

        // When: Calculating the summary
        val result = CheckoutEngine.calculateSummary(subtotal, role)

        // Then:
        // Subtotal = 1200.0
        // Tax (20%) = 240.0
        // Service Fee (0%) = 0.0
        // Raw Total = 1440.0
        // Rounding to R5 = 1440.0
        assertEquals(0.0, result.serviceFee, 0.001)
        assertEquals(1440.0, result.total, 0.001)
    }

    /**
     * Requirement: Test service fee at exact boundary values (R500, R1000)
     */
    @Test
    fun calculateSummary_boundaryValues_usesCorrectTiers()
    {
        // Boundary R500 (inclusive of mid tier)
        val res500 = CheckoutEngine.calculateSummary(500.0, UserRole.STANDARD)
        assertEquals(500.0 * 0.065, res500.serviceFee, 0.001)

        // Boundary R1000 (inclusive of mid tier)
        val res1000 = CheckoutEngine.calculateSummary(1000.0, UserRole.STANDARD)
        assertEquals(1000.0 * 0.065, res1000.serviceFee, 0.001)
    }

    /**
     * Requirement: Test student discount (2.5% of subtotal)
     */
    @Test
    fun calculateSummary_studentUser_appliesDiscount()
    {
        // Given: A subtotal of R400 and a Student User
        val subtotal = 400.0
        val role = UserRole.STUDENT

        // When: Calculating the summary
        val result = CheckoutEngine.calculateSummary(subtotal, role)

        // Then:
        // Subtotal = 400.0
        // Tax (20%) = 80.0
        // Service Fee (10%) = 40.0
        // Student Discount (2.5%) = 10.0
        // Raw Total = 400 + 80 + 40 - 10 = 510.0
        // Rounding to R5 = 510.0
        assertEquals(10.0, result.studentDiscount, 0.001)
        assertEquals(510.0, result.total, 0.001)
    }

    /**
     * Requirement: Test rounding to next R5
     */
    @Test
    fun calculateSummary_anyTotal_roundsToNextR5()
    {
        // Given: A subtotal that results in a messy total
        // Subtotal R102.50
        val subtotal = 102.50
        val role = UserRole.STANDARD

        // Calculation:
        // Tax = 20.50
        // Fee = 10.25
        // Total = 102.50 + 20.50 + 10.25 = 133.25
        // Expected Rounded Total = 135.0

        // When: Calculating summary
        val result = CheckoutEngine.calculateSummary(subtotal, role)

        // Then:
        assertEquals(135.0, result.total, 0.001)
    }
}
