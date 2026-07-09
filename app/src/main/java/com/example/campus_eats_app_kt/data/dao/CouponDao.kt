package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import kotlinx.coroutines.flow.Flow

/**
 * CouponDao provides access to discount codes.
 */
@Dao
interface CouponDao
{
    /**
     * Persists a promotional coupon.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    /**
     * Lists all existing coupons.
     */
    @Query("SELECT * FROM coupons")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    /**
     * Removes a coupon from the system.
     */
    @Delete
    suspend fun deleteCoupon(coupon: CouponEntity)
}
