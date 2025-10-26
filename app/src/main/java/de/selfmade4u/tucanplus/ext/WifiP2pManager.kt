package de.selfmade4u.tucanplus.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo
import android.util.Log
import android.widget.Toast
import de.selfmade4u.tucanplus.localfirst.LocalNetworkNSD.Companion.TAG
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
suspend fun WifiP2pManager.addLocalService(
    channel: WifiP2pManager.Channel,
    serviceInfo: WifiP2pServiceInfo
): Closeable = suspendCancellableCoroutine { continuation ->
    val listener = object : WifiP2pManager.ActionListener {
        override fun onFailure(reason: Int) {
            Log.d(TAG, "addLocalService onFailure $reason")
            continuation.resumeWithException(Exception("addLocalService failed with reason $reason"))
        }

        override fun onSuccess() {
            Log.d(TAG, "addLocalService onSuccess")
            continuation.resume(object : Closeable {
                override fun close() {
                    removeLocalService(
                        channel,
                        serviceInfo,
                        object : WifiP2pManager.ActionListener {
                            override fun onFailure(reason: Int) {
                                Log.d(TAG, "removeLocalService onFailure $reason")
                            }

                            override fun onSuccess() {
                                Log.d(TAG, "removeLocalService onSuccess")
                            }
                        })
                }
            })
        }
    }
    addLocalService(channel, serviceInfo, listener)
    continuation.invokeOnCancellation {
        removeLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                Log.d(TAG, "removeLocalService onFailure $reason")
            }

            override fun onSuccess() {
                Log.d(TAG, "removeLocalService onSuccess")
            }
        })
    }
}

fun WifiP2pManager.setDnsSdResponseListenersFlow(
    channel: WifiP2pManager.Channel,
    request: WifiP2pDnsSdServiceRequest
): Flow<List<WifiP2pDevice>> = callbackFlow {
    var discoveredServices: List<WifiP2pDevice> = listOf()
    setDnsSdResponseListeners(
        channel,
        { instanceName, registrationType, srcDevice ->
            Log.d(TAG, "setDnsSdResponseListenersFlow $instanceName $registrationType $srcDevice")
            discoveredServices = discoveredServices + srcDevice;
            trySendBlocking(discoveredServices)
        }
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
suspend fun WifiP2pManager.addServiceRequest(
    channel: WifiP2pManager.Channel,
    request: WifiP2pDnsSdServiceRequest
): Closeable = suspendCancellableCoroutine { continuation ->
    val listener = object : WifiP2pManager.ActionListener {
        override fun onFailure(reason: Int) {
            Log.d(TAG, "addServiceRequest onFailure $reason")
            continuation.resumeWithException(Exception("addServiceRequest failed with reason $reason"))
        }

        override fun onSuccess() {
            Log.d(TAG, "addServiceRequest onSuccess")
            continuation.resume(object : Closeable {
                override fun close() {
                    removeServiceRequest(channel, request, object : WifiP2pManager.ActionListener {
                        override fun onFailure(reason: Int) {
                            Log.d(TAG, "removeServiceRequest onFailure $reason")
                        }

                        override fun onSuccess() {
                            Log.d(TAG, "removeServiceRequest onSuccess")
                        }

                    })
                }
            })
        }
    }
    addServiceRequest(channel, request, listener)
    continuation.invokeOnCancellation {
        removeServiceRequest(channel, request, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                Log.d(TAG, "removeServiceRequest onFailure $reason")
            }

            override fun onSuccess() {
                Log.d(TAG, "removeServiceRequest onSuccess")
            }

        })
    }
}

/** Discover stuff */
suspend fun WifiP2pManager.discoverServices(channel: WifiP2pManager.Channel): Closeable =
    suspendCancellableCoroutine { continuation ->
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
                                Log.d(TAG, "clearServiceRequests onFailure $reason")
                            }

                            override fun onSuccess() {
                                Log.d(TAG, "clearServiceRequests onSuccess")
                            }
                        })
                    }
                })
            }
        })
        continuation.invokeOnCancellation {
            clearServiceRequests(channel, object : WifiP2pManager.ActionListener {
                override fun onFailure(reason: Int) {
                    Log.d(TAG, "clearServiceRequests onFailure $reason")
                }

                override fun onSuccess() {
                    Log.d(TAG, "clearServiceRequests onSuccess")
                }
            })
        }
    }

fun isWifiDirectSupported(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)

fun wifiP2pState(context: Context) = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action!!) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    when (state) {
                        WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                            trySendBlocking(true)
                        }

                        WifiP2pManager.WIFI_P2P_STATE_DISABLED -> {
                            trySendBlocking(false)
                        }
                    }
                }
            }
        }

    }
    context.registerReceiver(receiver, IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
    })
    awaitClose {
        context.unregisterReceiver(receiver)
    }
}

suspend fun WifiP2pManager.discoverPeers(channel: WifiP2pManager.Channel) : Closeable =
    suspendCancellableCoroutine { continuation ->
        discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                Log.d(TAG, "discoverPeers onFailure $reason")
                when (reason) {
                    WifiP2pManager.ERROR -> {
                        Log.e(TAG, "Failure to discover peers: ERROR")
                    }
                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        Log.e(TAG, "Failure to discover peers: P2P_UNSUPPORTED")
                    }
                    WifiP2pManager.BUSY -> {
                        Log.e(TAG, "Failure to discover peers: BUSY")
                    }
                }
                continuation.resumeWithException(Exception("discoverPeers failed with reason $reason"))
            }

            override fun onSuccess() {
                Log.d(TAG, "discoverPeers onSuccess")
                continuation.resume(object : Closeable {
                    override fun close() {
                        stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
                            override fun onFailure(reason: Int) {
                                Log.d(TAG, "stopPeerDiscovery onFailure $reason")
                            }

                            override fun onSuccess() {
                                Log.d(TAG, "stopPeerDiscovery onSuccess")
                            }
                        })
                    }
                })
            }
        })
        continuation.invokeOnCancellation {
            stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
                override fun onFailure(reason: Int) {
                    Log.d(TAG, "stopPeerDiscovery onFailure $reason")
                }

                override fun onSuccess() {
                    Log.d(TAG, "stopPeerDiscovery onSuccess")
                }
            })
        }
    }

// TODO reduce code duplication
fun WifiP2pManager.peersFlow(context: Context, channel: WifiP2pManager.Channel): Flow<List<WifiP2pDevice>> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action!!) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    requestPeers(channel) { peers: WifiP2pDeviceList? ->
                        trySendBlocking(peers!!.deviceList.toList())
                    }
                }
            }
        }

    }
    context.registerReceiver(receiver, IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
    })
    awaitClose {
        context.unregisterReceiver(receiver)
    }
}

suspend fun WifiP2pManager.connect(channel: WifiP2pManager.Channel, device: WifiP2pDevice) = suspendCancellableCoroutine { continuation ->
val config = WifiP2pConfig().apply {
        deviceAddress = device.deviceAddress
        wps.setup = WpsInfo.PBC
    }
    connect(channel, config, object : WifiP2pManager.ActionListener {
        override fun onFailure(reason: Int) {
            Log.d(TAG, "connect onFailure $reason")
            continuation.resumeWithException(Exception("discoverServices failed with reason $reason"))
        }

        override fun onSuccess() {
            Log.d(TAG, "connect onSuccess")
            continuation.resume(Unit)
        }

    })
    continuation.invokeOnCancellation {
        cancelConnect(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                Log.d(TAG, "cancelConnect onFailure $reason")
            }

            override fun onSuccess() {
                Log.d(TAG, "cancelConnect onSuccess")
            }
        })
    }
}