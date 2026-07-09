package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * CouponRepositoryTest verifies creation and validation of promotional codes.
 */
class CouponRepositoryTest
{
    private lateinit var couponDao: CouponDao
    private lateinit var repository: CouponRepository

    @Before
    fun setUp()
    {
        couponDao = mockk(relaxed = true)
        repository = CouponRepository(couponDao)
    }

    /**
     * Requirement: Test successful coupon validation
     */
    @Test
    fun validateCoupon_activeCode_returnsCoupon() = runTest {
        // Given
        val coupons = listOf(CouponEntity("SAVE10", 10.0, true))
        every { couponDao.getAllCoupons() } returns flowOf(coupons)

        // When
        val result = repository.validateCoupon("SAVE10")

        // Then
        assertEquals(10.0, result?.discountPercent ?: 0.0, 0.001)
    }

    /**
     * Requirement: Test failed coupon validation (Inactive)
     */
    @Test
    fun validateCoupon_inactiveCode_returnsNull() = runTest {
        // Given
        val coupons = listOf(CouponEntity("EXPIRED", 50.0, false))
        every { couponDao.getAllCoupons() } returns flowOf(coupons)

        // When
        val result = repository.validateCoupon("EXPIRED")

        // Then
        assertNull(result)
    }

    /**
     * Requirement: Test coupon creation
     */
    @Test
    fun createCoupon_callsDao() = runTest {
        repository.createCoupon("NEW", 20.0)
        coVerify { couponDao.insertCoupon(match { it.code == "NEW" && it.discountPercent == 20.0 }) }
    }
}
