package com.example.campus_eats_app_kt.util

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.campus_eats_app_kt.BuildConfig
import com.example.campus_eats_app_kt.data.CampusEatsDatabase
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
 * Finding 2: Seeding is gated by BuildConfig.ENABLE_DEMO_DATA to prevent bypass in production.
 */
object DatabaseSeeder
{
    private const val TAG = "DatabaseSeeder"

    /**
     * Hashes/encrypts passwords using SHA-256.
     * Note: Finding 2 identifies this as a potential bypass if used locally without Firebase.
     */
    fun encryptPassword(password: String): String
    {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + String.format("%02x", it) }
    }

    /**
     * Seeds the database asynchronously if enabled and empty.
     * Finding 16: Uses atomic transaction and consistent marker check.
     */
    suspend fun seed(context: Context) = withContext(Dispatchers.IO)
    {
        if (!BuildConfig.ENABLE_DEMO_DATA)
        {
            Log.i(TAG, "Demo data seeding skipped (ENABLE_DEMO_DATA=false)")
            return@withContext
        }

        val db = CampusEatsDatabase.getDatabase(context)
        val userDao = db.userDao()

        // Use the first administrator as the "seeded" marker to prevent repeated execution
        if (userDao.getUserByEmail("amara.nkosi@campuseats.test") != null)
        {
            Log.v(TAG, "Database already contains seed data. Skipping seeder.")
            return@withContext
        }

        db.withTransaction()
        {
            val menuItemDao = db.menuItemDao()
            val couponDao = db.couponDao()
            val debitCardDao = db.debitCardDao()

            Log.d(TAG, "Seeding authoritative users table with exact project sample data...")
            val users = listOf(
                UserEntity(
                    userId = "ADMN4K7P2Q9XRT5M",
                    fullName = "Amara Nkosi",
                    username = "amara.nkosi",
                    email = "amara.nkosi@campuseats.test",
                    passwordHash = encryptPassword("Adm1n#Amara"),
                    role = UserRole.ADMINISTRATOR,
                    status = UserStatus.ACTIVE,
                    walletBalance = 500.0,
                ),
                UserEntity(
                    userId = "ADMN8B3W6Y1ZPL4N",
                    fullName = "Pieter van Wyk",
                    username = "pieter.vanwyk",
                    email = "pieter.vanwyk@campuseats.test",
                    passwordHash = encryptPassword("Adm1n#Pieter"),
                    role = UserRole.ADMINISTRATOR,
                    status = UserStatus.ACTIVE,
                    walletBalance = 500.0,
                ),
                UserEntity(
                    userId = "VNDR2T5H8J3KQ7L",
                    fullName = "Thandiwe Mokoena",
                    username = "thandiwe.mokoena",
                    email = "thandiwe.mokoena@campuseats.test",
                    passwordHash = encryptPassword("Vend0r#Thandi"),
                    role = UserRole.VENDOR,
                    status = UserStatus.ACTIVE,
                    shopName = "Campus Corner Kitchen",
                    shopStatus = ShopStatus.OPEN,
                ),
                UserEntity(
                    userId = "VNDR9F4G7N2MXP5Q",
                    fullName = "Sipho Dlamini",
                    username = "sipho.dlamini",
                    email = "sipho.dlamini@campuseats.test",
                    passwordHash = encryptPassword("Vend0r#Sipho"),
                    role = UserRole.VENDOR,
                    status = UserStatus.ACTIVE,
                    shopName = "Braai Brothers",
                    shopStatus = ShopStatus.OPEN,
                ),
                UserEntity(
                    userId = "VNDR6C1V9B4LZR8T",
                    fullName = "Annelie Botha",
                    username = "annelie.botha",
                    email = "annelie.botha@campuseats.test",
                    passwordHash = encryptPassword("Vend0r#Annelie"),
                    role = UserRole.VENDOR,
                    status = UserStatus.ACTIVE,
                    shopName = "Coffee and Koeksisters",
                    shopStatus = ShopStatus.OPEN,
                ),
                UserEntity(
                    userId = "STDN3J7R5H2KXQ9M",
                    fullName = "Lerato Khumalo",
                    username = "lerato.khumalo",
                    email = "lerato.khumalo@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Lerato"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 250.0,
                ),
                UserEntity(
                    userId = "STDN7P4W1Y6NBLZ2",
                    fullName = "Johan Pretorius",
                    username = "johan.pretorius",
                    email = "johan.pretorius@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Johan"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 180.0,
                ),
                UserEntity(
                    userId = "STDN5T8M3K2LZXR6Q",
                    fullName = "Zanele Ndlovu",
                    username = "zanele.ndlovu",
                    email = "zanele.ndlovu@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Zanele"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 320.0,
                ),
                UserEntity(
                    userId = "STDN9R2B7V4MQP1X",
                    fullName = "Marius Steyn",
                    username = "marius.steyn",
                    email = "marius.steyn@campuseats.test",
                    passwordHash = encryptPassword("Stand@rd#Marius"),
                    role = UserRole.STANDARD,
                    status = UserStatus.ACTIVE,
                    walletBalance = 150.0,
                ),
                UserEntity(
                    userId = "STDT4K9X2P7MNZR5B",
                    fullName = "Naledi Mahlangu",
                    username = "naledi.mahlangu",
                    email = "naledi.mahlangu@campuseats.test",
                    passwordHash = encryptPassword("Stud3nt#Naledi"),
                    role = UserRole.STUDENT,
                    status = UserStatus.ACTIVE,
                    walletBalance = 400.0,
                ),
            )
            for (user in users)
            {
                userDao.insertUser(user)
            }

            Log.d(TAG, "Seeding menu items linked to new Vendor IDs...")
            val items = listOf(
                MenuItemEntity(vendorId = "VNDR2T5H8J3KQ7L", name = "Pap and Chakalaka", description = "Traditional maize porridge with spicy relish", price = 45.0, stock = 20, category = "Meals"),
                MenuItemEntity(vendorId = "VNDR2T5H8J3KQ7L", name = "Grilled Chicken Quarter", description = "Flame-grilled chicken quarter with chips", price = 65.0, stock = 15, category = "Meals"),
                MenuItemEntity(vendorId = "VNDR2T5H8J3KQ7L", name = "Mogodu", description = "Traditional slow-cooked tripe stew", price = 55.0, stock = 10, category = "Meals"),
                MenuItemEntity(vendorId = "VNDR9F4G7N2MXP5Q", name = "Boerewors Roll", description = "Classic South African sausage roll with relish", price = 35.0, stock = 30, category = "Braai"),
                MenuItemEntity(vendorId = "VNDR9F4G7N2MXP5Q", name = "Steak and Chips", description = "200g rump steak with seasoned fries", price = 85.0, stock = 12, category = "Braai"),
                MenuItemEntity(vendorId = "VNDR9F4G7N2MXP5Q", name = "Vegetarian Skewers", description = "Grilled mixed vegetable skewers", price = 40.0, stock = 25, category = "Braai"),
                MenuItemEntity(vendorId = "VNDR6C1V9B4LZR8T", name = "Speciality Coffee", description = "Freshly brewed artisanal espresso blend", price = 30.0, stock = 50, category = "Beverages"),
                MenuItemEntity(vendorId = "VNDR6C1V9B4LZR8T", name = "Fresh Koeksisters", description = "Sweet traditional braided syrup-infused pastry", price = 15.0, stock = 40, category = "Snacks"),
                MenuItemEntity(vendorId = "VNDR6C1V9B4LZR8T", name = "Muffin Combo", description = "Large blueberry muffin with any hot drink", price = 45.0, stock = 20, category = "Breakfast"),
                MenuItemEntity(vendorId = "VNDR6C1V9B4LZR8T", name = "Rooibos Tea", description = "Organic South African herbal infusion", price = 22.0, stock = 60, category = "Beverages"),
            )
            for (item in items)
            {
                menuItemDao.insertMenuItem(item)
            }

            Log.d(TAG, "Seeding coupons...")
            val coupons = listOf(
                CouponEntity("CAMPUS10", 10.0, isActive = true),
                CouponEntity("EATS20", 20.0, isActive = true),
                CouponEntity("WELCOME5", 5.0, isActive = true),
                CouponEntity("VEND30", 30.0, isActive = true),
                CouponEntity("BURGER50", 50.0, isActive = true),
                CouponEntity("DISCOUNT15", 15.0, isActive = true),
                CouponEntity("SPRING25", 25.0, isActive = true),
                CouponEntity("BREAKFAST12", 12.0, isActive = true),
                CouponEntity("MIDWEEK18", 18.0, isActive = true),
                CouponEntity("FINALCODE40", 40.0, isActive = true),
            )
            for (coupon in coupons)
            {
                couponDao.insertCoupon(coupon)
            }

            Log.d(TAG, "Seeding debit cards linked to sample User IDs...")
            val cards = listOf(
                DebitCardEntity(userId = "STDT4K9X2P7MNZR5B", cardNumber = "**** **** **** 8888", expiryDate = "12/28"),
                DebitCardEntity(userId = "STDN3J7R5H2KXQ9M", cardNumber = "**** **** **** 1111", expiryDate = "05/27"),
                DebitCardEntity(userId = "STDN7P4W1Y6NBLZ2", cardNumber = "**** **** **** 2222", expiryDate = "08/26"),
                DebitCardEntity(userId = "STDN5T8M3K2LZXR6Q", cardNumber = "**** **** **** 3333", expiryDate = "09/29"),
                DebitCardEntity(userId = "STDN9R2B7V4MQP1X", cardNumber = "**** **** **** 4444", expiryDate = "11/27"),
                DebitCardEntity(userId = "ADMN4K7P2Q9XRT5M", cardNumber = "**** **** **** 5555", expiryDate = "01/30"),
                DebitCardEntity(userId = "ADMN8B3W6Y1ZPL4N", cardNumber = "**** **** **** 6666", expiryDate = "02/28"),
                DebitCardEntity(userId = "VNDR2T5H8J3KQ7L", cardNumber = "**** **** **** 7777", expiryDate = "03/27"),
                DebitCardEntity(userId = "VNDR9F4G7N2MXP5Q", cardNumber = "**** **** **** 9999", expiryDate = "04/26"),
                DebitCardEntity(userId = "VNDR6C1V9B4LZR8T", cardNumber = "**** **** **** 0000", expiryDate = "06/28"),
            )
            for (card in cards)
            {
                debitCardDao.insertCard(card)
            }
        }
        Log.d(TAG, "Database successfully pre-populated with project-defined records.")
    }
}
