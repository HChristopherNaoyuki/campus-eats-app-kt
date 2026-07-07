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
 * without data loss or "duplicate column" crashes.
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
    version = 7, // Incremented from 6 to 7 due to schema changes in UserEntity, OrderEntity, and FeedbackEntity
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
         * Handles the addition of mandatory profile, fulfillment, and categorization columns.
         * 
         * Logic: Uses addColumnIfNotExists to prevent "duplicate column name" crashes if 
         * the schema has already been partially modified.
         */
        private val MIGRATION_6_7 = object : Migration(6, 7)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                // Update users table with new profile requirements
                addColumnIfNotExists(db, "users", "username", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "users", "registrationDate", "INTEGER NOT NULL DEFAULT 0")

                // Update orders table with fulfillment and payment metadata
                addColumnIfNotExists(
                    db,
                    "orders",
                    "paymentMethod",
                    "TEXT NOT NULL DEFAULT 'DEBIT_CARD'"
                )
                addColumnIfNotExists(db, "orders", "pickupTime", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "orders", "specialRequests", "TEXT")

                // Update feedback table with categorization (Fixed crash target)
                addColumnIfNotExists(db, "feedback", "type", "TEXT NOT NULL DEFAULT 'COMPLIMENT'")
            }
        }

        /**
         * Safely adds a column to a table only if it does not already exist in the SQLite schema.
         * 
         * @param db The lower-level database access object provided by Room.
         * @param tableName The table to check and modify.
         * @param columnName The name of the column to potentially add.
         * @param columnDefinition The SQL definition string (Type, Constraints, Defaults).
         */
        private fun addColumnIfNotExists(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDefinition: String
        )
        {
            // Execute PRAGMA to get the list of columns in the table
            val cursor = db.query("PRAGMA table_info($tableName)")
            var columnExists = false

            try
            {
                while (cursor.moveToNext())
                {
                    // The 'name' column in PRAGMA table_info result is at index 1
                    val name = cursor.getString(1)
                    if (name == columnName)
                    {
                        columnExists = true
                        break
                    }
                }
            }
            finally
            {
                cursor.close()
            }

            // Perform the ALTER statement only if the introspection confirms the column is missing
            if (!columnExists)
            {
                db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
            }
        }

        /**
         * Returns the singleton database instance.
         * Uses a thread-safe double-check locking pattern to ensure only one instance exists.
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
                    .fallbackToDestructiveMigration() // Last resort if no valid migration path is found
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
