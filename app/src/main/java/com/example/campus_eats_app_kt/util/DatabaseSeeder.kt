package com.example.campus_eats_app_kt.util

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.campus_eats_app_kt.BuildConfig
import com.example.campus_eats_app_kt.data.CampusEatsDatabase
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * DatabaseSeeder handles prepopulating the local database with sample data.
 * Finding 2: Seeding is gated by BuildConfig.ENABLE_DEMO_DATA.
 * Finding 16: Uses authoritative records and atomic transactions.
 */
object DatabaseSeeder
{
    private const val TAG = "DatabaseSeeder"

    /**
     * Hashes passwords using SHA-256.
     */
    fun encryptPassword(password: String): String
    {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + String.format("%02x", it) }
    }

    suspend fun seed(context: Context) = withContext(Dispatchers.IO)
    {
        if (!BuildConfig.ENABLE_DEMO_DATA) return@withContext

        val db = CampusEatsDatabase.getDatabase(context)
        val userDao = db.userDao()

        if (userDao.getUserByEmail("amara.nkosi@campuseats.test") != null) return@withContext

        db.withTransaction()
        {
            val menuItemDao = db.menuItemDao()
            val couponDao = db.couponDao()
            val orderDao = db.orderDao()

            Log.d(TAG, "Seeding 10 authoritative user accounts...")
            val users = listOf(
                UserEntity("ADMN4K7P2Q9XRT5M", "Amara Nkosi", "amara.nkosi", "amara.nkosi@campuseats.test", encryptPassword("Adm1n#Amara"), UserRole.ADMINISTRATOR, UserStatus.ACTIVE, 500.0),
                UserEntity("ADMN8B3W6Y1ZPL4N", "Pieter van Wyk", "pieter.vanwyk", "pieter.vanwyk@campuseats.test", encryptPassword("Adm1n#Pieter"), UserRole.ADMINISTRATOR, UserStatus.ACTIVE, 500.0),
                UserEntity("VNDR2T5H8J3KQ7L", "Thandiwe Mokoena", "thandiwe.mokoena", "thandiwe.mokoena@campuseats.test", encryptPassword("Vend0r#Thandi"), UserRole.VENDOR, UserStatus.ACTIVE, 0.0, "Campus Corner Kitchen", ShopStatus.OPEN),
                UserEntity("VNDR9F4G7N2MXP5Q", "Sipho Dlamini", "sipho.dlamini", "sipho.dlamini@campuseats.test", encryptPassword("Vend0r#Sipho"), UserRole.VENDOR, UserStatus.ACTIVE, 0.0, "Braai Brothers", ShopStatus.OPEN),
                UserEntity("VNDR6C1V9B4LZR8T", "Annelie Botha", "annelie.botha", "annelie.botha@campuseats.test", encryptPassword("Vend0r#Annelie"), UserRole.VENDOR, UserStatus.ACTIVE, 0.0, "Coffee and Koeksisters", ShopStatus.OPEN),
                UserEntity("STDN3J7R5H2KXQ9M", "Lerato Khumalo", "lerato.khumalo", "lerato.khumalo@campuseats.test", encryptPassword("Stand@rd#Lerato"), UserRole.STANDARD, UserStatus.ACTIVE, 250.0),
                UserEntity("STDN7P4W1Y6NBLZ2", "Johan Pretorius", "johan.pretorius", "johan.pretorius@campuseats.test", encryptPassword("Stand@rd#Johan"), UserRole.STANDARD, UserStatus.ACTIVE, 180.0),
                UserEntity("STDN5T8M3K2LZXR6Q", "Zanele Ndlovu", "zanele.ndlovu", "zanele.ndlovu@campuseats.test", encryptPassword("Stand@rd#Zanele"), UserRole.STANDARD, UserStatus.ACTIVE, 320.0),
                UserEntity("STDN9R2B7V4MQP1X", "Marius Steyn", "marius.steyn", "marius.steyn@campuseats.test", encryptPassword("Stand@rd#Marius"), UserRole.STANDARD, UserStatus.ACTIVE, 150.0),
                UserEntity("STDT4K9X2P7MNZR5B", "Naledi Mahlangu", "naledi.mahlangu", "naledi.mahlangu@campuseats.test", encryptPassword("Stud3nt#Naledi"), UserRole.STUDENT, UserStatus.ACTIVE, 400.0)
            )
            users.forEach { userDao.insertUser(it) }

            Log.d(TAG, "Seeding menu items...")
            val items = listOf(
                MenuItemEntity(1, "VNDR2T5H8J3KQ7L", "Pap and Chakalaka", "Traditional special", 45.0, 20, "Meals"),
                MenuItemEntity(2, "VNDR2T5H8J3KQ7L", "Grilled Chicken", "Flame-grilled quarter", 65.0, 15, "Meals"),
                MenuItemEntity(3, "VNDR9F4G7N2MXP5Q", "Boerewors Roll", "Braai staple", 35.0, 30, "Braai"),
                MenuItemEntity(4, "VNDR9F4G7N2MXP5Q", "Steak and Chips", "200g rump", 85.0, 12, "Braai"),
                MenuItemEntity(5, "VNDR9F4G7N2MXP5Q", "Vegetarian Skewer", "Mixed veggies", 40.0, 25, "Braai"),
                MenuItemEntity(6, "VNDR6C1V9B4LZR8T", "Speciality Coffee", "Artisanal blend", 30.0, 50, "Beverages"),
                MenuItemEntity(7, "VNDR6C1V9B4LZR8T", "Koeksister", "Sweet pastry", 15.0, 40, "Snacks"),
                MenuItemEntity(8, "VNDR6C1V9B4LZR8T", "Muffin", "Blueberry", 25.0, 20, "Snacks"),
                MenuItemEntity(9, "VNDR6C1V9B4LZR8T", "Rooibos Tea", "Herbal infusion", 22.0, 60, "Beverages"),
                MenuItemEntity(10, "VNDR2T5H8J3KQ7L", "Mogodu", "Tripe stew", 55.0, 10, "Meals")
            )
            items.forEach { menuItemDao.insertMenuItem(it) }

            Log.d(TAG, "Seeding coupons...")
            val coupons = listOf(
                CouponEntity("CAMPUS10", 10.0), CouponEntity("EATS20", 20.0),
                CouponEntity("WELCOME5", 5.0), CouponEntity("VEND30", 30.0),
                CouponEntity("BURGER50", 50.0), CouponEntity("DISCOUNT15", 15.0),
                CouponEntity("SPRING25", 25.0), CouponEntity("BREAKFAST12", 12.0),
                CouponEntity("MIDWEEK18", 18.0), CouponEntity("FINALCODE40", 40.0)
            )
            coupons.forEach { couponDao.insertCoupon(it) }

            Log.d(TAG, "Seeding initial orders...")
            val orders = listOf(
                OrderEntity(IdGenerator.generateOrderId(), "STDT4K9X2P7MNZR5B", "VNDR2T5H8J3KQ7L", "[]", 45.0, OrderStatus.COMPLETED, PaymentMethod.CAMPUS_WALLET, "12:30"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN3J7R5H2KXQ9M", "VNDR9F4G7N2MXP5Q", "[]", 70.0, OrderStatus.COMPLETED, PaymentMethod.DEBIT_CARD, "13:00"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN7P4W1Y6NBLZ2", "VNDR6C1V9B4LZR8T", "[]", 30.0, OrderStatus.READY, PaymentMethod.CAMPUS_WALLET, "08:15"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN5T8M3K2LZXR6Q", "VNDR6C1V9B4LZR8T", "[]", 45.0, OrderStatus.PREPARING, PaymentMethod.DEBIT_CARD, "09:30"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN9R2B7V4MQP1X", "VNDR2T5H8J3KQ7L", "[]", 65.0, OrderStatus.PENDING, PaymentMethod.DEBIT_CARD, "12:45"),
                OrderEntity(IdGenerator.generateOrderId(), "STDT4K9X2P7MNZR5B", "VNDR6C1V9B4LZR8T", "[]", 22.0, OrderStatus.COMPLETED, PaymentMethod.CAMPUS_WALLET, "10:00"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN3J7R5H2KXQ9M", "VNDR9F4G7N2MXP5Q", "[]", 80.0, OrderStatus.COMPLETED, PaymentMethod.DEBIT_CARD, "14:00"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN7P4W1Y6NBLZ2", "VNDR6C1V9B4LZR8T", "[]", 45.0, OrderStatus.COMPLETED, PaymentMethod.CAMPUS_WALLET, "07:45"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN5T8M3K2LZXR6Q", "VNDR2T5H8J3KQ7L", "[]", 55.0, OrderStatus.COMPLETED, PaymentMethod.CAMPUS_WALLET, "13:15"),
                OrderEntity(IdGenerator.generateOrderId(), "STDN9R2B7V4MQP1X", "VNDR9F4G7N2MXP5Q", "[]", 85.0, OrderStatus.COMPLETED, PaymentMethod.DEBIT_CARD, "12:15")
            )
            orders.forEach { orderDao.insertOrder(it) }
        }
    }
}
