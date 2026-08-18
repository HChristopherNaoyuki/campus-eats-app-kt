package com.example.campus_eats_app_kt.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * NetworkConnectivityManager provides synchronous checks for internet availability.
 */
class NetworkConnectivityManager(private val context: Context)
{
    /**
     * Checks if the device has an active internet connection.
     */
    fun hasInternetConnection(): Boolean
    {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Throws an Exception if no internet connection is available.
     * Prevents silent failures for online-dependent operations.
     */
    fun ensureInternet()
    {
        if (!hasInternetConnection())
        {
            throw Exception("No internet connection available. Please check your network settings.")
        }
    }
}
