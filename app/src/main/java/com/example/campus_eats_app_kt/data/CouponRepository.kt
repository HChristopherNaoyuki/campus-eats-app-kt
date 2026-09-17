package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * CouponRepository handles the creation and validation of promotional discount codes.
 */
class CouponRepository(private val couponDao: CouponDao)
{
    /**
     * Retrieves all available coupons.
     */
    fun getAllCoupons(): Flow<List<CouponEntity>> = couponDao.getAllCoupons()

    /**
     * Creates a new discount coupon.
     */
    suspend fun createCoupon(
        code: String,
        discountPercent: Double,
        expiryDate: Long = 0L,
        assignedUserId: String? = null,
    )
    {
        couponDao.insertCoupon(
            CouponEntity(
                code = code,
                discountPercent = discountPercent,
                expiryDate = expiryDate,
                assignedUserId = assignedUserId,
            ),
        )
    }

    /**
     * Permanently removes a coupon from the system.
     */
    @Suppress("unused")
    suspend fun deleteCoupon(coupon: CouponEntity)
    {
        couponDao.deleteCoupon(coupon)
    }

    /**
     * Validates a coupon code and returns the entity if it is active.
     */
    suspend fun validateCoupon(code: String, userId: String? = null): CouponEntity?
    {
        return try
        {
            val now = System.currentTimeMillis()
            getAllCoupons().first().find()
            { coupon ->
                val codeMatch = coupon.code == code
                val isActive = coupon.isActive
                val notExpired = (coupon.expiryDate == 0L) || (coupon.expiryDate >= now)
                val userMatch = (coupon.assignedUserId == null) || (coupon.assignedUserId == userId)
                codeMatch && isActive && notExpired && userMatch
            }
        }
        catch (_: Exception)
        {
            null
        }
    }
}
