package com.example.campus_eats_app_kt.util

import com.example.campus_eats_app_kt.data.entity.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * OrderFlowTest verifies vendor status progression and student receipt labels.
 */
class OrderFlowTest
{
    @Test
    fun nextStatus_pending_isAccepted()
    {
        assertEquals(OrderStatus.ACCEPTED, OrderFlow.nextStatus(OrderStatus.PENDING))
    }

    @Test
    fun nextStatus_accepted_isPreparing()
    {
        assertEquals(OrderStatus.PREPARING, OrderFlow.nextStatus(OrderStatus.ACCEPTED))
    }

    @Test
    fun nextStatus_preparing_isReady()
    {
        assertEquals(OrderStatus.READY, OrderFlow.nextStatus(OrderStatus.PREPARING))
    }

    @Test
    fun nextStatus_ready_isCompleted()
    {
        assertEquals(OrderStatus.COMPLETED, OrderFlow.nextStatus(OrderStatus.READY))
    }

    @Test
    fun nextStatus_completed_hasNoNextStep()
    {
        assertNull(OrderFlow.nextStatus(OrderStatus.COMPLETED))
    }

    @Test
    fun nextStatus_cancelled_hasNoNextStep()
    {
        assertNull(OrderFlow.nextStatus(OrderStatus.CANCELLED))
    }

    @Test
    fun receiptLabel_completed_isPickedUp()
    {
        assertEquals("Picked up", OrderFlow.receiptLabel(OrderStatus.COMPLETED))
    }

    @Test
    fun receiptLabel_ready_isReadyForPickup()
    {
        assertEquals("Ready for pickup", OrderFlow.receiptLabel(OrderStatus.READY))
    }

    @Test
    fun receiptLabel_cancelled_isCancelled()
    {
        assertEquals("Cancelled", OrderFlow.receiptLabel(OrderStatus.CANCELLED))
    }

    @Test
    fun receiptLabel_preparing_showsInProgress()
    {
        assertEquals(
            "In progress: PREPARING",
            OrderFlow.receiptLabel(OrderStatus.PREPARING),
        )
    }
}
