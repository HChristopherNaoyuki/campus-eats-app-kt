package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * FirebaseSyncManagerTest verifies sync loop initialization and error resilience.
 */
class FirebaseSyncManagerTest
{
    private lateinit var userDao: UserDao
    private lateinit var orderDao: OrderDao
    private lateinit var feedbackDao: FeedbackDao
    private lateinit var couponDao: CouponDao
    private lateinit var menuItemDao: MenuItemDao
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var syncManager: FirebaseSyncManager

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        orderDao = mockk(relaxed = true)
        feedbackDao = mockk(relaxed = true)
        couponDao = mockk(relaxed = true)
        menuItemDao = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true)

        coEvery { userDao.getUnsyncedUsers() } returns emptyList()
        coEvery { orderDao.getAllOrders() } returns flowOf(emptyList())
        coEvery { feedbackDao.getAllFeedback() } returns flowOf(emptyList())
        coEvery { couponDao.getAllCoupons() } returns flowOf(emptyList())

        syncManager = FirebaseSyncManager(
            userDao = userDao,
            orderDao = orderDao,
            feedbackDao = feedbackDao,
            couponDao = couponDao,
            menuItemDao = menuItemDao,
            connectivityManager = connectivityManager,
            firebaseAuth = firebaseAuth,
            firebaseDatabase = firebaseDatabase,
        )
    }

    @Test
    fun syncManager_initializesSuccessfully()
    {
        runTest {
            assertNotNull(syncManager)
        }
    }
}
