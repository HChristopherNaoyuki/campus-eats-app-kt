package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlin.math.ceil

data class CheckoutSummary(
    val subtotal: Double,
    val tax: Double,
    val serviceFee: Double,
    val studentDiscount: Double,
    val total: Double
)

object CheckoutEngine {
    fun calculateSummary(subtotal: Double, role: UserRole): CheckoutSummary {
        val tax = subtotal * 0.20
        
        val serviceFee = when {
            subtotal < 500 -> subtotal * 0.10
            subtotal < 1000 -> subtotal * 0.065
            else -> 0.0
        }
        
        val studentDiscount = if (role == UserRole.STUDENT) subtotal * 0.025 else 0.0
        
        var total = subtotal + tax + serviceFee - studentDiscount
        
        // Round up to the next R5 increment
        total = ceil(total / 5.0) * 5.0
        
        return CheckoutSummary(
            subtotal = subtotal,
            tax = tax,
            serviceFee = serviceFee,
            studentDiscount = studentDiscount,
            total = total
        )
    }
}
