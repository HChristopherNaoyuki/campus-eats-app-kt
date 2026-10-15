package com.example.campus_eats_app_kt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.dao.DebitCardDao
import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity

/**
 * CampusEatsDatabase is the primary Room database for the application.
 * It follows an offline-first architecture, storing all user, order, and menu data locally.
 *
 * This implementation includes robust, idempotent migrations to handle schema updates
 * without data loss or duplicate column crashes.
 */
@Database(
    entities = [
        UserEntity::class,
        MenuItemEntity::class,
        OrderEntity::class,
        CartItemEntity::class,
        FeedbackEntity::class,
        CouponEntity::class,
        DebitCardEntity::class,
    ],
    version = 12, // Incremented to 12 for CartItemEntity imageUrl property
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class CampusEatsDatabase : RoomDatabase()
{
    abstract fun userDao(): UserDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun couponDao(): CouponDao
    abstract fun debitCardDao(): DebitCardDao

    companion object
    {
        @Volatile
        private var INSTANCE: CampusEatsDatabase? = null

        private val MIGRATION_11_12 = object : Migration(11, 12)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                addColumnIfNotExists(db, "cart_items", "imageUrl", "TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                addColumnIfNotExists(db, "menu_items", "isInventoryTracked", "INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                addColumnIfNotExists(db, "orders", "isPendingSync", "INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                addColumnIfNotExists(db, "coupons", "expiryDate", "INTEGER NOT NULL DEFAULT 0")
                addColumnIfNotExists(db, "coupons", "assignedUserId", "TEXT")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                addColumnIfNotExists(db, "users", "usercode", "TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                addColumnIfNotExists(db, "users", "username", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "users", "registrationDate", "INTEGER NOT NULL DEFAULT 0")

                addColumnIfNotExists(
                    db,
                    "orders",
                    "paymentMethod",
                    "TEXT NOT NULL DEFAULT 'DEBIT_CARD'",
                )
                addColumnIfNotExists(db, "orders", "pickupTime", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "orders", "specialRequests", "TEXT")

                addColumnIfNotExists(db, "feedback", "type", "TEXT NOT NULL DEFAULT 'compliment'")
            }
        }

        private fun addColumnIfNotExists(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDefinition: String,
        )
        {
            db.query("PRAGMA table_info($tableName)").use()
            { cursor ->
                var columnExists = false
                while (cursor.moveToNext())
                {
                    val name = cursor.getString(1)
                    if (name == columnName)
                    {
                        columnExists = true
                        break
                    }
                }

                if (!columnExists)
                {
                    db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
                }
            }
        }

        fun getDatabase(context: Context): CampusEatsDatabase
        {
            return INSTANCE ?: synchronized(this)
            {
                val existing = INSTANCE
                if (existing != null) return@synchronized existing

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampusEatsDatabase::class.java,
                    "campus_eats_database",
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
