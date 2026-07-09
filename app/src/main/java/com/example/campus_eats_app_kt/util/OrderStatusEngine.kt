package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.OrderStatus

/**
 * OrderStatusEngine manages the valid state transitions for an order.
 * It enforces business rules to ensure orders follow a logical lifecycle.
 */
object OrderStatusEngine
{
    /**
     * Determines if a transition from one status to another is valid.
     * 
     * Valid Lifecycles:
     * - PENDING -> ACCEPTED or CANCELLED
     * - ACCEPTED -> PREPARING or CANCELLED
     * - PREPARING -> READY or CANCELLED
     * - READY -> COMPLETED or CANCELLED
     * - COMPLETED/CANCELLED -> No further transitions
     * 
     * @param current The current status of the order.
     * @param target The requested new status.
     * @return True if the transition is allowed by business rules.
     */
    fun isValidTransition(current: OrderStatus, target: OrderStatus): Boolean
    {
        if (current == target) return true
        if (target == OrderStatus.CANCELLED)
        {
            // Orders can be cancelled at any point before completion
            return current != OrderStatus.COMPLETED && current != OrderStatus.CANCELLED
        }

        return when (current)
        {
            OrderStatus.PENDING -> target == OrderStatus.ACCEPTED
            OrderStatus.ACCEPTED -> target == OrderStatus.PREPARING
            OrderStatus.PREPARING -> target == OrderStatus.READY
            OrderStatus.READY -> target == OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> false // Terminal state
            OrderStatus.CANCELLED -> false // Terminal state
        }
    }
}
