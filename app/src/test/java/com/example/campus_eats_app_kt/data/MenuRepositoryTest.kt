package com.example.campus_eats_app_kt.data

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * MenuRepositoryTest verifies the retrieval of menu items and vendor filtering.
 */
class MenuRepositoryTest
{
    private lateinit var menuItemDao: MenuItemDao
    private lateinit var userDao: UserDao
    private lateinit var repository: MenuRepository

    @Before
    fun setUp()
    {
        menuItemDao = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        repository = MenuRepository(menuItemDao, userDao)
    }

    /**
     * Requirement: Test vendor filtering logic
     */
    @Test
    fun getAllVendors_filtersOnlyVendors() = runTest {
        // Given
        val users = listOf(
            UserEntity("U1", "Student", "s1", "s@t.com", "p", UserRole.STUDENT),
            UserEntity("V1", "Vendor", "v1", "v@t.com", "p", UserRole.VENDOR)
        )
        every { userDao.getAllUsers() } returns flowOf(users)

        // When/Then
        repository.getAllVendors().test {
            val vendors = awaitItem()
            assertEquals(1, vendors.size)
            assertEquals("V1", vendors[0].userId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test adding menu item
     */
    @Test
    fun addMenuItem_callsDao() = runTest {
        val item = mockk<com.example.campus_eats_app_kt.data.entity.MenuItemEntity>()
        repository.addMenuItem(item)
        coVerify { menuItemDao.insertMenuItem(item) }
    }
}
