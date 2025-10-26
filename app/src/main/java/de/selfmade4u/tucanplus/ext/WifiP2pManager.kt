package de.selfmade4u.tucanplus.ext

import android.net.nsd.NsdServiceInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo
import android.util.Log
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.TAG
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// https://developer.android.com/develop/connectivity/wifi/nsd-wifi-direct#kotlin
// https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager#isWiFiDirectR2Supported()

// TODO FIXME deduplicate the ActionListener stuff in the method later in this file
suspend fun WifiP2pManager.addLocalService(channel: WifiP2pManager.Channel, serviceInfo: WifiP2pServiceInfo): Closeable = suspendCancellableCoroutine { continuation ->
    val listener = object : WifiP2pManager.ActionListener {
        override fun onFailure(reason: Int) {
            Log.d(TAG, "addLocalService onFailure $reason")
            continuation.resumeWithException(Exception("addLocalService failed with reason $reason"))
        }

        override fun onSuccess() {
            Log.d(TAG, "addLocalService onSuccess")
            val self = this
            continuation.resume(object : Closeable {
                override fun close() {
                    removeLocalService(channel, serviceInfo, self)
                }
            })
        }
    }
    addLocalService(channel, serviceInfo, listener)
    continuation.invokeOnCancellation {
        removeLocalService(channel, serviceInfo, listener)
    }
}

fun WifiP2pManager.setDnsSdResponseListenersFlow(channel: WifiP2pManager.Channel, request: WifiP2pDnsSdServiceRequest): Flow<List<WifiP2pDevice>> = callbackFlow {
    var discoveredServices: List<WifiP2pDevice> = listOf()
    setDnsSdResponseListeners(channel,
        { instanceName, registrationType, srcDevice ->
            Log.d(TAG, "setDnsSdResponseListenersFlow $instanceName $registrationType $srcDevice")
            discoveredServices = discoveredServices + srcDevice;
            trySendBlocking(discoveredServices) }
    ) { fullDomainName, txtRecordMap, srcDevice ->
        Log.d(TAG, "setDnsSdResponseListenersFlow $fullDomainName $txtRecordMap $srcDevice")
        discoveredServices = discoveredServices + srcDevice;
        trySendBlocking(discoveredServices)
    }
    val serviceRequest = addServiceRequest(channel, request)
    val discovery = discoverServices(channel)
    awaitClose {
        serviceRequest.close()
        discovery.close()
    }
}

/** Specify what we want to discover */
suspend fun WifiP2pManager.addServiceRequest(channel: WifiP2pManager.Channel, request: WifiP2pDnsSdServiceRequest): Closeable = suspendCancellableCoroutine { continuation ->
    val listener = object : WifiP2pManager.ActionListener {
        override fun onFailure(reason: Int) {
            Log.d(TAG, "addServiceRequest onFailure $reason")
            continuation.resumeWithException(Exception("addServiceRequest failed with reason $reason"))
        }

        override fun onSuccess() {
            Log.d(TAG, "addServiceRequest onSuccess")
            val self = this
            continuation.resume(object : Closeable {
                override fun close() {
                    removeServiceRequest(channel, request, self)
                }
            })
        }
    }
    addServiceRequest(channel, request, listener)
    continuation.invokeOnCancellation {
        removeServiceRequest(channel, request, listener)
    }
}

/** Discover stuff */
suspend fun WifiP2pManager.discoverServices(channel: WifiP2pManager.Channel): Closeable = suspendCancellableCoroutine { continuation ->
    discoverServices(channel, object : WifiP2pManager.ActionListener {
        override fun onFailure(reason: Int) {
            Log.d(TAG, "discoverServices onFailure $reason")
            continuation.resumeWithException(Exception("discoverServices failed with reason $reason"))
        }

        override fun onSuccess() {
            Log.d(TAG, "discoverServices onSuccess")
            continuation.resume(object : Closeable {
                override fun close() {
                    clearServiceRequests(channel, object : WifiP2pManager.ActionListener {
                        override fun onFailure(reason: Int) {

                        }

                        override fun onSuccess() {

                        }
                    })
                }
            })
        }
    })
    continuation.invokeOnCancellation {
        clearServiceRequests(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {

            }

            override fun onSuccess() {

            }
        })
    }
}