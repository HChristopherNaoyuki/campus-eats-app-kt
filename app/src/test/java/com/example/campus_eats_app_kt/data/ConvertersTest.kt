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
 */
class ConvertersTest
{
    private val converters = Converters()

    @Test
    fun userRoleConversion_isCorrect()
    {
        val role = UserRole.ADMIN
        val string = converters.fromUserRole(role)
        assertEquals("ADMIN", string)
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

    @Test
    fun feedbackTypeConversion_isCorrect()
    {
        val type = FeedbackType.COMPLAINT
        val string = converters.fromFeedbackType(type)
        assertEquals("COMPLAINT", string)
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
