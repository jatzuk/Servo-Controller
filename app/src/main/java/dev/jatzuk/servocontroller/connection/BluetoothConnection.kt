package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

private const val TAG = "BluetoothConnection"
private const val UUIDString = "00001101-0000-1000-8000-00805f9b34fb"

class BluetoothConnection : Connection {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var device: BluetoothDevice? = null

    override val connectionState = MutableLiveData(ConnectionState.OFF)

    init {
        Companion.connection = this
    }

    fun setDevice(device: BluetoothDevice) {
        this.device = device
    }

    override fun isConnected() = try {
        device?.let {
            val method = it.javaClass.getMethod("isConnected")
            method.invoke(device) as Boolean
        }
    } catch (e: IllegalStateException) {
        Log.e(TAG, "isConnected: illegal state exception", e)
        false
    } ?: false

    override fun isConnectionTypeSupported() = bluetoothAdapter != null

    override fun isHardwareEnabled() = bluetoothAdapter?.isEnabled ?: false

    fun buildDeviceList() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        pairedDevices?.forEach {
            Log.d(TAG, "buildDeviceList: ${it.name} ${it.address} ${it.bluetoothClass}")
            if (it.address == "98:D3:41:F9:79:F6") {
                Log.d(TAG, "device found: ${it.name} ${it.address} ${it.bluetoothClass}")
                val device = bluetoothAdapter?.getRemoteDevice(it.address)
                setDevice(device!!)
                return@forEach
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun connect() = withContext(Dispatchers.IO) {
        socket = device!!.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUIDString))
        bluetoothAdapter?.cancelDiscovery()
        try {
            connectionState.postValue(ConnectionState.CONNECTING)
            socket?.connect()
            connectionState.postValue(ConnectionState.CONNECTED)
            Log.d(TAG, "got output stream")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to connect", e)
            connectionState.postValue(ConnectionState.DISCONNECTED)
            false
        }
    }

    override fun send(data: ByteArray) = try {
        socket?.outputStream!!.write(data)
        true
    } catch (e: IOException) {
        Log.e(TAG, "Error occurred when sending data", e)
        false
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            connectionState.postValue(ConnectionState.DISCONNECTING)
            socket?.close()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
            false
        } finally {
            connectionState.postValue(ConnectionState.DISCONNECTED)
        }
    }

    override fun getConnectionType() = ConnectionType.BLUETOOTH

    companion object {

        lateinit var connection: BluetoothConnection
    }
}
