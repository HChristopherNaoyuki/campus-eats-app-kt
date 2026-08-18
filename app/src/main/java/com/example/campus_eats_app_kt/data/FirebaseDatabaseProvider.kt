package com.example.campus_eats_app_kt.data

import com.google.firebase.database.FirebaseDatabase

/**
 * FirebaseDatabaseProvider ensures the application connects to the designated Realtime Database instance.
 */
object FirebaseDatabaseProvider
{
    private const val DATABASE_URL = "https://campus-eats-db-default-rtdb.firebaseio.com/"

    /**
     * Returns a configured FirebaseDatabase instance.
     */
    val instance: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(DATABASE_URL).apply {
            // Principle: Performance - Enable offline persistence to reduce network usage for repeated reads.
            setPersistenceEnabled(true)

            // Principle: Performance - Configure cache size to ensure efficient memory and disk usage.
            // Setting to 10MB is usually sufficient for a campus dining app's metadata.
            setPersistenceCacheSizeBytes(10 * 1024 * 1024)
        }
    }
}
