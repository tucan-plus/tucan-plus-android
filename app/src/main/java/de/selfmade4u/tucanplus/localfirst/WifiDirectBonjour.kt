package de.selfmade4u.tucanplus.localfirst

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Looper
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.selfmade4u.tucanplus.ext.addLocalService
import de.selfmade4u.tucanplus.ext.setDnsSdResponseListenersFlow
import de.selfmade4u.tucanplus.localfirst.LocalNetworkNSD.Companion.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi


// TODO add one without service discovery
// TODO add one with the other service discovery (needs to be able to work in parallel with the others)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun WifiDirectBonjour() {
    // TODO FIXMe check if available
    val context = LocalContext.current
    val flow: Flow<List<WifiP2pDevice>> = remember {
        flow {
            val manager: WifiP2pManager =
                ContextCompat.getSystemService(context, WifiP2pManager::class.java)!!
            val channel = manager.initialize(context, Looper.getMainLooper(), {
                Log.d(TAG, "CHANNEL LOST");
                Toast.makeText(context, "Channel LOST", Toast.LENGTH_LONG).show()
                // TODO try reaquire
            })
            val record: Map<String, String> = mapOf(
                "listenport" to 42.toString(),
                "buddyname" to "John Doe${(Math.random() * 1000).toInt()}",
                "available" to "visible"
            )
            val serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(
                    "_test${(Math.random() * 1000).toInt()}",
                    "_presence._tcp",
                    record
                )
            manager.addLocalService(channel, serviceInfo)
            emitAll(
                manager.setDnsSdResponseListenersFlow(
                    channel,
                    WifiP2pDnsSdServiceRequest.newInstance()
                )
            )
        }
    }
    val discovered by flow.collectAsStateWithLifecycle(listOf())
    val coroutineScope = rememberCoroutineScope()
    Column {
        Text("Wifi Direct Bonjour")
        discovered.forEach { peer ->
            key(peer.deviceName) {
                var currentPeer by remember { mutableStateOf(peer) }
                Text(
                    currentPeer.deviceName ?: currentPeer.toString(), modifier = Modifier.Companion
                        .clickable(enabled = true) {
                            coroutineScope.launch {

                            }
                        })
            }
        }
    }
}

// https://stackoverflow.com/questions/79666219/using-a-flow-to-observe-the-onreceive-of-a-broadcastreceiver-and-emit-some-value
// https://stackoverflow.com/questions/78850026/how-to-convert-java-swing-events-to-kotlin-flow-with-maximal-performance/78850740#78850740

// 02:15:b4:00:00:00 Android_apnG found at (2), so it seems they can see each other at least in one direction? Interesting