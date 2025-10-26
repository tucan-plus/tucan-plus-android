package de.selfmade4u.tucanplus.localfirst

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import de.selfmade4u.tucanplus.localfirst.LocalNetworkNSD.Companion.SERVICE_TYPE
import de.selfmade4u.tucanplus.localfirst.LocalNetworkNSD.Companion.TAG
import de.selfmade4u.tucanplus.ext.registerAndDiscoverServicesFlow
import de.selfmade4u.tucanplus.ext.resolveService
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.net.ConnectException
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
    val nsdManager = remember { getSystemService(context, NsdManager::class.java)!! }
    val flow: Flow<List<NsdServiceInfo>> = remember {
        flow {
            val info = NsdServiceInfo().apply {
                serviceName = "T+ ${Uuid.random()}"
                serviceType = SERVICE_TYPE
                val server = embeddedServer(CIO, port = 0, watchPaths = listOf()) {
                    routing {
                        get("/") {
                            Log.d(TAG, "Responding to someone")
                            call.respondText("Hello, world!")
                            Log.d(TAG, "Responded to someone")
                        }
                    }
                }.startSuspend(false)
                port = server.engine.resolvedConnectors().first().port
                Log.d(TAG, "our port $port")
            }
            emitAll(nsdManager.registerAndDiscoverServicesFlow(info, SERVICE_TYPE))
        }
    }
    val discovered by flow.collectAsStateWithLifecycle(listOf())
    val coroutineScope = rememberCoroutineScope()
    Column {
        Text("Local Network")
        discovered.forEach { peer ->
            key(peer.serviceName) {
                var currentPeer by remember { mutableStateOf(peer) }
                Text(
                    "${currentPeer.serviceName}", modifier = Modifier
                        .clickable(enabled = true) {
                            coroutineScope.launch {
                                currentPeer = nsdManager.resolveService(peer)
                                @Suppress("DEPRECATION")
                                // Official Fairphone 3 OS does not support the new registerServiceInfoCallback API
                                val host = currentPeer.host
                                val port: Int = currentPeer.port
                                val client = HttpClient()
                                try {
                                    Log.d(TAG, "Trying to connect to http://$host:$port/")
                                    val response = client.get("http://$host:42/")
                                    Log.d(TAG, "Got response $response ${response.bodyAsText()}")
                                    Toast.makeText(
                                        context,
                                        "Got response $response ${response.bodyAsText()}",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                } catch (e: ConnectException) {
                                    Log.e("TucanLogin", "ConnectException ${e.suppressedExceptions}", e)
                                    Toast.makeText(
                                        context,
                                        "ConnectException http://$host:$port/ $e",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        })
            }
        }
    }
}