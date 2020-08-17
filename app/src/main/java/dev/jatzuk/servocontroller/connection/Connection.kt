package dev.jatzuk.servocontroller.connection

interface Connection {

    fun connect(): Boolean

    fun send(data: ByteArray): Boolean

    fun disconnect(): Boolean

    fun isConnected(): Boolean

    fun isConnectionTypeSupported(): Boolean
}
