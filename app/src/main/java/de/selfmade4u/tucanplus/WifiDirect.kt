package de.selfmade4u.tucanplus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
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
import de.selfmade4u.tucanplus.LocalNetworkNSD.Companion.TAG
import de.selfmade4u.tucanplus.ext.addLocalService
import de.selfmade4u.tucanplus.ext.resolveService
import de.selfmade4u.tucanplus.ext.setDnsSdResponseListenersFlow
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.net.ConnectException
import kotlin.uuid.ExperimentalUuidApi

class WifiDirectBroadcastReceiver(val manager: WifiP2pManager, val channel: WifiP2pManager.Channel) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String = intent!!.action!!
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                when (state) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
                        Toast.makeText(context, "Wifi Direct enabled", Toast.LENGTH_SHORT).show()
                        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

                            override fun onSuccess() {
                                Toast.makeText(context, "Discovered peers", Toast.LENGTH_SHORT).show()
                            }

                            override fun onFailure(reasonCode: Int) {
                                when (reasonCode) {
                                    WifiP2pManager.ERROR -> {
                                        Toast.makeText(context, "Failure to discover peers: ERROR", Toast.LENGTH_SHORT).show()
                                    }
                                    WifiP2pManager.P2P_UNSUPPORTED -> {
                                        Toast.makeText(context, "Failure to discover peers: P2P_UNSUPPORTED", Toast.LENGTH_SHORT).show()
                                    }
                                    WifiP2pManager.BUSY -> {
                                        Toast.makeText(context, "Failure to discover peers: BUSY", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        })
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                        Toast.makeText(context, "Wifi Direct disabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                    // Handle peers list
                    Toast.makeText(context, "Peers $peers", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}

// https://developer.android.com/develop/connectivity/wifi/wifip2p
// https://developer.android.com/develop/connectivity/wifi/wifi-direct
class WifiDirect {

    // https://developer.android.com/develop/connectivity/wifi/wifi-permissions
    fun setup(context: Context) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Toast.makeText(context, "Wifi Direct not supported", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "Wifi Direct supported", Toast.LENGTH_SHORT).show()
        val manager: WifiP2pManager = getSystemService(context, WifiP2pManager::class.java)!!

        var channel: WifiP2pManager.Channel? = null
        var receiver: BroadcastReceiver? = null
        channel = manager.initialize(context, Looper.getMainLooper(), null)
        channel?.also { channel ->
            receiver = WifiDirectBroadcastReceiver(manager, channel)
        }
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        receiver?.also { receiver ->
            // TODO FIXME unregister stuff is wrong here
            context.registerReceiver(receiver, intentFilter)
        }
    }
}


// TODO add one without service discovery
// TODO add one with the other service discovery (needs to be able to work in parallel with the others)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun WifiDirectList() {
    // TODO FIXMe check if available
    val context = LocalContext.current
    val flow: Flow<List<WifiP2pDevice>> = remember {
        flow {
            val manager: WifiP2pManager = getSystemService(context, WifiP2pManager::class.java)!!
            val channel = manager.initialize(context, Looper.getMainLooper(), null)
            val record: Map<String, String> = mapOf(
                "listenport" to 42.toString(),
                "buddyname" to "John Doe${(Math.random() * 1000).toInt()}",
                "available" to "visible"
            )
            val serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record)
            manager.addLocalService(channel, serviceInfo)
            emitAll(manager.setDnsSdResponseListenersFlow(channel, WifiP2pDnsSdServiceRequest.newInstance()))
        }
    }
    val discovered by flow.collectAsStateWithLifecycle(listOf())
    val coroutineScope = rememberCoroutineScope()
    Column {
        discovered.forEach { peer ->
            key(peer.deviceName) {
                var currentPeer by remember { mutableStateOf(peer) }
                Text(
                    "${currentPeer.deviceName}", modifier = Modifier
                        .clickable(enabled = true) {
                            coroutineScope.launch {

                            }
                        })
            }
        }
    }
}