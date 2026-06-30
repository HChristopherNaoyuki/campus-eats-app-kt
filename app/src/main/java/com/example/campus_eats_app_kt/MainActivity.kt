package com.example.campus_eats_app_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.CampusEatsDatabase
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.ui.navigation.Route
import com.example.campus_eats_app_kt.ui.screens.*
import com.example.campus_eats_app_kt.ui.theme.CampusEatsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = CampusEatsDatabase.getDatabase(this)
        val authRepository = AuthRepository(database.userDao())
        val menuRepository = MenuRepository(database.menuItemDao(), database.userDao())
        val cartRepository = CartRepository(database.cartDao())
        val orderRepository = OrderRepository(database.orderDao(), database.cartDao())
        val adminRepository = AdminRepository(database.userDao())

        setContent {
            CampusEatsAppTheme {
                val backStack = rememberNavBackStack(Route.Landing)
                
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    modifier = Modifier.fillMaxSize(),
                    entryProvider = entryProvider {
                        entry<Route.Landing> {
                            LandingScreen(
                                onLoginClick = { backStack.add(Route.Login) },
                                onRegisterClick = { backStack.add(Route.Register()) }
                            )
                        }
                        entry<Route.Login> {
                            val viewModel: LoginViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { LoginViewModel(authRepository) }
                                }
                            )
                            LoginScreen(
                                onLoginSuccess = { userId, role -> 
                                    backStack.clear()
                                    backStack.add(Route.Dashboard(userId, role)) 
                                },
                                onForgotPasswordClick = { backStack.add(Route.ForgotPassword) },
                                onBackClick = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.Register> {
                            val viewModel: RegistrationViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { RegistrationViewModel(authRepository) }
                                }
                            )
                            RegistrationScreen(
                                onRegistrationSuccess = { userId, role -> 
                                    backStack.clear()
                                    backStack.add(Route.Dashboard(userId, role))
                                },
                                onBackClick = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.ForgotPassword> {
                            val viewModel: ForgotPasswordViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { ForgotPasswordViewModel(authRepository) }
                                }
                            )
                            ForgotPasswordScreen(
                                onResetSuccess = { backStack.removeLastOrNull() },
                                onBackClick = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.Dashboard> { route ->
                            when (route.role) {
                                "VENDOR" -> {
                                    val viewModel: VendorDashboardViewModel = viewModel(
                                        factory = viewModelFactory {
                                            initializer { VendorDashboardViewModel(orderRepository, route.userId) }
                                        }
                                    )
                                    VendorDashboardScreen(
                                        onManageMenu = { backStack.add(Route.VendorMenuManagement(route.userId)) },
                                        onLogout = { 
                                            backStack.clear()
                                            backStack.add(Route.Landing) 
                                        },
                                        viewModel = viewModel
                                    )
                                }
                                "ADMIN" -> {
                                    val viewModel: AdminDashboardViewModel = viewModel(
                                        factory = viewModelFactory {
                                            initializer { AdminDashboardViewModel(adminRepository) }
                                        }
                                    )
                                    AdminDashboardScreen(
                                        onLogout = { 
                                            backStack.clear()
                                            backStack.add(Route.Landing) 
                                        },
                                        viewModel = viewModel
                                    )
                                }
                                else -> {
                                    val viewModel: StudentDashboardViewModel = viewModel(
                                        factory = viewModelFactory {
                                            initializer { StudentDashboardViewModel(orderRepository, route.userId) }
                                        }
                                    )
                                    StudentDashboardScreen(
                                        onBrowseVendors = { backStack.add(Route.CustomerVendorBrowse(route.userId)) },
                                        onLogout = { 
                                            backStack.clear()
                                            backStack.add(Route.Landing) 
                                        },
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                        entry<Route.VendorMenuManagement> { route ->
                            val viewModel: VendorMenuViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { VendorMenuViewModel(menuRepository, route.vendorId) }
                                }
                            )
                            VendorMenuManagementScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                onAddItemClick = { backStack.add(Route.AddEditMenuItem(route.vendorId)) },
                                onEditItemClick = { itemId -> backStack.add(Route.AddEditMenuItem(route.vendorId, itemId)) },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.CustomerVendorBrowse> { route ->
                            val viewModel: VendorBrowseViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { VendorBrowseViewModel(menuRepository) }
                                }
                            )
                            CustomerVendorBrowseScreen(
                                onVendorClick = { vendorId -> backStack.add(Route.CustomerMenuBrowse(route.userId, vendorId)) },
                                onCartClick = { backStack.add(Route.Cart(route.userId)) },
                                onLogout = { 
                                    backStack.clear()
                                    backStack.add(Route.Landing) 
                                },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.AddEditMenuItem> { route ->
                            val viewModel: AddEditMenuViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { AddEditMenuViewModel(menuRepository, route.vendorId, route.itemId) }
                                }
                            )
                            AddEditMenuItemScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.CustomerMenuBrowse> { route ->
                            val viewModel: MenuBrowseViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { MenuBrowseViewModel(menuRepository, cartRepository, route.userId, route.vendorId) }
                                }
                            )
                            CustomerMenuBrowseScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                onCartClick = { backStack.add(Route.Cart(route.userId)) },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.Cart> { route ->
                            val viewModel: CartViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { CartViewModel(cartRepository, route.userId) }
                                }
                            )
                            CartScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                onCheckoutClick = { backStack.add(Route.Checkout(route.userId)) },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.Checkout> { route ->
                            val viewModel: CheckoutViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { CheckoutViewModel(cartRepository, orderRepository, route.userId) }
                                }
                            )
                            CheckoutScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                onOrderPlaced = { 
                                    backStack.clear()
                                    // In real app, we might want to pass the user role here properly.
                                    // For now, let's just go back to Login or try to restore state.
                                    // Best is to go back to Dashboard.
                                    backStack.add(Route.Dashboard(route.userId, "STUDENT"))
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                )
            }
        }
    }
}
