package com.example.campus_eats_app_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CampusEatsDatabase
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.ui.navigation.Route
import com.example.campus_eats_app_kt.ui.screens.AddEditMenuItemScreen
import com.example.campus_eats_app_kt.ui.screens.AddEditMenuViewModel
import com.example.campus_eats_app_kt.ui.screens.CartScreen
import com.example.campus_eats_app_kt.ui.screens.CartViewModel
import com.example.campus_eats_app_kt.ui.screens.CheckoutScreen
import com.example.campus_eats_app_kt.ui.screens.CheckoutViewModel
import com.example.campus_eats_app_kt.ui.screens.CustomerMenuBrowseScreen
import com.example.campus_eats_app_kt.ui.screens.CustomerVendorBrowseScreen
import com.example.campus_eats_app_kt.ui.screens.ForgotPasswordScreen
import com.example.campus_eats_app_kt.ui.screens.ForgotPasswordViewModel
import com.example.campus_eats_app_kt.ui.screens.LandingScreen
import com.example.campus_eats_app_kt.ui.screens.LoginScreen
import com.example.campus_eats_app_kt.ui.screens.LoginViewModel
import com.example.campus_eats_app_kt.ui.screens.MainScreen
import com.example.campus_eats_app_kt.ui.screens.MenuBrowseViewModel
import com.example.campus_eats_app_kt.ui.screens.RegistrationScreen
import com.example.campus_eats_app_kt.ui.screens.RegistrationViewModel
import com.example.campus_eats_app_kt.ui.screens.VendorBrowseViewModel
import com.example.campus_eats_app_kt.ui.screens.VendorMenuManagementScreen
import com.example.campus_eats_app_kt.ui.screens.VendorMenuViewModel
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
        val statsRepository =
            StatsRepository(database.userDao(), database.menuItemDao(), database.orderDao())
        val feedbackRepository = FeedbackRepository(database.feedbackDao())
        val couponRepository = CouponRepository(database.couponDao())

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
                                    backStack.add(Route.Main(userId, role)) 
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
                                    backStack.add(Route.Main(userId, role))
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
                        entry<Route.Main> { route ->
                            MainScreen(
                                userId = route.userId,
                                role = route.role,
                                authRepository = authRepository,
                                menuRepository = menuRepository,
                                cartRepository = cartRepository,
                                orderRepository = orderRepository,
                                adminRepository = adminRepository,
                                statsRepository = statsRepository,
                                feedbackRepository = feedbackRepository,
                                couponRepository = couponRepository,
                                onLogout = { 
                                    backStack.clear()
                                    backStack.add(Route.Landing) 
                                },
                                onNavigateToCheckout = { backStack.add(Route.Checkout(route.userId)) },
                                onNavigateToVendorMenu = { vendorId -> backStack.add(Route.VendorMenuManagement(vendorId)) },
                                onNavigateToAddMenuItem = { vendorId, itemId -> backStack.add(Route.AddEditMenuItem(vendorId, itemId)) },
                                onNavigateToCart = { backStack.add(Route.Cart(route.userId)) },
                                onNavigateToMenuBrowse = { userId, vendorId -> backStack.add(Route.CustomerMenuBrowse(userId, vendorId)) }
                            )
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
                                    backStack.add(Route.Main(route.userId, "STUDENT"))
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
