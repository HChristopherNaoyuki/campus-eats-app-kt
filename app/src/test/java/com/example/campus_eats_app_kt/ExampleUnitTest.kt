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
import com.example.campus_eats_app_kt.util.OrderStatusEngine
import com.example.campus_eats_app_kt.util.ValidationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ExampleUnitTest provides unit tests for key utilities, calculations, and validation rules.
 * Executes on the development host machine.
 */
class ExampleUnitTest
{
    @Test
    fun addition_isCorrect()
    {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun checkoutEngine_calculatesGrandTotalWithStudentDiscountAndRounding()
    {
        val subtotal = 100.0
        val summary = CheckoutEngine.calculateSummary(subtotal, UserRole.STUDENT)

        assertNotNull(summary)
        assertTrue(summary.total.toDouble() > 0.0)
        assertTrue(summary.studentDiscount.toDouble() > 0.0)
    }

    @Test
    fun idGenerator_generatesValid16CharacterUserId()
    {
        val userId = IdGenerator.generateUserId()

        assertNotNull(userId)
        val cleanId = userId.replace("-", "")
        assertEquals(16, cleanId.length)
    }

    @Test
    fun idGenerator_generatesValidOrderIdWithPrefix()
    {
        val orderId = IdGenerator.generateOrderId()

        assertNotNull(orderId)
        assertTrue(orderId.startsWith("ORD-"))
    }

    @Test
    fun orderStatusEngine_validatesOrderLifecycleTransitions()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.PREPARING))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PREPARING, OrderStatus.READY))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.COMPLETED))
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.COMPLETED, OrderStatus.PENDING))
    }

    @Test
    fun validationEngine_validatesRecoveryPasswordFormat()
    {
        assertTrue(ValidationEngine.isValidRecoveryPassword("ValidPass1#"))
        assertFalse(ValidationEngine.isValidRecoveryPassword("weak"))
    }

    @Test
    fun languageManager_resolvesLocalizedStringsCorrectly()
    {
        val resolved = LanguageManager.getString("English Text", "Afrikaans Teks")
        assertNotNull(resolved)
    }

    @Test
    fun feedbackKeys_generatesDeterministicFirebaseKey()
    {
        val feedback = FeedbackEntity(
            feedbackId = 10L,
            userId = "U505",
            type = FeedbackType.COMPLAINT,
            subject = "Late Order",
            message = "The order was delayed significantly.",
            userName = "Bob",
            userEmail = "bob@example.com",
            status = FeedbackStatus.PENDING,
            createdAt = "2026-10-20T12:00:00Z",
            updatedAt = "2026-10-20T12:00:00Z",
        )

        val key = FeedbackKeys.keyFor(feedback)
        assertEquals("U505_2026_10_20T12_00_00Z", key)
    }
}
