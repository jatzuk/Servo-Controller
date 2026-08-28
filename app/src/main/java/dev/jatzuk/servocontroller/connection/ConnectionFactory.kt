package dev.jatzuk.servocontroller.connection

import android.content.Context

object ConnectionFactory {

    private var bluetoothConnection: BluetoothConnection? = null
    private var wifiConnection: WifiConnection? = null

    fun getConnection(context: Context, connectionType: ConnectionType) = when (connectionType) {
        ConnectionType.BLUETOOTH -> {
            if (bluetoothConnection == null) {
                bluetoothConnection = BluetoothConnection(context.applicationContext)
                wifiConnection = null
            }
            bluetoothConnection!!
        }
        ConnectionType.WIFI -> {
            if (wifiConnection == null) {
                wifiConnection = WifiConnection(context.applicationContext)
                bluetoothConnection = null
            }
            wifiConnection!!
        }
    }
}
