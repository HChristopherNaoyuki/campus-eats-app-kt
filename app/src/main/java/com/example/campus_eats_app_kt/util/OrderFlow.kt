package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.OrderStatus

/**
 * OrderFlow holds pure helpers for the order lifecycle so the vendor screen
 * and the student receipts share one tested definition.
 */
object OrderFlow
{
    /**
     * Returns the next lifecycle step for the vendor action button,
     * or null when the order is in a terminal state.
     */
    fun nextStatus(current: OrderStatus): OrderStatus?
    {
        return when (current)
        {
            OrderStatus.PENDING -> OrderStatus.ACCEPTED
            OrderStatus.ACCEPTED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> null
            OrderStatus.CANCELLED -> null
        }
    }

    /**
     * Returns the receipt label for the student. COMPLETED is treated as
     * picked up because the data model has no separate picked up field.
     */
    fun receiptLabel(status: OrderStatus): String
    {
        return when (status)
        {
            OrderStatus.COMPLETED -> "Picked up"
            OrderStatus.READY -> "Ready for pickup"
            OrderStatus.CANCELLED -> "Cancelled"
            else -> "In progress: ${status.name}"
        }
    }
}
