package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * AdminViewModel orchestrates administrative business logic across user management, 
 * order oversight, and system feedback.
 */
class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val orderRepository: OrderRepository,
    private val couponRepository: CouponRepository,
    private val feedbackRepository: FeedbackRepository,
) : ViewModel()
{
    // State exposed to UI
    val users: StateFlow<List<UserEntity>> = adminRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendors: StateFlow<List<UserEntity>> = users
        .map { it.filter { user -> user.role == UserRole.VENDOR } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = orderRepository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Toggles the active status of a user.
     */
    fun toggleUserStatus(user: UserEntity)
    {
        viewModelScope.launch()
        {
            if (user.status == UserStatus.ACTIVE)
            {
                adminRepository.suspendUser(user.userId)
            }
            else
            {
                adminRepository.activateUser(user.userId)
            }
        }
    }

    /**
     * Deletes a user from the system.
     */
    fun deleteUser(user: UserEntity)
    {
        viewModelScope.launch()
        {
            adminRepository.deleteUser(user)
        }
    }

    /**
     * Updates the status of an order.
     */
    fun updateOrderStatus(order: OrderEntity, status: OrderStatus)
    {
        viewModelScope.launch()
        {
            orderRepository.updateOrderStatus(order, status)
        }
    }

    /**
     * Issues credits to a user's wallet.
     */
    fun issueCredits(userId: String, amount: Double)
    {
        viewModelScope.launch()
        {
            adminRepository.issueCredits(userId, amount)
        }
    }

    /**
     * Generates a new discount coupon.
     */
    fun generateCoupon(code: String, discount: Double)
    {
        viewModelScope.launch()
        {
            couponRepository.createCoupon(code, discount)
        }
    }

    /**
     * Retrieves feedback filtered by type.
     */
    fun getFeedbackByType(type: FeedbackType) =
        feedbackRepository.getAllFeedback().map { list -> list.filter { it.type == type } }
}
