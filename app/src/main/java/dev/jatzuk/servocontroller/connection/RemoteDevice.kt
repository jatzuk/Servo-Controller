package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Parcelable
import dev.jatzuk.servocontroller.other.SELECTED_DEVICE_DATA_EXTRA
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME

object RemoteDevice {

    var device: Parcelable? = null

    fun writeToSharedPreferences(context: Context, connection: Connection) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isPaired = when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> {
                (connection as BluetoothConnection).isSelectedDevicePaired()
            }
            ConnectionType.WIFI -> {
                // TODO: 16/09/2020 WIFI
                false
            }
        }
        if (isPaired) { // FIXME: 21/09/2020 bluetooth only
            device?.let {
                (it as BluetoothDevice)
                val deviceString = "${it.name}~${it.address}"
                sharedPreferences.edit().putString(
                    SELECTED_DEVICE_DATA_EXTRA,
                    deviceString
                ).apply()
            }
        }
    }

    fun loadFromSharedPreferences(context: Context): Pair<String, String>? {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val deviceString = sharedPreferences.getString(SELECTED_DEVICE_DATA_EXTRA, null)
        return deviceString?.let {
            val list = it.split('~')
            list[0] to list[1]
        }
    }
}
