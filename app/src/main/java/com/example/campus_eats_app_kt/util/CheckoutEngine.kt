package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.UserRole
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * CheckoutSummary encapsulates the financial breakdown of an order.
 */
data class CheckoutSummary(
    val subtotal: BigDecimal,
    val tax: BigDecimal,
    val serviceFee: BigDecimal,
    val studentDiscount: BigDecimal,
    val total: BigDecimal,
)

/**
 * CheckoutEngine contains the core business logic for calculating order totals.
 * It applies tax, service fees, and role-based discounts according to university policy.
 */
object CheckoutEngine
{
    // Business Logic Constants
    private val TAX_RATE = BigDecimal("0.20")
    private val STUDENT_DISCOUNT_RATE = BigDecimal("0.025")
    private val ROUNDING_INCREMENT = BigDecimal("5.0")

    // Service Fee Tiers
    private val TIER_LOW_THRESHOLD = BigDecimal("500.0")
    private val TIER_MID_THRESHOLD = BigDecimal("1000.0")
    private val TIER_LOW_FEE_RATE = BigDecimal("0.10")
    private val TIER_MID_FEE_RATE = BigDecimal("0.065")
    private val TIER_HIGH_FEE_RATE = BigDecimal("0.0")

    /**
     * Computes the final cost breakdown for an order subtotal.
     *
     * Tiered Service Fee Logic:
     * - Under R500: 10%
     * - R500 to R1000: 6.5%
     * - Above R1000: Free
     *
     * @param subtotalDouble The sum of prices for all items in the cart.
     * @param role The role of the user, used to determine discount eligibility.
     * @return A CheckoutSummary object containing the detailed financial breakdown.
     */
    fun calculateSummary(subtotalDouble: Double, role: UserRole): CheckoutSummary
    {
        val subtotal = BigDecimal(subtotalDouble.toString()).setScale(2, RoundingMode.HALF_UP)

        // 1. Compute standard tax (20% of subtotal)
        val tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP)

        // 2. Compute tiered service fee based on order value
        val serviceFee = when
        {
            subtotal < TIER_LOW_THRESHOLD -> subtotal.multiply(TIER_LOW_FEE_RATE)
            subtotal <= TIER_MID_THRESHOLD -> subtotal.multiply(TIER_MID_FEE_RATE)
            else -> subtotal.multiply(TIER_HIGH_FEE_RATE)
        }.setScale(2, RoundingMode.HALF_UP)

        // 3. Apply student-exclusive discount (2.5% of subtotal)
        val studentDiscount = if (role == UserRole.STUDENT)
        {
            subtotal.multiply(STUDENT_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
        }
        else
        {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        // 4. Calculate initial total before rounding
        var total = subtotal.add(tax).add(serviceFee).subtract(studentDiscount)

        // 5. Perform mandatory financial rounding for cash handling optimization (Round up to next R5)
        // Logic: ceil(total / 5) * 5
        val divided = total.divide(ROUNDING_INCREMENT, 0, RoundingMode.CEILING)
        total = divided.multiply(ROUNDING_INCREMENT).setScale(2, RoundingMode.HALF_UP)

        return CheckoutSummary(
            subtotal = subtotal,
            tax = tax,
            serviceFee = serviceFee,
            studentDiscount = studentDiscount,
            total = total
        )
    }
}
