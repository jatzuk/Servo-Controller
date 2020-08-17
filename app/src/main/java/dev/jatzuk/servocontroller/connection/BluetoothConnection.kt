package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*

private const val TAG = "BTConnectionService"
private const val UUIDString = "00001101-0000-1000-8000-00805f9b34fb"

class BluetoothConnection : Connection {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var device: BluetoothDevice? = null

    fun setDevice(device: BluetoothDevice) {
        this.device = device
    }

    override fun isConnected(): Boolean {
        Log.d(TAG, "isConnected: ${device?.bondState == BluetoothProfile.STATE_CONNECTED}")
        return device?.bondState == BluetoothProfile.STATE_CONNECTED
    }

    override fun isConnectionTypeSupported() = bluetoothAdapter != null

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
    override fun connect(): Boolean {
        socket = device!!.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUIDString))
        bluetoothAdapter?.cancelDiscovery()

        return runBlocking(Dispatchers.IO) {
            try {
                socket?.connect()
                Log.d(TAG, "got output stream")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Failed to connect", e)
                false
            }
        }
    }

    override fun send(data: ByteArray) = try {
        socket?.outputStream!!.write(data)
        true
    } catch (e: IOException) {
        Log.e(TAG, "Error occurred when sending data", e)
        false
    }

    override fun disconnect() = try {
        socket?.close()
        true
    } catch (e: IOException) {
        Log.e(TAG, "Could not close the client socket", e)
        false
    }
}
