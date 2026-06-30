package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Query("SELECT * FROM coupons")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Delete
    suspend fun deleteCoupon(coupon: CouponEntity)
}
