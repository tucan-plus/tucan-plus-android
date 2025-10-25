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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// https://kotlinlang.org/docs/cancellation-and-timeouts.html#closing-resources-with-finally

data class RegisteredService(
    val nsdManager: NsdManager,
    val listener: NsdManager.RegistrationListener,
    val serviceInfo: NsdServiceInfo
) : Closeable {

    override fun close() {
        nsdManager.unregisterService(listener)
    }
}

suspend fun NsdManager.awaitRegisterService(serviceInfo: NsdServiceInfo): RegisteredService =
    suspendCancellableCoroutine { continuation ->
        val registrationListener = object : NsdManager.RegistrationListener {

            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                Log.d(TAG, "onServiceRegistered $nsdServiceInfo")
                continuation.resume(
                    RegisteredService(
                        this@awaitRegisterService,
                        this,
                        nsdServiceInfo
                    )
                ) { cause, resourceToClose, context ->
                    unregisterService(this)
                }
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.d(TAG, "onRegistrationFailed $serviceInfo $errorCode")
                continuation.resumeWithException(Exception("$serviceInfo registration failed with error code $errorCode"))
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Log.d(TAG, "onServiceUnregistered $arg0")
                continuation.cancel(Exception("onServiceUnregistered $arg0"))
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.d(TAG, "onUnregistrationFailed $serviceInfo $errorCode")
                continuation.resumeWithException(Exception("$serviceInfo unregistration failed with error code $errorCode"))
            }
        }
        registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        continuation.invokeOnCancellation {
            unregisterService(registrationListener)
        }
    }

// TODO important: https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#(kotlinx.coroutines.flow.Flow).asLiveData(kotlin.coroutines.CoroutineContext,%20kotlin.Long)
fun NsdManager.registerAndDiscoverServicesFlow(
    serviceInfo: NsdServiceInfo,
    serviceType: String
): Flow<List<NsdServiceInfo>> = callbackFlow {
    val ourServiceInfo = awaitRegisterService(serviceInfo)
    val discoveryListener = object : NsdManager.DiscoveryListener {
        var discoveredServices: List<NsdServiceInfo> = listOf()

        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "onDiscoveryStarted $regType")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "onServiceFound $service")
            when {
                service.serviceType == SERVICE_TYPE && service.serviceName != ourServiceInfo.serviceInfo.serviceName -> {
                    discoveredServices = discoveredServices + service;
                    Log.w(TAG, "updating flow")
                    trySendBlocking(discoveredServices)
                    Log.w(TAG, "updated flow")
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.d(TAG, "onServiceLost $service")
            discoveredServices = discoveredServices - service;
            trySendBlocking(discoveredServices)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG, "onDiscoveryStopped $serviceType")
            channel.close()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.d(TAG, "onStartDiscoveryFailed $serviceType $errorCode")
            cancel(CancellationException("Starting discovery failed for $serviceType: Error code: $errorCode"))
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.d(TAG, "onStopDiscoveryFailed $serviceType $errorCode")
            cancel(CancellationException("Stopping discovery failed for $serviceType: Error code: $errorCode"))
        }
    }
    discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    awaitClose {
        stopServiceDiscovery(discoveryListener)
        ourServiceInfo.close()
    }
}

suspend fun NsdManager.resolveService(serviceInfo: NsdServiceInfo): NsdServiceInfo = suspendCancellableCoroutine { continuation ->
    val listener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(
            serviceInfo: NsdServiceInfo?,
            errorCode: Int
        ) {
            Log.d(TAG, "onResolveFailed $serviceInfo $errorCode")
            continuation.resumeWithException(Exception("Resolve failed for $serviceInfo with error code $errorCode"))
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            Log.d(TAG, "onServiceResolved")
            continuation.resume(serviceInfo!!)
        }
    }

    // Official Fairphone 3 OS does not support the new registerServiceInfoCallback API
    @Suppress("DEPRECATION")
    resolveService(serviceInfo, listener)
}