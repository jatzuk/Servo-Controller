package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.wifi.ScanResult
import android.os.Parcelable
import dev.jatzuk.servocontroller.other.SELECTED_DEVICE_DATA_EXTRA
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME

object RemoteDevice {

    var device: Parcelable? = null
    private const val delimiter = '~'

    fun writeToSharedPreferences(context: Context, connection: Connection) {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> {
                val isPaired = (connection as BluetoothConnection).isSelectedDevicePaired()
                if (isPaired) processBluetoothDevice(context)
            }
            ConnectionType.WIFI -> {
                processWifiNetwork(context)
            }
        }
    }

    private fun processBluetoothDevice(context: Context) {
        device?.let {
            it as BluetoothDevice
            saveToSharedPreferences(context, it.name, it.address)
        }
    }

    private fun processWifiNetwork(context: Context) {
        device?.let {
            it as ScanResult
            saveToSharedPreferences(context, it.SSID, it.BSSID)
        }
    }

    private fun saveToSharedPreferences(
        context: Context,
        name: String,
        address: String,
    ) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(
            SELECTED_DEVICE_DATA_EXTRA,
            "$name$delimiter$address"
        ).apply()
    }

    fun loadFromSharedPreferences(context: Context): Pair<String, String>? {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val deviceString = sharedPreferences.getString(SELECTED_DEVICE_DATA_EXTRA, null)
        return deviceString?.let {
            val list = it.split(delimiter)
            list[0] to list[1]
        }
    }
}
