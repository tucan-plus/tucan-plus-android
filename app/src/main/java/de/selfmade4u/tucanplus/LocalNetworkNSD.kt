package de.selfmade4u.tucanplus

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.SERVICE_TYPE
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.TAG
import de.selfmade4u.tucanplus.ext.awaitRegisterService
import de.selfmade4u.tucanplus.ext.registerAndDiscoverServicesFlow
import de.selfmade4u.tucanplus.ext.resolveService
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.ServerSocket
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
        val serverSocket = ServerSocket(0)
        serviceName = "TucanPlus ${Uuid.random()}"
        serviceType = SERVICE_TYPE
        port = serverSocket.localPort
        embeddedServer(CIO)

    }, SERVICE_TYPE) };
    val discovered by flow.collectAsStateWithLifecycle(listOf())
    val coroutineScope = rememberCoroutineScope()
    Column() {
        discovered.forEach { peer ->
            key(peer.serviceName) {
                var currentPeer by remember { mutableStateOf(peer) }
                Text("$currentPeer", modifier = Modifier
                    .clickable(enabled = true) {
                        coroutineScope.launch {
                            currentPeer = nsdManager.resolveService(peer)
                            // name host port and network are safe
                            val port: Int = currentPeer.port
                            val host: InetAddress = currentPeer.host
                        }
                    })
            }
        }
    }
}