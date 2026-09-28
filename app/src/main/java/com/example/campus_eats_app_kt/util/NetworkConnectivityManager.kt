package com.example.campus_eats_app_kt.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * NetworkConnectivityManager provides synchronous checks for internet availability.
 * Uses validated network capability when available to reduce false positives from
 * captive portals or networks without actual internet access.
 */
class NetworkConnectivityManager(private val context: Context)
{
    /**
     * Checks if the device has an active, validated internet connection.
     * Returns true only when a network with internet capability is present.
     * Prefer NET_CAPABILITY_VALIDATED when the API level supports reliable validation.
     */
    fun hasInternetConnection(): Boolean
    {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        // VALIDATED indicates the network has successfully passed internet connectivity checks.
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return hasInternet && isValidated
    }

    /**
     * Throws an Exception if no internet connection is available.
     * Prevents silent failures for online-dependent operations such as
     * Firebase Authentication and Realtime Database writes.
     */
    fun ensureInternet()
    {
        if (!hasInternetConnection())
        {
            throw Exception("No internet connection available. Please check your network settings.")
        }
    }
}
