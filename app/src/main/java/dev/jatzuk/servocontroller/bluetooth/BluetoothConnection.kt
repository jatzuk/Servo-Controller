package dev.jatzuk.servocontroller.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

private const val TAG = "BTConnectionService"
private const val UUIDString = "00001101-0000-1000-8000-00805f9b34fb"

class BluetoothConnection {

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null

    fun buildDeviceList() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        pairedDevices?.forEach {
            Log.d(TAG, "buildDeviceList: ${it.name} ${it.address} ${it.bluetoothClass}")
            if (it.address == "98:D3:41:F9:79:F6") {
                Log.d(TAG, "device found: ${it.name} ${it.address} ${it.bluetoothClass}")
                val device = bluetoothAdapter?.getRemoteDevice(it.address)
                connect(device!!)
                return@forEach
            }
        }
    }

    fun sendData(byteArray: ByteArray) {
        try {
            socket?.outputStream!!.write(byteArray)
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun connect(device: BluetoothDevice) {
        socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUIDString))
        bluetoothAdapter?.cancelDiscovery()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                socket?.connect()
                Log.d(TAG, "got output stream")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to connect", e)
            }
        }
    }
}
