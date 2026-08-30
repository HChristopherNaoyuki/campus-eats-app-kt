package com.example.campus_eats_app_kt.data

import androidx.room.TypeConverter
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod

/**
 * Converters handles the transformation of complex types (Enums) into 
 * persistent formats supported by SQLite (Strings).
 */
class Converters
{
    @TypeConverter
    fun fromUserRole(role: UserRole): String
    {
        return role.name
    }

    @TypeConverter
    fun toUserRole(role: String): UserRole
    {
        return UserRole.valueOf(role)
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String
    {
        return status.name
    }

    @TypeConverter
    fun toOrderStatus(status: String): OrderStatus
    {
        return OrderStatus.valueOf(status)
    }

    @TypeConverter
    fun fromUserStatus(status: UserStatus): String
    {
        return status.name
    }

    @TypeConverter
    fun toUserStatus(status: String): UserStatus
    {
        return UserStatus.valueOf(status)
    }

    @TypeConverter
    fun fromShopStatus(status: ShopStatus?): String?
    {
        return status?.name
    }

    @TypeConverter
    fun toShopStatus(status: String?): ShopStatus?
    {
        return status?.let { ShopStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromFeedbackType(type: FeedbackType): String
    {
        return type.name
    }

    @TypeConverter
    fun toFeedbackType(type: String): FeedbackType
    {
        return FeedbackType.valueOf(type)
    }

    @TypeConverter
    fun fromFeedbackStatus(status: FeedbackStatus): String
    {
        return status.name
    }

    @TypeConverter
    fun toFeedbackStatus(status: String): FeedbackStatus
    {
        return FeedbackStatus.valueOf(status)
    }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String
    {
        return method.name
    }

    @TypeConverter
    fun toPaymentMethod(method: String): PaymentMethod
    {
        return PaymentMethod.valueOf(method)
    }
}
