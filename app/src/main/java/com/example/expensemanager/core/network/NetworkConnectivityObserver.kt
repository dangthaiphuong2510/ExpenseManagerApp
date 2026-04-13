package com.example.expensemanager.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton // Thêm import này

@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        trySend(getCurrentStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectivityObserver.Status.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(ConnectivityObserver.Status.Losing)
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityObserver.Status.Lost)
            }

            override fun onUnavailable() {
                trySend(ConnectivityObserver.Status.Unavailable)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun getCurrentStatus(): ConnectivityObserver.Status {
        val network = connectivityManager.activeNetwork ?: return ConnectivityObserver.Status.Unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectivityObserver.Status.Unavailable
        
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        return if (hasInternet) ConnectivityObserver.Status.Available
        else ConnectivityObserver.Status.Unavailable
    }
}