package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.connection.receiver.WifiReceiver
import dev.jatzuk.servocontroller.mvp.homeFragment.ConnectionStrategy

// TODO: 17/08/2020 class to represent a wifi connection manager
class WifiConnection(context: Context) : Connection {

    override val connectionState = MutableLiveData(ConnectionState.OFF)
    override val selectedDevice: Parcelable? get() = ServerDevice.device as WifiP2pDevice?
    override val connectionStrategy = ConnectionStrategy()

    val manager:
            WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    var channel: WifiP2pManager.Channel? = null
    override var receiver: BroadcastReceiver? = null

    init {
        channel = manager?.initialize(context, Looper.getMainLooper(), null)
        channel?.also {
            receiver = WifiReceiver(manager!!, it)
        }
    }

    override fun isConnected(): Boolean {
        return false //TODO("Not yet implemented")
    }

    override fun isConnectionTypeSupported(): Boolean {
        return false
    }

    override fun isHardwareEnabled(): Boolean {
        return true //TODO("Not yet implemented")
    }

    override suspend fun connect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun send(data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getConnectionType() = ConnectionType.WIFI

    fun getBondedDevices() = MutableList<BluetoothDevice?>(0) { null } // fixme

    override fun <T> getAvailableDevices(): LiveData<ArrayList<T>> {
        TODO("Not yet implemented")
    }

    override fun startScan() {
        TODO("Not yet implemented")
    }

    override fun stopScan() {
        TODO("Not yet implemented")
    }

    override fun registerReceiver(context: Context) {
//        TODO("Not yet implemented")
    }

    override fun unregisterReceiver(context: Context) {
//        TODO("Not yet implemented")
    }

    override fun checkIfPreviousDeviceStored(context: Context) {
        TODO("Not yet implemented")
    }

    override fun getSelectedDeviceCredentials(): Pair<String, String>? {
        TODO("Not yet implemented")
    }
}
