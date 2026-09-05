package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ConvertersTest ensures that type conversions for Room database remain consistent.
 * Specifically validates that enum names are correctly serialized for SQLite persistence.
 */
class ConvertersTest
{
    private val converters = Converters()

    @Test
    fun userRoleConversion_isCorrect()
    {
        val role = UserRole.ADMINISTRATOR
        val string = converters.fromUserRole(role)
        assertEquals("ADMINISTRATOR", string)
        assertEquals(role, converters.toUserRole(string))
    }

    @Test
    fun orderStatusConversion_isCorrect()
    {
        val status = OrderStatus.COMPLETED
        val string = converters.fromOrderStatus(status)
        assertEquals("COMPLETED", string)
        assertEquals(status, converters.toOrderStatus(string))
    }

    @Test
    fun userStatusConversion_isCorrect()
    {
        val status = UserStatus.SUSPENDED
        val string = converters.fromUserStatus(status)
        assertEquals("SUSPENDED", string)
        assertEquals(status, converters.toUserStatus(string))
    }

    @Test
    fun shopStatusConversion_isCorrect()
    {
        val status = ShopStatus.CLOSED
        val string = converters.fromShopStatus(status)
        assertEquals("CLOSED", string)
        assertEquals(status, converters.toShopStatus(string))
    }

    /**
     * Requirement: Feedback types must be lowercase for Firebase compatibility.
     * Ensures the converter preserves the lowercase strings in DB even if Kotlin uses uppercase Enums.
     */
    @Test
    fun feedbackTypeConversion_isCorrect()
    {
        // Enum values are mapped to lowercase (complaint, compliment) in SQLite to align with Firebase rules.
        val type = FeedbackType.COMPLAINT
        val string = converters.fromFeedbackType(type)
        assertEquals("complaint", string)
        assertEquals(type, converters.toFeedbackType(string))
    }

    @Test
    fun paymentMethodConversion_isCorrect()
    {
        val method = PaymentMethod.CAMPUS_WALLET
        val string = converters.fromPaymentMethod(method)
        assertEquals("CAMPUS_WALLET", string)
        assertEquals(method, converters.toPaymentMethod(string))
    }
}
