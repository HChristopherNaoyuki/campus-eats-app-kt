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
    // Business Logic Constants
    private const val TAX_RATE = 0.20 // 20%
    private const val STUDENT_DISCOUNT_RATE = 0.025 // 2.5%
    private const val ROUNDING_INCREMENT = 5.0 // Round up to nearest R5

    // Service Fee Tiers
    private const val TIER_LOW_THRESHOLD = 500.0
    private const val TIER_MID_THRESHOLD = 1000.0
    private const val TIER_LOW_FEE_RATE = 0.10 // 10%
    private const val TIER_MID_FEE_RATE = 0.065 // 6.5%
    private const val TIER_HIGH_FEE_RATE = 0.0 // 0%

    /**
     * Computes the final cost breakdown for an order subtotal.
     *
     * Tiered Service Fee Logic:
     * - Under R500: 10%
     * - R500 to R1000: 6.5%
     * - Above R1000: Free
     *
     * @param subtotal The sum of prices for all items in the cart.
     * @param role The role of the user, used to determine discount eligibility.
     * @return A CheckoutSummary object containing the detailed financial breakdown.
     */
    fun calculateSummary(subtotal: Double, role: UserRole): CheckoutSummary
    {
        // 1. Compute standard tax (20% of subtotal)
        val tax = subtotal * TAX_RATE

        // 2. Compute tiered service fee based on order value
        val serviceFee = when
        {
            subtotal < TIER_LOW_THRESHOLD -> subtotal * TIER_LOW_FEE_RATE
            subtotal <= TIER_MID_THRESHOLD -> subtotal * TIER_MID_FEE_RATE
            else -> subtotal * TIER_HIGH_FEE_RATE
        }

        // 3. Apply student-exclusive discount (2.5% of subtotal)
        val studentDiscount = if (role == UserRole.STUDENT)
        {
            subtotal * STUDENT_DISCOUNT_RATE
        }
        else
        {
            0.0
        }

        // 4. Calculate initial total before rounding
        var total = subtotal + tax + serviceFee - studentDiscount

        // 5. Perform mandatory financial rounding for cash handling optimization (Round up to next R5)
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
