package de.selfmade4u.tucanplus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.registerReceiver

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
            return;
        }
        Toast.makeText(context, "Wifi Direct supported", Toast.LENGTH_SHORT).show()
        val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
            getSystemService(context, WifiP2pManager::class.java)
        }



        var channel: WifiP2pManager.Channel? = null
        var receiver: BroadcastReceiver? = null
        channel = manager?.initialize(context, Looper.getMainLooper(), null)
        channel?.also { channel ->
            receiver = WifiDirectBroadcastReceiver(manager!!, channel)
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