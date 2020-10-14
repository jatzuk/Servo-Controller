package dev.jatzuk.servocontroller.connection

import android.content.Context

object ConnectionFactory {

    private var bluetoothConnection: BluetoothConnection? = null
    private var wifiConnection: WifiConnection? = null

    fun getConnection(context: Context, connectionType: ConnectionType) = when (connectionType) {
        ConnectionType.BLUETOOTH -> {
            if (bluetoothConnection == null) {
                bluetoothConnection = BluetoothConnection()
                wifiConnection = null
            }
            bluetoothConnection!!
        }
        ConnectionType.WIFI -> {
            if (wifiConnection == null) {
                wifiConnection = WifiConnection(context)
                bluetoothConnection = null
            }
            wifiConnection!!
        }
    }
}
