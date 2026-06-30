package com.example.campus_eats_app_kt.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Landing : Route

    @Serializable
    data object Login : Route

    @Serializable
    data class Register(val role: String? = null) : Route

    @Serializable
    data object ForgotPassword : Route

    @Serializable
    data class Main(val userId: String, val role: String) : Route

    @Serializable
    data class VendorMenuManagement(val vendorId: String) : Route

    @Serializable
    data class AddEditMenuItem(val vendorId: String, val itemId: Long? = null) : Route

    @Serializable
    data class CustomerVendorBrowse(val userId: String) : Route

    @Serializable
    data class CustomerMenuBrowse(val userId: String, val vendorId: String) : Route

    @Serializable
    data class Cart(val userId: String) : Route

    @Serializable
    data class Checkout(val userId: String) : Route
}
