package dev.jatzuk.servocontroller.connection

object ConnectionFactory {

    fun getConnection(connectionType: ConnectionType): Connection = when (connectionType) {
        ConnectionType.BLUETOOTH -> BluetoothConnection()
        ConnectionType.WIFI -> WifiConnection()
    }
}
