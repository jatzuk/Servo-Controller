package dev.jatzuk.servocontroller.connection

import androidx.lifecycle.MutableLiveData

interface Connection {

    val connectionState: MutableLiveData<ConnectionState>

    suspend fun connect(): Boolean

    fun send(data: ByteArray): Boolean

    suspend fun disconnect(): Boolean

    fun isConnected(): Boolean

    fun isConnectionTypeSupported(): Boolean

    fun isHardwareEnabled(): Boolean

    fun getConnectionType(): ConnectionType

    fun startScan()

    fun stopScan()
}

enum class ConnectionState {
    ON, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, OFF
}

enum class ConnectionType {
    BLUETOOTH, WIFI
}
