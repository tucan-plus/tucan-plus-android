package de.selfmade4u.tucanplus

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.SERVICE_TYPE
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.TAG
import de.selfmade4u.tucanplus.ext.awaitRegisterService
import de.selfmade4u.tucanplus.ext.registerAndDiscoverServicesFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
        const val SERVICE_TYPE = "_tucanplus._tcp." // must end with dot
    }
}

// https://developer.android.com/develop/ui/compose/state
@OptIn(ExperimentalUuidApi::class)
@Composable
fun ShowLocalServices() {
    val context = LocalContext.current
    val nsdManager = getSystemService(context, NsdManager::class.java)!!
    val flow = remember { nsdManager.registerAndDiscoverServicesFlow(NsdServiceInfo().apply {
        serviceName = "TucanPlus ${Uuid.random()}"
        serviceType = SERVICE_TYPE
        port = 42
    }, SERVICE_TYPE) };
    val discovered by flow.collectAsStateWithLifecycle(listOf())
    Column() {
        discovered.forEach { peer ->
            key(peer.serviceName) {
                Text("$peer")
            }
        }
    }
}