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
    suspend fun createCoupon(code: String, discountPercent: Double)
    {
        couponDao.insertCoupon(CouponEntity(code, discountPercent))
    }

    /**
     * Permanently removes a coupon from the system.
     */
    suspend fun deleteCoupon(coupon: CouponEntity)
    {
        couponDao.deleteCoupon(coupon)
    }

    /**
     * Validates a coupon code and returns the entity if it is active.
     */
    suspend fun validateCoupon(code: String): CouponEntity?
    {
        return try
        {
            getAllCoupons().first().find { it.code == code && it.isActive }
        }
        catch (e: Exception)
        {
            null
        }
    }
}
