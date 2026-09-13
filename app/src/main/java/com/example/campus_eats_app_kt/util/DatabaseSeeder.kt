package com.example.campus_eats_app_kt.util

import android.content.Context
import android.util.Log
import com.example.campus_eats_app_kt.data.CampusEatsDatabase
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * DatabaseSeeder handles prepopulating the local database with sample data.
 * It ensures that each table contains at least 10 valid records to comply with 
 * project specifications while keeping the system error-free.
 */
object DatabaseSeeder
{
    private const val TAG = "DatabaseSeeder"

    /**
     * Hashes/encrypts passwords using SHA-256 for secure storage.
     */
    fun encryptPassword(password: String): String
    {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + String.format("%02x", it) }
    }

    /**
     * Seeds the database asynchronously if any tables are lacking sufficient data.
     */
    suspend fun seed(context: Context) = withContext(Dispatchers.IO)
    {
        val db = CampusEatsDatabase.getDatabase(context)
        val userDao = db.userDao()
        val menuItemDao = db.menuItemDao()
        val orderDao = db.orderDao()
        val cartDao = db.cartDao()
        val feedbackDao = db.feedbackDao()
        val couponDao = db.couponDao()
        val debitCardDao = db.debitCardDao()

        // 1. Seed Users (Exactly the 10 users specified in user requirements)
        val existingUsers = userDao.getUserByEmail("amara.nkosi@campuseats.test")
        if (existingUsers == null)
        {
            Log.d(TAG, "Seeding users table...")
            val users = listOf(
                UserEntity(
                    userId = "ADMN-4K7P-2Q9X-RT5M",
                    fullName = "Amara Nkosi",
                    username = "amara.nkosi",
                    email = "amara.nkosi@campuseats.test",
                    passwordHash = encryptPassword("Adm1n#Amara"),
                    role = UserRole.ADMINISTRATOR,
                    status = UserStatus.ACTIVE,
                    walletBalance = 500.0
                ),
                UserEntity(
                    userId = "ADMN-8B3W-6Y1Z-PL4N",
                    fullName = "Pieter van Wyk",
                    username = "pieter.vanwyk",
                    email = "pieter.vanwyk@campuseats.test",
                    passwordHash = encryptPassword("Adm1n#Pieter"),
                    role = UserRole.ADMINISTRATOR,
                    status = UserStatus.ACTIVE,
                    walletBalance = 500.0
                ),
                UserEntity(
                    userId = "VNDR-2T5H-8J3K-Q7L0",
                    fullName = "Thandiwe Mokoena",
                    username = "thandiwe.mokoena",
                    email = "thandiwe.mokoena@campuseats.test",
                    passwordHash = encryptPassword("Vend0r#Thandi"),
                    role = UserRole.VENDOR,
                    status = UserStatus.ACTIVE,
                    shopName = "Campus Corner Kitchen",
                    shopStatus = ShopStatus.OPEN
                ),
                UserEntity(
                    userId = "VNDR-9F4G-7N2M-XP5Q",
                    fullName = "Sipho Dlamini",
                    username = "sipho.dlamini",
                    email = "sipho.dlamini@campuseats.test",
                    passwordHash = encryptPassword("Vend0r#Sipho"),
                    role = UserRole.VENDOR,
                    status = UserStatus.ACTIVE,
                    shopName = "Braai Brothers",
                    shopStatus = ShopStatus.OPEN
                ),
                UserEntity(
                    userId = "VNDR-6C1V-9B4L-ZR8T",
                    fullName = "Annelie Botha",
                    username = "annelie.botha",
                    email = "annelie.botha@campuseats.test",
                    passwordHash = encryptPassword("Vend0r#Annelie"),
                    role = UserRole.VENDOR,
                    status = UserStatus.ACTIVE,
                    shopName = "Coffee and Koeksisters",
                    shopStatus = ShopStatus.OPEN
                ),
                UserEntity(
                    userId = "STDN-3J7R-5H2K-XQ9M",
                    fullName = "Lerato Khumalo",
                    username = "lerato.khumalo",
                    email = "lerato.khumalo@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Lerato"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 250.0
                ),
                UserEntity(
                    userId = "STDN-7P4W-1Y6N-BLZ2",
                    fullName = "Johan Pretorius",
                    username = "johan.pretorius",
                    email = "johan.pretorius@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Johan"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 180.0
                ),
                UserEntity(
                    userId = "STDN-5T8M-3K2L-ZXR6",
                    fullName = "Zanele Ndlovu",
                    username = "zanele.ndlovu",
                    email = "zanele.ndlovu@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Zanele"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 320.0
                ),
                UserEntity(
                    userId = "STDN-9R2B-7V4M-QP1X",
                    fullName = "Marius Steyn",
                    username = "marius.steyn",
                    email = "marius.steyn@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Marius"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 150.0
                ),
                UserEntity(
                    userId = "STDT-4K9X-2P7M-NZR5",
                    fullName = "Naledi Mahlangu",
                    username = "naledi.mahlangu",
                    email = "naledi.mahlangu@campuseats.test",
                    passwordHash = encryptPassword("Stud3nt#Naledi"),
                    role = UserRole.STUDENT,
                    status = UserStatus.ACTIVE,
                    walletBalance = 400.0
                )
            )
            for (user in users)
            {
                userDao.insertUser(user)
            }
        }

        // 2. Seed Menu Items (At least 10 items)
        if (menuItemDao.getAllMenuItems().first().isEmpty())
        {
            Log.d(TAG, "Seeding menu_items table...")
            val items = listOf(
                MenuItemEntity(vendorId = "VNDR-2T5H-8J3K-Q7L0", name = "Pap and Chakalaka", description = "Traditional maize porridge with spicy relish", price = 45.0, stock = 20, category = "Meals"),
                MenuItemEntity(vendorId = "VNDR-2T5H-8J3K-Q7L0", name = "Grilled Chicken Quarter", description = "Flame-grilled chicken quarter with chips", price = 65.0, stock = 15, category = "Meals"),
                MenuItemEntity(vendorId = "VNDR-2T5H-8J3K-Q7L0", name = "Mogodu", description = "Traditional slow-cooked tripe stew", price = 55.0, stock = 10, category = "Meals"),
                MenuItemEntity(vendorId = "VNDR-9F4G-7N2M-XP5Q", name = "Boerewors Roll", description = "Classic South African sausage roll with relish", price = 35.0, stock = 30, category = "Braai"),
                MenuItemEntity(vendorId = "VNDR-9F4G-7N2M-XP5Q", name = "Steak and Chips", description = "200g rump steak with seasoned fries", price = 85.0, stock = 12, category = "Braai"),
                MenuItemEntity(vendorId = "VNDR-9F4G-7N2M-XP5Q", name = "Vegetarian Skewers", description = "Grilled mixed vegetable skewers", price = 40.0, stock = 25, category = "Braai"),
                MenuItemEntity(vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Speciality Coffee", description = "Freshly brewed artisanal espresso blend", price = 30.0, stock = 50, category = "Beverages"),
                MenuItemEntity(vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Fresh Koeksisters", description = "Sweet traditional braided syrup-infused pastry", price = 15.0, stock = 40, category = "Snacks"),
                MenuItemEntity(vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Muffin Combo", description = "Large blueberry muffin with any hot drink", price = 45.0, stock = 20, category = "Breakfast"),
                MenuItemEntity(vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Rooibos Tea", description = "Organic South African herbal infusion", price = 22.0, stock = 60, category = "Beverages")
            )
            for (item in items)
            {
                menuItemDao.insertMenuItem(item)
            }
        }

        // 3. Seed Coupons (At least 10 items)
        if (couponDao.getAllCoupons().first().isEmpty())
        {
            Log.d(TAG, "Seeding coupons table...")
            val coupons = listOf(
                CouponEntity("CAMPUS10", 10.0, true),
                CouponEntity("EATS20", 20.0, true),
                CouponEntity("WELCOME5", 5.0, true),
                CouponEntity("VEND30", 30.0, true),
                CouponEntity("BURGER50", 50.0, true),
                CouponEntity("DISCOUNT15", 15.0, true),
                CouponEntity("SPRING25", 25.0, true),
                CouponEntity("BREAKFAST12", 12.0, true),
                CouponEntity("MIDWEEK18", 18.0, true),
                CouponEntity("FINALCODE40", 40.0, true)
            )
            for (coupon in coupons)
            {
                couponDao.insertCoupon(coupon)
            }
        }

        // 4. Seed Debit Cards (At least 10 items)
        if (debitCardDao.getCardsByUserId("STDT-4K9X-2P7M-NZR5").first().isEmpty())
        {
            Log.d(TAG, "Seeding debit_cards table...")
            val cards = listOf(
                DebitCardEntity(userId = "STDT-4K9X-2P7M-NZR5", cardNumber = "4321********8888", expiryDate = "12/28", cvv = "123"),
                DebitCardEntity(userId = "STDN-3J7R-5H2K-XQ9M", cardNumber = "5432********1111", expiryDate = "05/27", cvv = "456"),
                DebitCardEntity(userId = "STDN-7P4W-1Y6N-BLZ2", cardNumber = "4000********2222", expiryDate = "08/26", cvv = "789"),
                DebitCardEntity(userId = "STDN-5T8M-3K2L-ZXR6", cardNumber = "5105********3333", expiryDate = "09/29", cvv = "234"),
                DebitCardEntity(userId = "STDN-9R2B-7V4M-QP1X", cardNumber = "4916********4444", expiryDate = "11/27", cvv = "567"),
                DebitCardEntity(userId = "ADMN-4K7P-2Q9X-RT5M", cardNumber = "4532********5555", expiryDate = "01/30", cvv = "890"),
                DebitCardEntity(userId = "ADMN-8B3W-6Y1Z-PL4N", cardNumber = "4556********6666", expiryDate = "02/28", cvv = "321"),
                DebitCardEntity(userId = "VNDR-2T5H-8J3K-Q7L0", cardNumber = "4716********7777", expiryDate = "03/27", cvv = "654"),
                DebitCardEntity(userId = "VNDR-9F4G-7N2M-XP5Q", cardNumber = "5221********9999", expiryDate = "04/26", cvv = "987"),
                DebitCardEntity(userId = "VNDR-6C1V-9B4L-ZR8T", cardNumber = "5353********0000", expiryDate = "06/28", cvv = "159")
            )
            for (card in cards)
            {
                debitCardDao.insertCard(card)
            }
        }

        // 5. Seed Cart Items (At least 10 items)
        if (cartDao.getCartByUserId("STDT-4K9X-2P7M-NZR5").first().isEmpty())
        {
            Log.d(TAG, "Seeding cart_items table...")
            val cartItems = listOf(
                CartItemEntity(userId = "STDT-4K9X-2P7M-NZR5", itemId = 1L, vendorId = "VNDR-2T5H-8J3K-Q7L0", name = "Pap and Chakalaka", price = 45.0, quantity = 1),
                CartItemEntity(userId = "STDN-3J7R-5H2K-XQ9M", itemId = 4L, vendorId = "VNDR-9F4G-7N2M-XP5Q", name = "Boerewors Roll", price = 35.0, quantity = 2),
                CartItemEntity(userId = "STDN-7P4W-1Y6N-BLZ2", itemId = 7L, vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Speciality Coffee", price = 30.0, quantity = 1),
                CartItemEntity(userId = "STDN-5T8M-3K2L-ZXR6", itemId = 8L, vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Fresh Koeksisters", price = 15.0, quantity = 3),
                CartItemEntity(userId = "STDN-9R2B-7V4M-QP1X", itemId = 2L, vendorId = "VNDR-2T5H-8J3K-Q7L0", name = "Grilled Chicken Quarter", price = 65.0, quantity = 1),
                CartItemEntity(userId = "STDT-4K9X-2P7M-NZR5", itemId = 10L, vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Rooibos Tea", price = 22.0, quantity = 1),
                CartItemEntity(userId = "STDN-3J7R-5H2K-XQ9M", itemId = 6L, vendorId = "VNDR-9F4G-7N2M-XP5Q", name = "Vegetarian Skewers", price = 40.0, quantity = 2),
                CartItemEntity(userId = "STDN-7P4W-1Y6N-BLZ2", itemId = 9L, vendorId = "VNDR-6C1V-9B4L-ZR8T", name = "Muffin Combo", price = 45.0, quantity = 1),
                CartItemEntity(userId = "STDN-5T8M-3K2L-ZXR6", itemId = 3L, vendorId = "VNDR-2T5H-8J3K-Q7L0", name = "Mogodu", price = 55.0, quantity = 1),
                CartItemEntity(userId = "STDN-9R2B-7V4M-QP1X", itemId = 5L, vendorId = "VNDR-9F4G-7N2M-XP5Q", name = "Steak and Chips", price = 85.0, quantity = 1)
            )
            for (item in cartItems)
            {
                cartDao.addToCart(item)
            }
        }

        // 6. Seed Feedback (At least 10 items)
        if (feedbackDao.getAllFeedback().first().isEmpty())
        {
            Log.d(TAG, "Seeding feedback table...")
            val feedbacks = listOf(
                FeedbackEntity(userId = "STDT-4K9X-2P7M-NZR5", type = FeedbackType.COMPLIMENT, subject = "Great Service", message = "Food from Campus Corner is amazing!", userName = "Naledi Mahlangu", userEmail = "naledi.mahlangu@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:00:00Z", updatedAt = "2026-03-01T12:00:00Z"),
                FeedbackEntity(userId = "STDN-3J7R-5H2K-XQ9M", type = FeedbackType.COMPLAINT, subject = "Long Waiting Time", message = "Waited 30 minutes at Braai Brothers", userName = "Lerato Khumalo", userEmail = "lerato.khumalo@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:05:00Z", updatedAt = "2026-03-01T12:05:00Z"),
                FeedbackEntity(userId = "STDN-7P4W-1Y6N-BLZ2", type = FeedbackType.COMPLIMENT, subject = "Delicious Koeksisters", message = "Best pastries on campus grounds", userName = "Johan Pretorius", userEmail = "johan.pretorius@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:10:00Z", updatedAt = "2026-03-01T12:10:00Z"),
                FeedbackEntity(userId = "STDN-5T8M-3K2L-ZXR6", type = FeedbackType.COMPLIMENT, subject = "Friendly Vendor", message = "Annelie is always smiling!", userName = "Zanele Ndlovu", userEmail = "zanele.ndlovu@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:15:00Z", updatedAt = "2026-03-01T12:15:00Z"),
                FeedbackEntity(userId = "STDN-9R2B-7V4M-QP1X", type = FeedbackType.COMPLAINT, subject = "Cold Chips", message = "Fries were slightly cold today", userName = "Marius Steyn", userEmail = "marius.steyn@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:20:00Z", updatedAt = "2026-03-01T12:20:00Z"),
                FeedbackEntity(userId = "STDT-4K9X-2P7M-NZR5", type = FeedbackType.COMPLIMENT, subject = "Clean App UI", message = "App navigation is smooth and interactive", userName = "Naledi Mahlangu", userEmail = "naledi.mahlangu@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:25:00Z", updatedAt = "2026-03-01T12:25:00Z"),
                FeedbackEntity(userId = "STDN-3J7R-5H2K-XQ9M", type = FeedbackType.COMPLIMENT, subject = "Spicy Chakalaka", message = "Perfect spice level, loved it", userName = "Lerato Khumalo", userEmail = "lerato.khumalo@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:30:00Z", updatedAt = "2026-03-01T12:30:00Z"),
                FeedbackEntity(userId = "STDN-7P4W-1Y6N-BLZ2", type = FeedbackType.COMPLAINT, subject = "Out of Stock", message = "Blueberry muffins ran out early", userName = "Johan Pretorius", userEmail = "johan.pretorius@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:35:00Z", updatedAt = "2026-03-01T12:35:00Z"),
                FeedbackEntity(userId = "STDN-5T8M-3K2L-ZXR6", type = FeedbackType.COMPLIMENT, subject = "Awesome Boerewors", message = "Authentic flavor, great job", userName = "Zanele Ndlovu", userEmail = "zanele.ndlovu@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:40:00Z", updatedAt = "2026-03-01T12:40:00Z"),
                FeedbackEntity(userId = "STDN-9R2B-7V4M-QP1X", type = FeedbackType.COMPLIMENT, subject = "Quick Payout Setup", message = "Vendor configuration works well", userName = "Marius Steyn", userEmail = "marius.steyn@campuseats.test", status = FeedbackStatus.PENDING, createdAt = "2026-03-01T12:45:00Z", updatedAt = "2026-03-01T12:45:00Z")
            )
            for (fb in feedbacks)
            {
                feedbackDao.insertFeedback(fb)
            }
        }

        // 7. Seed Orders (At least 10 items)
        if (orderDao.getAllOrders().first().isEmpty())
        {
            Log.d(TAG, "Seeding orders table...")
            val orders = listOf(
                OrderEntity(customerId = "STDT-4K9X-2P7M-NZR5", vendorId = "VNDR-2T5H-8J3K-Q7L0", itemsJson = "[]", totalAmount = 45.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.CAMPUS_WALLET, pickupTime = "12:30"),
                OrderEntity(customerId = "STDN-3J7R-5H2K-XQ9M", vendorId = "VNDR-9F4G-7N2M-XP5Q", itemsJson = "[]", totalAmount = 70.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.DEBIT_CARD, pickupTime = "13:00"),
                OrderEntity(customerId = "STDN-7P4W-1Y6N-BLZ2", vendorId = "VNDR-6C1V-9B4L-ZR8T", itemsJson = "[]", totalAmount = 30.0, status = OrderStatus.READY, paymentMethod = PaymentMethod.CAMPUS_WALLET, pickupTime = "08:15"),
                OrderEntity(customerId = "STDN-5T8M-3K2L-ZXR6", vendorId = "VNDR-6C1V-9B4L-ZR8T", itemsJson = "[]", totalAmount = 45.0, status = OrderStatus.PREPARING, paymentMethod = PaymentMethod.DEBIT_CARD, pickupTime = "09:30"),
                OrderEntity(customerId = "STDN-9R2B-7V4M-QP1X", vendorId = "VNDR-2T5H-8J3K-Q7L0", itemsJson = "[]", totalAmount = 65.0, status = OrderStatus.PENDING, paymentMethod = PaymentMethod.DEBIT_CARD, pickupTime = "12:45"),
                OrderEntity(customerId = "STDT-4K9X-2P7M-NZR5", vendorId = "VNDR-6C1V-9B4L-ZR8T", itemsJson = "[]", totalAmount = 22.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.CAMPUS_WALLET, pickupTime = "10:00"),
                OrderEntity(customerId = "STDN-3J7R-5H2K-XQ9M", vendorId = "VNDR-9F4G-7N2M-XP5Q", itemsJson = "[]", totalAmount = 80.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.DEBIT_CARD, pickupTime = "14:00"),
                OrderEntity(customerId = "STDN-7P4W-1Y6N-BLZ2", vendorId = "VNDR-6C1V-9B4L-ZR8T", itemsJson = "[]", totalAmount = 45.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.CAMPUS_WALLET, pickupTime = "07:45"),
                OrderEntity(customerId = "STDN-5T8M-3K2L-ZXR6", vendorId = "VNDR-2T5H-8J3K-Q7L0", itemsJson = "[]", totalAmount = 55.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.CAMPUS_WALLET, pickupTime = "13:15"),
                OrderEntity(customerId = "STDN-9R2B-7V4M-QP1X", vendorId = "VNDR-9F4G-7N2M-XP5Q", itemsJson = "[]", totalAmount = 85.0, status = OrderStatus.COMPLETED, paymentMethod = PaymentMethod.DEBIT_CARD, pickupTime = "12:15")
            )
            for (order in orders)
            {
                orderDao.insertOrder(order)
            }
        }
        Log.d(TAG, "Database successfully pre-populated with required 10 records per table.")
    }
}
