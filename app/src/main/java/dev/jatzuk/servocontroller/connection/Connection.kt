package dev.jatzuk.servocontroller.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface Connection {

    var receiver: BroadcastReceiver?

    val connectionState: MutableLiveData<ConnectionState>
    val selectedDevice: Parcelable?

    suspend fun connect(): Boolean

    fun send(data: ByteArray): Boolean

    suspend fun disconnect(): Boolean

    fun isConnected(): Boolean

    fun isConnectionTypeSupported(): Boolean

    fun isHardwareEnabled(): Boolean

    fun getConnectionType(): ConnectionType

    fun startScan()

    fun stopScan()

    fun registerReceiver(context: Context)

    fun unregisterReceiver(context: Context)

    fun <T> getAvailableDevices(): LiveData<ArrayList<T>>

    fun checkIfPreviousDeviceStored(context: Context)

    fun getSelectedDeviceCredentials(): Pair<String, String>?
}

enum class ConnectionState {
    ON, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, OFF
}

enum class ConnectionType {
    BLUETOOTH, WIFI
}
