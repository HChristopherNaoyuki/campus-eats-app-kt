package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlin.math.ceil

/**
 * CheckoutSummary encapsulates the financial breakdown of an order.
 */
data class CheckoutSummary(
    val subtotal: Double,
    val tax: Double,
    val serviceFee: Double,
    val studentDiscount: Double,
    val total: Double
)

/**
 * CheckoutEngine contains the core business logic for calculating order totals.
 * It applies tax, service fees, and role-based discounts according to university policy.
 */
object CheckoutEngine
{
    private const val TAX_RATE = 0.20 // 20%
    private const val STUDENT_DISCOUNT_RATE = 0.025 // 2.5%
    private const val ROUNDING_INCREMENT = 5.0 // Round up to nearest R5

    /**
     * Computes the final cost breakdown for an order subtotal.
     */
    fun calculateSummary(subtotal: Double, role: UserRole): CheckoutSummary
    {
        // Compute standard tax
        val tax = subtotal * TAX_RATE

        // Compute tiered service fee based on order value
        val serviceFee = when
        {
            subtotal < 500 -> subtotal * 0.10 // 10% for small orders
            subtotal < 1000 -> subtotal * 0.065 // 6.5% for medium orders
            else -> 0.0 // Waived for large orders
        }

        // Apply student-exclusive discount
        val studentDiscount =
            if (role == UserRole.STUDENT) subtotal * STUDENT_DISCOUNT_RATE else 0.0

        // Calculate initial total
        var total = subtotal + tax + serviceFee - studentDiscount

        // Perform mandatory financial rounding for cash handling optimization
        total = ceil(total / ROUNDING_INCREMENT) * ROUNDING_INCREMENT

        return CheckoutSummary(
            subtotal = subtotal,
            tax = tax,
            serviceFee = serviceFee,
            studentDiscount = studentDiscount,
            total = total
        )
    }
}
