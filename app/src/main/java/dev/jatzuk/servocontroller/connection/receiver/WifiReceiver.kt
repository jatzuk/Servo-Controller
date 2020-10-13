package dev.jatzuk.servocontroller.connection.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.connection.ConnectionState
import dev.jatzuk.servocontroller.connection.WifiConnection

private const val TAG = "WifiReceiver"

class WifiReceiver(
    private val connection: WifiConnection,
    private val wifiManager: WifiManager,
    private val wifiP2pManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel
) : BroadcastReceiver() {

    private val _availableP2PDevices = MutableLiveData<ArrayList<WifiP2pDevice>>(ArrayList())
    val availableP2PDevices: LiveData<ArrayList<WifiP2pDevice>> get() = _availableP2PDevices

    private val _availableWifiAccessPoints = MutableLiveData<ArrayList<ScanResult>>(ArrayList())
    val availableWifiAccessPoints: LiveData<ArrayList<ScanResult>> get() = _availableWifiAccessPoints

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity

                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                when (state) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
                        Log.d(TAG, "onReceive: WIFI_P2P_STATE_ENABLED")
                        connection.connectionState.postValue(ConnectionState.ON)
//                        manager.requestPeers(channel) { peers ->
//                            Log.d(TAG, "onReceive peer: $peers")
//                        }
                    }
                    else -> {
                        // Wifi p2p is not enabled
                        Log.d(TAG, "onReceive: WIFI_P2P_STATE_DISABLED")
                        connection.connectionState.postValue(ConnectionState.OFF)
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.d(TAG, "onReceive: WIFI_P2P_PEERS_CHANGED_ACTION")
                wifiP2pManager.requestPeers(channel) { peers ->
                    val devices = peers.deviceList.toTypedArray()
                    val filtered = devices.filter { !_availableP2PDevices.value!!.contains(it) }
                    _availableP2PDevices.value?.addAll(filtered)
//                    _availableDevices.value = _availableDevices.value
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
//                connection.connectionState.postValue(ConnectionState.CONNECTED)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }

            // default wifi scan
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                Log.d(TAG, "onReceive: SCAN_RESULTS_AVAILABLE_ACTION")
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    val scanResults = wifiManager.scanResults.sortedByDescending { it.SSID }
                    val bssids = scanResults.map { it.BSSID }.toMutableSet()
                    for (scanResult in scanResults) {
                        if (scanResult.BSSID in bssids) {
                            bssids.remove(scanResult.BSSID)
                            _availableWifiAccessPoints.value?.add(scanResult)
                        }
                    }
                }
            }
        }
    }

    fun clearAvailableP2PDevices() {
        _availableP2PDevices.value!!.clear()
    }

    fun clearAvailableWifiAccessPoints() {
        _availableWifiAccessPoints.value!!.clear()
    }
}
