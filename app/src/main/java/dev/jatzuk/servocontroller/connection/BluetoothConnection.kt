package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver
import dev.jatzuk.servocontroller.mvp.homeFragment.ConnectionStrategy
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

    override var receiver: BroadcastReceiver? = BluetoothReceiver(this)
    override val selectedDevice: Parcelable? get() = ServerDevice.device as BluetoothDevice?
    override val connectionStrategy = ConnectionStrategy()

    override val connectionState = MutableLiveData(
        when (bluetoothAdapter?.state) {
            BluetoothAdapter.STATE_ON -> ConnectionState.ON
            else -> ConnectionState.OFF
        }
    )

    override fun checkIfPreviousDeviceStored(context: Context) {
        val pair = ServerDevice.loadFromSharedPreferences(context)
        if (selectedDevice == null) {
            pair?.let {
                val bonded = getBondedDevices()
                if (!bonded.isNullOrEmpty()) {
                    for (dev in bonded) {
                        if (dev.address == it.second) {
                            setDevice(dev)
                            break
                        }
                    }
                }
            }
        }
    }

    fun setDevice(device: BluetoothDevice) {
        this.device = device
        ServerDevice.device = device
    }

    override fun getSelectedDeviceCredentials() = (selectedDevice as BluetoothDevice?)?.let {
        it.name to it.address
    }

    override fun isConnected() = try {
        device?.let {
            val method = it.javaClass.getMethod("isConnected")
            method.invoke(it) as Boolean
        }
    } catch (e: IllegalStateException) {
        Log.e(TAG, "isConnected: illegal state exception", e)
        false
    } ?: false

    override fun isConnectionTypeSupported() = bluetoothAdapter != null

    override fun isHardwareEnabled() = bluetoothAdapter?.isEnabled ?: false

    fun buildDeviceList() {
        getBondedDevices()?.forEach {
            Log.d(TAG, "buildDeviceList: ${it.name} ${it.address} ${it.bluetoothClass}")
            if (it.address == "98:D3:41:F9:79:F6") {
                Log.d(TAG, "device found: ${it.name} ${it.address} ${it.bluetoothClass}")
                val device = bluetoothAdapter?.getRemoteDevice(it.address)
                setDevice(device!!)
                return@forEach
            }
        }
    }

    fun getBondedDevices() = bluetoothAdapter?.bondedDevices?.toList()

    override fun startScan() {
        stopScan()
        (receiver as BluetoothReceiver?)?.clearAvailableDevices()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopScan() {
        bluetoothAdapter?.cancelDiscovery()
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

    fun isSelectedDevicePaired() =
        getBondedDevices()?.contains(ServerDevice.device as BluetoothDevice?) ?: false

    @Suppress("UNCHECKED_CAST")
    override fun <T> getAvailableDevices(): LiveData<ArrayList<T>> {
        val devices = (receiver as BluetoothReceiver).availableDevices
        return devices as LiveData<ArrayList<T>>
    }

    override fun registerReceiver(context: Context) {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED) // for homeFragment
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(receiver, intentFilter)
    }

    override fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(receiver)
    }
}
