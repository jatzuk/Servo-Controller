package dev.jatzuk.servocontroller.connection

import android.content.Context

object ConnectionFactory {

    fun getConnection(context: Context, connectionType: ConnectionType) = when (connectionType) {
        ConnectionType.BLUETOOTH -> BluetoothConnection()
        ConnectionType.WIFI -> WifiConnection(context)
    }
}
