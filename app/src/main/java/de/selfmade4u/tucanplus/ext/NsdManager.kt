package de.selfmade4u.tucanplus.ext

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import kotlin.coroutines.resumeWithException

// https://kotlinlang.org/docs/cancellation-and-timeouts.html#closing-resources-with-finally

data class RegisteredService(val nsdManager: NsdManager, val listener: NsdManager.RegistrationListener, val serviceInfo: NsdServiceInfo) : Closeable {

    override fun close() {
        nsdManager.unregisterService(listener)
    }
}

suspend fun NsdManager.awaitRegisterService(serviceInfo: NsdServiceInfo): RegisteredService {
    return suspendCancellableCoroutine { continuation ->
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
}