package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.OrderStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * OrderStatusEngineTest verifies the lifecycle transitions of orders.
 */
class OrderStatusEngineTest
{
    /**
     * Requirement: Test Pending to Accepted transition
     */
    @Test
    fun isValidTransition_pendingToAccepted_isValid()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED))
    }

    /**
     * Requirement: Test Accepted to Preparing transition
     */
    @Test
    fun isValidTransition_acceptedToPreparing_isValid()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.PREPARING))
    }

    /**
     * Requirement: Test Preparing to Ready transition
     */
    @Test
    fun isValidTransition_preparingToReady_isValid()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PREPARING, OrderStatus.READY))
    }

    /**
     * Requirement: Test Ready to Completed transition
     */
    @Test
    fun isValidTransition_readyToCompleted_isValid()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.COMPLETED))
    }

    /**
     * Requirement: Test Cancellation at each status
     */
    @Test
    fun isValidTransition_cancelBeforeCompletion_isValid()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.CANCELLED))
        assertTrue(
            OrderStatusEngine.isValidTransition(
                OrderStatus.PREPARING,
                OrderStatus.CANCELLED
            )
        )
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.CANCELLED))
    }

    /**
     * Requirement: Test terminal states
     */
    @Test
    fun isValidTransition_fromTerminalStates_isInvalid()
    {
        assertFalse(
            OrderStatusEngine.isValidTransition(
                OrderStatus.COMPLETED,
                OrderStatus.ACCEPTED
            )
        )
        assertFalse(
            OrderStatusEngine.isValidTransition(
                OrderStatus.CANCELLED,
                OrderStatus.ACCEPTED
            )
        )
        assertFalse(
            OrderStatusEngine.isValidTransition(
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED
            )
        )
    }

    /**
     * Requirement: Test invalid status transitions
     */
    @Test
    fun isValidTransition_skippingSteps_isInvalid()
    {
        // Cannot jump from PENDING to READY
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.READY))
        // Cannot jump from ACCEPTED to COMPLETED
        assertFalse(
            OrderStatusEngine.isValidTransition(
                OrderStatus.ACCEPTED,
                OrderStatus.COMPLETED
            )
        )
    }
}
