package de.selfmade4u.tucanplus.localfirst

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.widget.Toast
import androidx.annotation.RequiresPermission

// https://developer.android.com/develop/connectivity/wifi/wifi-aware
// https://developer.android.com/develop/connectivity/bluetooth/ble/ble-overview
// https://www.ditto.com/blog/cross-platform-p2p-wi-fi-how-the-eu-killed-awdl
// https://github.com/android/platform-samples/blob/main/samples/connectivity/bluetooth/ble/src/main/java/com/example/platform/connectivity/bluetooth/ble/server/GATTServerSampleService.kt
class WifiAware {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES])
    fun setup(context: Context) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            Toast.makeText(context, "Wifi Aware not supported", Toast.LENGTH_LONG).show()
            return
        }
        val wifiAwareManager = context.getSystemService(WifiAwareManager::class.java)
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                // discard current sessions
                if (wifiAwareManager.isAvailable) {
                    Toast.makeText(context, "Wifi Aware availability changed to available", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Wifi Aware availability changed to unavailable", Toast.LENGTH_LONG).show()
                }
            }
        }
        context.registerReceiver(myReceiver, filter)
        if (!wifiAwareManager.isAvailable) {
            Toast.makeText(context, "Wifi Aware not available", Toast.LENGTH_LONG).show()
            return
        }
        wifiAwareManager.attach(object : AttachCallback() {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES])
            override fun onAttached(session: WifiAwareSession?) {
                Toast.makeText(context, "Wifi Aware attached", Toast.LENGTH_LONG).show()

                val publishConfig: PublishConfig = PublishConfig.Builder()
                    .setServiceName("tucan-plus")
                    .build()
                session!!.publish(publishConfig, object : DiscoverySessionCallback() {

                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        Toast.makeText(context, "Publication successful", Toast.LENGTH_LONG).show()

                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        Toast.makeText(context, "Message received from peer", Toast.LENGTH_LONG).show()

                    }
                }, null)

                val config: SubscribeConfig = SubscribeConfig.Builder()
                    .setServiceName("tucan-plus")
                    .build()
                session.subscribe(config, object : DiscoverySessionCallback() {

                    override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                        Toast.makeText(context, "Subscription successful", Toast.LENGTH_LONG).show()

                    }

                    override fun onServiceDiscovered(
                        peerHandle: PeerHandle,
                        serviceSpecificInfo: ByteArray,
                        matchFilter: List<ByteArray>
                    ) {
                        Toast.makeText(context, "Discovered service from peer", Toast.LENGTH_LONG).show()

                    }
                }, null)
            }
        }, object : IdentityChangedListener() {
            override fun onIdentityChanged(mac: ByteArray?) {

            }
        }, null)
    }
}