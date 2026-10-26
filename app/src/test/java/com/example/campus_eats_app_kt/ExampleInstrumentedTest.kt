package com.example.campus_eats_app_kt

import com.example.campus_eats_app_kt.data.FeedbackKeys
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.CheckoutEngine
import com.example.campus_eats_app_kt.util.IdGenerator
import com.example.campus_eats_app_kt.util.LanguageManager
import com.example.campus_eats_app_kt.util.OrderFlow
import com.example.campus_eats_app_kt.util.OrderStatusEngine
import com.example.campus_eats_app_kt.util.ValidationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ExampleInstrumentedTest provides genuine unit tests for application utilities,
 * financial engines, navigation lifecycle helpers, and domain entity boundaries.
 */
class ExampleInstrumentedTest
{
    @Test
    fun addition_isCorrect()
    {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun checkoutEngine_calculatesStudentDiscountAndFeeCorrectly()
    {
        val summary = CheckoutEngine.calculateSummary(200.0, UserRole.STUDENT)
        assertNotNull(summary)
        assertEquals(5.0, summary.studentDiscount.toDouble(), 0.01)
        assertEquals(20.0, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_roundsTotalUpToNextMultipleOfFive()
    {
        val summary = CheckoutEngine.calculateSummary(100.0, UserRole.STANDARD)
        assertEquals(0.0, summary.total.toDouble() % 5.0, 0.01)
    }

    @Test
    fun orderFlow_mapsNextStatusForVendorProgression()
    {
        assertEquals(OrderStatus.ACCEPTED, OrderFlow.nextStatus(OrderStatus.PENDING))
        assertEquals(OrderStatus.PREPARING, OrderFlow.nextStatus(OrderStatus.ACCEPTED))
        assertEquals(OrderStatus.READY, OrderFlow.nextStatus(OrderStatus.PREPARING))
        assertEquals(OrderStatus.COMPLETED, OrderFlow.nextStatus(OrderStatus.READY))
        assertNull(OrderFlow.nextStatus(OrderStatus.COMPLETED))
        assertNull(OrderFlow.nextStatus(OrderStatus.CANCELLED))
    }

    @Test
    fun orderFlow_formatsStudentReceiptStatusLabels()
    {
        assertEquals("Picked up", OrderFlow.receiptLabel(OrderStatus.COMPLETED))
        assertEquals("Ready for pickup", OrderFlow.receiptLabel(OrderStatus.READY))
        assertEquals("Cancelled", OrderFlow.receiptLabel(OrderStatus.CANCELLED))
        assertEquals("In progress: PREPARING", OrderFlow.receiptLabel(OrderStatus.PREPARING))
    }

    @Test
    fun idGenerator_createsValid16CharacterUserId()
    {
        val userId = IdGenerator.generateUserId()
        assertNotNull(userId)
        val cleanId = userId.replace("-", "")
        assertEquals(16, cleanId.length)
    }

    @Test
    fun idGenerator_createsOrderIdWithOrdPrefix()
    {
        val orderId = IdGenerator.generateOrderId()
        assertNotNull(orderId)
        assertTrue(orderId.startsWith("ORD-"))
    }

    @Test
    fun orderStatusEngine_enforcesValidLifecycleTransitions()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED))
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.COMPLETED, OrderStatus.PENDING))
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.ACCEPTED))
    }

    @Test
    fun validationEngine_validatesRecoveryPasswordFormat()
    {
        assertTrue(ValidationEngine.isValidRecoveryPassword("ValidPass1#"))
        assertFalse(ValidationEngine.isValidRecoveryPassword("short"))
        assertFalse(ValidationEngine.isValidRecoveryPassword("NoSpecial123"))
    }

    @Test
    fun languageManager_resolvesEnglishAndAfrikaansStrings()
    {
        assertEquals(LanguageManager.ENGLISH, LanguageManager.normalize("English"))
        assertEquals(LanguageManager.AFRIKAANS, LanguageManager.normalize("Afrikaans"))
        assertEquals(LanguageManager.ENGLISH, LanguageManager.normalize("unknown"))
    }

    @Test
    fun feedbackKeys_generatesDeterministicFirebaseKey()
    {
        val feedback = FeedbackEntity(
            feedbackId = 1L,
            userId = "U101",
            type = FeedbackType.COMPLAINT,
            subject = "Issue",
            message = "Details of complaint.",
            userName = "Alice",
            userEmail = "alice@example.com",
            status = FeedbackStatus.PENDING,
            createdAt = "2026-10-21T10:00:00Z",
            updatedAt = "2026-10-21T10:00:00Z",
        )

        val key = FeedbackKeys.keyFor(feedback)
        assertEquals("U101_2026_10_21T10_00_00Z", key)
    }
}
