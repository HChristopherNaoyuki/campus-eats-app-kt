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
 */
@Database(
    entities = [
        UserEntity::class,
        MenuItemEntity::class,
        OrderEntity::class,
        CartItemEntity::class,
        FeedbackEntity::class,
        CouponEntity::class,
        DebitCardEntity::class
    ],
    version = 7, // Incremented from 6 to 7 due to schema changes in UserEntity and OrderEntity
    exportSchema = false
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

        /**
         * Migration from version 6 to 7.
         * This handles the addition of mandatory columns introduced during the refactor.
         * Defaults are provided for NOT NULL columns to prevent data loss on existing records.
         */
        private val MIGRATION_6_7 = object : Migration(6, 7)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                // Update users table with new profile requirements
                db.execSQL("ALTER TABLE users ADD COLUMN username TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN registrationDate INTEGER NOT NULL DEFAULT 0")

                // Update orders table with fulfillment and payment metadata
                db.execSQL("ALTER TABLE orders ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT 'DEBIT_CARD'")
                db.execSQL("ALTER TABLE orders ADD COLUMN pickupTime TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE orders ADD COLUMN specialRequests TEXT")

                // Update feedback table with categorization
                db.execSQL("ALTER TABLE feedback ADD COLUMN type TEXT NOT NULL DEFAULT 'COMPLIMENT'")
            }
        }

        /**
         * Returns the singleton database instance.
         * Uses a thread-safe double-check locking pattern.
         */
        fun getDatabase(context: Context): CampusEatsDatabase
        {
            return INSTANCE ?: synchronized(this)
            {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampusEatsDatabase::class.java,
                    "campus_eats_database"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration() // Safety fallback for corrupted or extremely old versions
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
