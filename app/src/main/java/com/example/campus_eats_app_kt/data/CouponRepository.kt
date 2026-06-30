package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import kotlinx.coroutines.flow.Flow

class CouponRepository(private val couponDao: CouponDao)
{
    fun getAllCoupons(): Flow<List<CouponEntity>> = couponDao.getAllCoupons()

    suspend fun createCoupon(code: String, discountPercent: Double)
    {
        couponDao.insertCoupon(CouponEntity(code, discountPercent))
    }

    suspend fun deleteCoupon(coupon: CouponEntity)
    {
        couponDao.deleteCoupon(coupon)
    }
}
