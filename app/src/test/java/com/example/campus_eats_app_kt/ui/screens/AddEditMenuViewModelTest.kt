package com.example.campus_eats_app_kt.ui.screens

import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import io.mockk.coVerify
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
 * AddEditMenuViewModelTest verifies the logic for creating and updating menu items.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddEditMenuViewModelTest
{
    private lateinit var repository: MenuRepository
    private lateinit var viewModel: AddEditMenuViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp()
    {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test pre-filling data for editing an existing item
     */
    @Test
    fun init_withItemId_loadsItemData() = runTest {
        // Given
        val vendorId = "V1"
        val itemId = 101L
        val item = MenuItemEntity(itemId, vendorId, "Pizza", "Cheesy", 80.0, 5, "Main")
        every { repository.getMenuItemsByVendor(vendorId) } returns flowOf(listOf(item))

        // When
        viewModel = AddEditMenuViewModel(repository, vendorId, itemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Pizza", viewModel.name)
        assertEquals("80.0", viewModel.price)
    }

    /**
     * Requirement: Test saving a new item
     */
    @Test
    fun saveItem_new_callsRepositoryAdd() = runTest {
        // Given
        viewModel = AddEditMenuViewModel(repository, "V1", null)
        viewModel.name = "Soda"
        viewModel.price = "15.0"
        viewModel.stock = "100"
        viewModel.category = "Drinks"

        // When
        viewModel.saveItem { }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.addMenuItem(match {
                it.name == "Soda" && it.price == 15.0 && it.stock == 100
            })
        }
    }

    /**
     * Requirement: Test saving an existing item update
     */
    @Test
    fun saveItem_existing_callsRepositoryUpdate() = runTest {
        // Given
        viewModel = AddEditMenuViewModel(repository, "V1", 101L)
        viewModel.name = "Burger"
        viewModel.price = "65.0"
        viewModel.stock = "20"
        viewModel.category = "Food"

        // When
        viewModel.saveItem { }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.updateMenuItem(match {
                it.itemId == 101L && it.name == "Burger"
            })
        }
    }
}
