package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.other.SELECTED_DEVICE_DATA_EXTRA
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME

interface Connection {

    val connectionState: MutableLiveData<ConnectionState>
    var selectedDevice: Any?

    suspend fun connect(): Boolean

    fun send(data: ByteArray): Boolean

    suspend fun disconnect(): Boolean

    fun isConnected(): Boolean

    fun isConnectionTypeSupported(): Boolean

    fun isHardwareEnabled(): Boolean

    fun getConnectionType(): ConnectionType

    fun startScan()

    fun stopScan()

    fun retrieveSelectedDeviceInfo(): Pair<String, String>? {
        val selectedDevice = when (getConnectionType()) {
            ConnectionType.BLUETOOTH -> ((this as BluetoothConnection?)?.selectedDevice as BluetoothDevice?)
            ConnectionType.WIFI -> null
        }

        return selectedDevice?.let {
            it.name to it.address
        }
    }

    fun retrieveSelectedDeviceInfoFromSharedPreferences(context: Context): Pair<String, String>? {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val deviceString = sharedPreferences.getString(SELECTED_DEVICE_DATA_EXTRA, null)
        return deviceString?.let {
            val list = it.split('~')
            list[0] to list[1]
        }
    }
}

enum class ConnectionState {
    ON, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, OFF
}

enum class ConnectionType {
    BLUETOOTH, WIFI
}
