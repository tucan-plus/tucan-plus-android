package de.selfmade4u.tucanplus.ext

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.SERVICE_TYPE
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.TAG
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resumeWithException

// https://kotlinlang.org/docs/cancellation-and-timeouts.html#closing-resources-with-finally

data class RegisteredService(val nsdManager: NsdManager, val listener: NsdManager.RegistrationListener, val serviceInfo: NsdServiceInfo) : Closeable {

    override fun close() {
        nsdManager.unregisterService(listener)
    }
}

suspend fun NsdManager.awaitRegisterService(serviceInfo: NsdServiceInfo): RegisteredService =
    suspendCancellableCoroutine { continuation ->
        val registrationListener = object : NsdManager.RegistrationListener {

            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                continuation.resume(RegisteredService(this@awaitRegisterService, this, nsdServiceInfo)) { cause, resourceToClose, context ->
                    unregisterService(this)
                }
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                continuation.resumeWithException(Exception("$serviceInfo registration failed with error code $errorCode"))
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {

            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                continuation.resumeWithException(Exception("$serviceInfo unregistration failed with error code $errorCode"))
            }
        }
        registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        continuation.invokeOnCancellation {
            unregisterService(registrationListener)
        }
    }

// TODO important: https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#(kotlinx.coroutines.flow.Flow).asLiveData(kotlin.coroutines.CoroutineContext,%20kotlin.Long)
fun NsdManager.discoverServicesFlow(serviceType: String): Flow<List<NsdServiceInfo>> = callbackFlow {
     val discoveryListener = object : NsdManager.DiscoveryListener {
         var discoveredServices: List<NsdServiceInfo> = listOf()

        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service discovery success$service")
            when {
                service.serviceType == SERVICE_TYPE -> {
                    discoveredServices = discoveredServices + service;
                    trySendBlocking(discoveredServices)
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            discoveredServices = discoveredServices - service;
            trySendBlocking(discoveredServices)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            channel.close()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            cancel(CancellationException("Starting discovery failed for $serviceType: Error code: $errorCode"))
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            cancel(CancellationException("Stopping discovery failed for $serviceType: Error code: $errorCode"))
        }
    }
    discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    awaitClose {
        stopServiceDiscovery(discoveryListener)
    }
}

//  nsdManager.resolveService(service, resolveListener)