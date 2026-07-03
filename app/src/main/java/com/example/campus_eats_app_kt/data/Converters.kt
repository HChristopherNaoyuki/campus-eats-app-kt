package com.example.campus_eats_app_kt.data

import androidx.room.TypeConverter
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(role: String): UserRole = UserRole.valueOf(role)

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(status: String): OrderStatus = OrderStatus.valueOf(status)

    @TypeConverter
    fun fromUserStatus(status: UserStatus): String = status.name

    @TypeConverter
    fun toUserStatus(status: String): UserStatus = UserStatus.valueOf(status)

    @TypeConverter
    fun fromShopStatus(status: ShopStatus?): String? = status?.name

    @TypeConverter
    fun toShopStatus(status: String?): ShopStatus? = status?.let { ShopStatus.valueOf(it) }

    @TypeConverter
    fun fromFeedbackType(type: com.example.campus_eats_app_kt.data.entity.FeedbackType): String =
        type.name

    @TypeConverter
    fun toFeedbackType(type: String): com.example.campus_eats_app_kt.data.entity.FeedbackType =
        com.example.campus_eats_app_kt.data.entity.FeedbackType.valueOf(type)

    @TypeConverter
    fun fromPaymentMethod(method: com.example.campus_eats_app_kt.data.entity.PaymentMethod): String =
        method.name

    @TypeConverter
    fun toPaymentMethod(method: String): com.example.campus_eats_app_kt.data.entity.PaymentMethod =
        com.example.campus_eats_app_kt.data.entity.PaymentMethod.valueOf(method)
}
