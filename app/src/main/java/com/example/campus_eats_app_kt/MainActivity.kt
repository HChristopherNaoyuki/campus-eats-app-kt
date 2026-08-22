package com.example.campus_eats_app_kt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.example.campus_eats_app_kt.data.DebitCardRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.FirebaseDatabaseProvider
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.data.network.RetrofitClient
import com.example.campus_eats_app_kt.ui.components.NetworkStatusBanner
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
import com.example.campus_eats_app_kt.ui.screens.AdminViewModel
import com.example.campus_eats_app_kt.ui.screens.MainScreen
import com.example.campus_eats_app_kt.ui.screens.MenuBrowseViewModel
import com.example.campus_eats_app_kt.ui.screens.OrderConfirmationScreen
import com.example.campus_eats_app_kt.ui.screens.RegistrationScreen
import com.example.campus_eats_app_kt.ui.screens.RegistrationViewModel
import com.example.campus_eats_app_kt.ui.screens.VendorBrowseViewModel
import com.example.campus_eats_app_kt.ui.screens.VendorMenuManagementScreen
import com.example.campus_eats_app_kt.ui.screens.VendorMenuViewModel
import com.example.campus_eats_app_kt.ui.theme.CampusEatsAppTheme
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.example.campus_eats_app_kt.util.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseAuth

/**
 * MainActivity serves as the entry point for the Campus Eats application.
 * It initializes dependencies and sets up the navigation structure using Compose Navigation.
 */
class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Connectivity Infrastructure
        val connectivityManager = NetworkConnectivityManager(applicationContext)
        val connectivityObserver = NetworkConnectivityObserver(applicationContext)

        // Dependency Initialization (Simplified DI pattern)
        val database = CampusEatsDatabase.getDatabase(this)
        val apiService = RetrofitClient.instance
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabaseProvider.instance

        // Diagnostic Log: Verify Firebase Configuration
        Log.i("FirebaseInit", "Project ID: ${firebaseAuth.app.options.projectId}")
        Log.i("FirebaseInit", "Application ID: ${firebaseAuth.app.options.applicationId}")
        Log.i(
            "FirebaseInit",
            "Database URL: ${firebaseDatabase.reference}",
        )

        val authRepository = AuthRepository(
            userDao = database.userDao(),
            apiService = apiService,
            connectivityManager = connectivityManager,
            firebaseAuth = firebaseAuth,
            firebaseDatabase = firebaseDatabase
        )
        val menuRepository = MenuRepository(
            menuItemDao = database.menuItemDao(),
            userDao = database.userDao(),
            apiService = apiService,
            connectivityManager = connectivityManager
        )
        val cartRepository = CartRepository(database.cartDao())
        val orderRepository = OrderRepository(
            orderDao = database.orderDao(),
            cartDao = database.cartDao(),
            userDao = database.userDao(),
            apiService = apiService,
            connectivityManager = connectivityManager
        )
        val adminRepository = AdminRepository(
            userDao = database.userDao(),
            connectivityManager = connectivityManager,
            firebaseDatabase = firebaseDatabase
        )
        val statsRepository =
            StatsRepository(database.userDao(), database.menuItemDao(), database.orderDao())
        val feedbackRepository = FeedbackRepository(
            feedbackDao = database.feedbackDao(),
            connectivityManager = connectivityManager,
            firebaseDatabase = firebaseDatabase
        )
        val couponRepository = CouponRepository(database.couponDao())
        val debitCardRepository = DebitCardRepository(database.debitCardDao())

        setContent {
            val networkStatus by connectivityObserver.observe()
                .collectAsState(initial = NetworkConnectivityObserver.Status.Available)

            CampusEatsAppTheme {
                val backStack = rememberNavBackStack(Route.Splash)

                Column(modifier = Modifier.fillMaxSize()) {
                    NetworkStatusBanner(status = networkStatus)

                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        modifier = Modifier.weight(1f),
                        entryProvider = entryProvider {
                        // Splash Screen / Auth Initializer
                        entry<Route.Splash> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                            LaunchedEffect(Unit) {
                                if (authRepository.isUserAuthenticated())
                                {
                                    val email = authRepository.getCurrentUserEmail()
                                    if (email != null)
                                    {
                                        val user = authRepository.getUserByEmail(email)
                                        if (user != null)
                                        {
                                            backStack.add(Route.Main(user.userId, user.role.name))
                                            backStack.removeAt(0) // Remove Splash
                                            return@LaunchedEffect
                                        }
                                    }
                                }
                                backStack.add(Route.Landing)
                                backStack.removeAt(0) // Remove Splash
                            }
                        }

                        // Landing Screen Entry
                        entry<Route.Landing> {
                            LandingScreen(
                                onLoginClick = { backStack.add(Route.Login) },
                                onRegisterClick = { backStack.add(Route.Register()) },
                                onForgotPasswordClick = { backStack.add(Route.ForgotPassword) }
                            )
                        }

                        // Login Screen Entry
                        entry<Route.Login> {
                            val viewModel: LoginViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { LoginViewModel(authRepository) }
                                }
                            )
                            LoginScreen(
                                onLoginSuccess = { userId, role ->
                                    // Atomic backstack update to prevent navigation glitches
                                    val nextRoute = Route.Main(userId, role)
                                    backStack.add(nextRoute)
                                    while (backStack.size > 1)
                                    {
                                        backStack.removeAt(0)
                                    }
                                },
                                onRegisterClick = { backStack.add(Route.Register()) },
                                onForgotPasswordClick = { backStack.add(Route.ForgotPassword) },
                                onBackClick = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }

                        // Registration Screen Entry
                        entry<Route.Register> {
                            val viewModel: RegistrationViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { RegistrationViewModel(authRepository) }
                                }
                            )
                            RegistrationScreen(
                                onRegistrationSuccess = { _, _ ->
                                    backStack.removeLastOrNull()
                                },
                                onBackClick = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }

                        // Forgot Password Screen Entry
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

                        // Main Role-Based Dashboard Entry
                        entry<Route.Main> { route ->
                            val adminViewModel: AdminViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        AdminViewModel(
                                            adminRepository,
                                            orderRepository,
                                            couponRepository,
                                            feedbackRepository
                                        )
                                    }
                                }
                            )
                            MainScreen(
                                userId = route.userId,
                                role = route.role,
                                authRepository = authRepository,
                                menuRepository = menuRepository,
                                cartRepository = cartRepository,
                                orderRepository = orderRepository,
                                adminViewModel = adminViewModel,
                                statsRepository = statsRepository,
                                feedbackRepository = feedbackRepository,
                                couponRepository = couponRepository,
                                debitCardRepository = debitCardRepository,
                                onLogout = {
                                    authRepository.logout()
                                    backStack.clear()
                                    backStack.add(Route.Landing)
                                },
                                onNavigateToCheckout = { backStack.add(Route.Checkout(route.userId)) },
                                onNavigateToVendorMenu = { vendorId -> backStack.add(Route.VendorMenuManagement(vendorId)) },
                                onNavigateToAddMenuItem = { vendorId, itemId -> backStack.add(Route.AddEditMenuItem(vendorId, itemId)) }
                            ) { userId, vendorId ->
                                backStack.add(
                                    Route.CustomerMenuBrowse(
                                        userId,
                                        vendorId
                                    )
                                )
                            }
                        }

                        // Vendor Menu Management Entry
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

                        // Customer Vendor Browsing Entry
                        entry<Route.CustomerVendorBrowse> { route ->
                            val viewModel: VendorBrowseViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { VendorBrowseViewModel(menuRepository) }
                                }
                            )
                            CustomerVendorBrowseScreen(
                                onVendorClick = { vendorId -> backStack.add(Route.CustomerMenuBrowse(route.userId, vendorId)) },
                                onCartClick = { backStack.add(Route.Cart(route.userId)) },
                                onReturnHome = { backStack.removeLastOrNull() },
                                onLogout = {
                                    backStack.clear()
                                    backStack.add(Route.Landing)
                                },
                                viewModel = viewModel
                            )
                        }

                        // Add/Edit Menu Item Entry
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

                        // Customer Menu Item Browsing Entry
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

                        // Cart Screen Entry
                        entry<Route.Cart> { route ->
                            val viewModel: CartViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        CartViewModel(
                                            cartRepository,
                                            authRepository,
                                            route.userId
                                        )
                                    }
                                }
                            )
                            CartScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                onCheckoutClick = { backStack.add(Route.Checkout(route.userId)) },
                                viewModel = viewModel
                            )
                        }

                        // Checkout Screen Entry
                        entry<Route.Checkout> { route ->
                            val viewModel: CheckoutViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        CheckoutViewModel(
                                            cartRepository,
                                            orderRepository,
                                            authRepository,
                                            route.userId
                                        )
                                    }
                                }
                            )
                            CheckoutScreen(
                                onBackClick = { backStack.removeLastOrNull() },
                                onOrderPlaced = { orderId ->
                                    val nextRoute = Route.OrderConfirmation(
                                        orderId,
                                        route.userId,
                                        "STUDENT"
                                    )
                                    backStack.add(nextRoute)
                                    while (backStack.size > 1)
                                    {
                                        backStack.removeAt(0)
                                    }
                                },
                                viewModel = viewModel
                            )
                        }

                        // Order Confirmation Entry
                        entry<Route.OrderConfirmation> { route ->
                            OrderConfirmationScreen(
                                orderId = route.orderId,
                                onTrackOrder = {
                                    backStack.clear()
                                    backStack.add(Route.Main(route.userId, route.role))
                                },
                                onReturnHome = {
                                    backStack.clear()
                                    backStack.add(Route.Main(route.userId, route.role))
                                }
                            )
                        }
                    }
                )
            }
        }
    }
    }
}
