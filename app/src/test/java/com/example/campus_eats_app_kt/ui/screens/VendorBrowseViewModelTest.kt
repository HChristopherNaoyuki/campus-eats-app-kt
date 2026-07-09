package com.example.campus_eats_app_kt.ui.screens

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * VendorBrowseViewModelTest verifies the vendor listing logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VendorBrowseViewModelTest
{
    private lateinit var repository: MenuRepository
    private lateinit var viewModel: VendorBrowseViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp()
    {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test vendor list loading
     */
    @Test
    fun vendors_loadsFromRepository() = runTest {
        // Given
        val vendors = listOf(
            UserEntity(
                "V1",
                "Vendor 1",
                "v1",
                "v1@t.com",
                "p",
                UserRole.VENDOR,
                shopName = "Shop 1"
            )
        )
        every { repository.getAllVendors() } returns flowOf(vendors)

        // When
        viewModel = VendorBrowseViewModel(repository)

        // Then
        viewModel.vendors.test {
            assertEquals(vendors, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
