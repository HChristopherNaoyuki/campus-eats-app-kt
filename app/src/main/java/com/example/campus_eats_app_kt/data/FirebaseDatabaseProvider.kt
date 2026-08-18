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
            // Enable persistence for offline capabilities (as per general Firebase best practices)
            // But requirement says "Prevent online operations from failing silently when offline"
            // and "require an active internet connection for features that depend on the online database"
            // So we might NOT want disk persistence if we strictly want online-only behavior.
            // However, RTDB handles sync well. We will focus on manual connectivity checks.
            setPersistenceEnabled(true)
        }
    }
}
