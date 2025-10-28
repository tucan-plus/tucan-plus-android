package de.selfmade4u.tucanplus.localfirst

import android.net.wifi.p2p.WifiP2pManager
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
import de.selfmade4u.tucanplus.ext.connect
import de.selfmade4u.tucanplus.ext.connectionStateFlow
import de.selfmade4u.tucanplus.ext.discoverPeers
import de.selfmade4u.tucanplus.ext.isWifiDirectSupported
import de.selfmade4u.tucanplus.ext.peersFlow
import de.selfmade4u.tucanplus.ext.wifiP2pState
import de.selfmade4u.tucanplus.localfirst.LocalNetworkNSD.Companion.TAG
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

// https://developer.android.com/develop/connectivity/wifi/wifip2p
// https://developer.android.com/develop/connectivity/wifi/wifi-direct
// https://developer.android.com/develop/connectivity/wifi/wifi-permissions


@Composable
fun WifiDirect() {
    val context = LocalContext.current
    val manager: WifiP2pManager = remember {
        ContextCompat.getSystemService(context, WifiP2pManager::class.java)!!
    }
    val channel = remember {
        manager.initialize(context, Looper.getMainLooper(), {
            Log.d(TAG, "CHANNEL LOST");
            Toast.makeText(context, "Channel LOST", Toast.LENGTH_LONG).show()
            // TODO try reaquire
        })
    }
    val enabledFlow = wifiP2pState(context)
    val enabled by enabledFlow.collectAsStateWithLifecycle(false)
    val deviceFlow = remember {
        flow {
            // TODO use this for bonjour and this together
            if (!isWifiDirectSupported(context)) {
                Toast.makeText(context, "Wifi Direct not supported", Toast.LENGTH_SHORT).show()
                // return
            }
            val discovery = manager.discoverPeers(channel)
            val peers = manager.peersFlow(context, channel)
            emitAll(peers)
            discovery.close()
        }
    }
    val discovered by deviceFlow.collectAsStateWithLifecycle(listOf())
    val connectionStateFlow = remember { manager.connectionStateFlow(context, channel) }
    val connectionState by connectionStateFlow.collectAsStateWithLifecycle(null)
    val coroutineScope = rememberCoroutineScope()
    Column {
        Text("enabled $enabled")
        Text("Wifi Direct")
        Text("$connectionState")
        discovered.forEach { peer ->
            key(peer.deviceName) {
                var currentPeer by remember { mutableStateOf(peer) }
                Text(
                    currentPeer.deviceName ?: currentPeer.toString(), modifier = Modifier.Companion
                        .clickable(enabled = true) {
                            coroutineScope.launch {
                                manager.connect(channel, currentPeer)
                            }
                        })
            }
        }
    }
}