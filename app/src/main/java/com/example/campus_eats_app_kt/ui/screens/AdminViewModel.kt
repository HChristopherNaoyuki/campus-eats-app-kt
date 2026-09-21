package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * AdminViewModel orchestrates administrative business logic.
 * Hardened in Batch 2 to remove dead code and ensure claim verification.
 */
class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository,
    orderRepository: OrderRepository,
    private val couponRepository: CouponRepository,
    private val feedbackRepository: FeedbackRepository,
) : ViewModel()
{
    private val _isAdmin = MutableStateFlow(value = false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    init
    {
        checkAdminStatus()
    }

    fun checkAdminStatus()
    {
        viewModelScope.launch()
        {
            _isAdmin.value = authRepository.isAdmin()
        }
    }

    val users: StateFlow<List<UserEntity>> = adminRepository.getAllUsers()
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
            if (authRepository.isAdmin())
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
    }

    /**
     * Generates a new discount coupon or issues a targeted credit.
     */
    fun generateCoupon(
        code: String,
        discount: Double,
        expiryDate: Long,
        assignedUserId: String? = null,
    )
    {
        viewModelScope.launch()
        {
            if (authRepository.isAdmin())
            {
                couponRepository.createCoupon(code, discount, expiryDate, assignedUserId)
            }
        }
    }

    /**
     * Retrieves feedback filtered by type.
     */
    fun getFeedbackByType(type: FeedbackType) =
        feedbackRepository.getAllFeedback().map()
        { list -> 
            list.filter { it.type == type } 
        }
}
