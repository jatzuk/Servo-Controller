package dev.jatzuk.servocontroller.connection

import androidx.lifecycle.MutableLiveData

// TODO: 17/08/2020 class to represent a wifi connection manager
class WifiConnection : Connection {

    override val connectionState = MutableLiveData(ConnectionState.OFF)

    override fun isConnected(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isConnectionTypeSupported(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isHardwareEnabled(): Boolean {
        TODO("Not yet implemented")
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
}
