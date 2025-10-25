package de.selfmade4u.tucanplus

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import de.selfmade4u.tucanplus.ext.awaitRegisterService
import de.selfmade4u.tucanplus.ext.discoverServicesFlow

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

    suspend fun registerService(context: Context, port: Int) {
        val nsdManager = getSystemService(context, NsdManager::class.java)!!
        nsdManager.awaitRegisterService(NsdServiceInfo().apply {
            serviceName = "TucanPlus"
            serviceType = SERVICE_TYPE
            setPort(port)
        }).use { result ->
            print(result.serviceInfo.serviceName)

            nsdManager.discoverServicesFlow(SERVICE_TYPE)

        }
    }

     suspend fun setup(context: Context) {
        registerService(context, 0)
    }

}