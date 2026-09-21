package com.example.campus_eats_app_kt.data

import androidx.room.withTransaction
import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * OrderRepositoryTest verifies order placement and status management.
 */
class OrderRepositoryTest
{
    private lateinit var database: CampusEatsDatabase
    private lateinit var orderDao: OrderDao
    private lateinit var cartDao: CartDao
    private lateinit var userDao: UserDao
    private lateinit var menuItemDao: MenuItemDao
    private lateinit var apiService: FakeRestaurantApiService
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var repository: OrderRepository

    @Before
    fun setUp()
    {
        database = mockk(relaxed = true)
        orderDao = mockk(relaxed = true)
        cartDao = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        menuItemDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction<String>(any()) } coAnswers {
            // Finding 13: Mocking withTransaction extension (args[1] is the lambda)
            val block = it.invocation.args[1] as suspend () -> String
            block()
        }

        repository = OrderRepository(
            database, 
            orderDao, 
            cartDao, 
            userDao, 
            menuItemDao, 
            apiService, 
            connectivityManager
        )
    }

    @Test
    fun placeOrder_persistsOrderAndClearsCart() = runTest {
        val userId = "USER-001"
        val vendorId = "VENDOR-001"
        coEvery { orderDao.insertOrder(any()) } returns Unit
        coEvery { userDao.debitWallet(any(), any()) } returns 1
        coEvery { menuItemDao.decrementStock(any(), any()) } returns 1

        repository.placeOrder(
            userId = userId,
            vendorId = vendorId,
            cartItems = emptyList(),
            totalAmount = 100.0,
            paymentMethod = PaymentMethod.DEBIT_CARD,
            pickupTime = "12:00",
        )

        coVerify { orderDao.insertOrder(any()) }
        coVerify { cartDao.clearCart(userId) }
    }

    @Test
    fun updateOrderStatus_updatesInDao() = runTest {
        val order = OrderEntity(
            orderId = "ORD-123",
            customerId = "C1",
            vendorId = "V1",
            itemsJson = "[]",
            totalAmount = 100.0,
            status = OrderStatus.PENDING,
            paymentMethod = PaymentMethod.DEBIT_CARD,
            pickupTime = "12:00",
        )
        coEvery { orderDao.updateOrder(any()) } returns Unit

        repository.updateOrderStatus(order, OrderStatus.ACCEPTED)

        coVerify {
            orderDao.updateOrder(
                match {
                    (it.orderId == "ORD-123") && (it.status == OrderStatus.ACCEPTED)
                },
            )
        }
    }
}
