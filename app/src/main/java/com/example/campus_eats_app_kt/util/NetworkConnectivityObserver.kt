package com.example.campus_eats_app_kt.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * NetworkConnectivityObserver provides a reactive stream of the device's internet connectivity status.
 */
class NetworkConnectivityObserver(context: Context)
{
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Represents the possible connectivity states.
     */
    enum class Status
    {
        Available, Unavailable, Losing, Lost
    }

    /**
     * Observes changes in network connectivity.
     */
    fun observe(): Flow<Status>
    {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback()
            {
                override fun onAvailable(network: Network)
                {
                    super.onAvailable(network)
                    launch { send(Status.Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int)
                {
                    super.onLosing(network, maxMsToLive)
                    launch { send(Status.Losing) }
                }

                override fun onLost(network: Network)
                {
                    super.onLost(network)
                    launch { send(Status.Lost) }
                }

                override fun onUnavailable()
                {
                    super.onUnavailable()
                    launch { send(Status.Unavailable) }
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)

            // Initial state check
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val hasInternet =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            launch { send(if (hasInternet) Status.Available else Status.Unavailable) }

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}
