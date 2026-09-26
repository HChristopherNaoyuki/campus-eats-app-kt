package com.example.campus_eats_app_kt.data

import com.google.firebase.database.FirebaseDatabase

/**
 * FirebaseDatabaseProvider manages the singleton instance of FirebaseDatabase.
 * Configures the specific regional Realtime Database URL and enables disk persistence.
 */
object FirebaseDatabaseProvider
{
    private const val DATABASE_URL = "https://campus-eats-db-default-rtdb.europe-west1.firebasedatabase.app"

    /**
     * Provides a thread-safe, lazily initialized FirebaseDatabase instance.
     * Persistence is enabled to support offline-first operation.
     */
    val instance: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(DATABASE_URL).apply {
            // Enable local disk persistence to store cached data offline
            setPersistenceEnabled(true)

            // Configure cache size limit to 10 megabytes for efficient storage
            setPersistenceCacheSizeBytes(10 * 1024 * 1024)
        }
    }
}
