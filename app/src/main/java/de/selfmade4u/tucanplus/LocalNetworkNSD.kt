package de.selfmade4u.tucanplus

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import de.selfmade4u.tucanplus.ext.awaitRegisterService

// suspendCancellableCoroutine
// suspendCoroutine
// https://www.baeldung.com/kotlin/callbacks-coroutines-conversion
// https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html
// https://carrion.dev/en/posts/callback-to-flow-conversion/
// https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/ ?

// hopefully also works with the emulator
// https://developer.android.com/develop/connectivity/wifi/use-nsd
class LocalNetworkNSD {

    companion object {
        const val TAG: String = "TucanPlus"
        const val SERVICE_TYPE = "_tucanplus._tcp"
    }


    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {


        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success$service")
            when {
                service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: $mServiceName")
                service.serviceName.contains("NsdChat") -> nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    suspend fun registerService(context: Context, port: Int) {
        val nsdManager = getSystemService(context, NsdManager::class.java)!!
        nsdManager.awaitRegisterService(NsdServiceInfo().apply {
            serviceName = "TucanPlus"
            serviceType = SERVICE_TYPE
            setPort(port)
        }).use { result ->
            print(result.serviceInfo.serviceName)

            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        }
    }

     suspend fun setup(context: Context) {
        registerService(context, 0)
    }

}