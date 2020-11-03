package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver
import dev.jatzuk.servocontroller.mvp.homeFragment.ConnectionStrategy
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.util.*

private const val UUIDString = "00001101-0000-1000-8000-00805f9b34fb"
private const val SCAN_TIMEOUT = 10_000L

private var socket: BluetoothSocket? = null

class BluetoothConnection : Connection {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLEScanner = bluetoothAdapter?.bluetoothLeScanner
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Timber.d("onScanResult: $result")
            result?.let {
                (receiver as BluetoothReceiver).availableDevices.value!!.add(it.device)
            }
        }
    }
    private var isBluetoothLEMode = false

    private var device: BluetoothDevice? = null

    override var receiver: BroadcastReceiver? = BluetoothReceiver(this)

    override val selectedDevice: Parcelable?
        get() = try {
            RemoteDevice.device as BluetoothDevice?
        } catch (e: ClassCastException) {
            null
        }

    override val connectionStrategy = ConnectionStrategy()

    private val _isScanning = MutableLiveData(false)
    override val isScanning: LiveData<Boolean> get() = _isScanning

    private var connectionTimeoutJob: CompletableJob? = null

    override val connectionState = MutableLiveData(
        when (bluetoothAdapter?.state) {
            BluetoothAdapter.STATE_ON -> ConnectionState.ON
            else -> ConnectionState.OFF
        }
    )

    override fun checkIfPreviousDeviceStored(context: Context) {
        val pair = RemoteDevice.loadFromSharedPreferences(context)
        if (selectedDevice == null) {
            pair?.let {
                val bonded = getBondedDevices()
                if (!bonded.isNullOrEmpty()) {
                    for (dev in bonded) {
                        if ((dev as BluetoothDevice).address == it.second) {
                            setDevice(dev)
                            break
                        }
                    }
                }
            }
        } else {
            device = selectedDevice as BluetoothDevice
            // we have active connection socket -> notify observer(presenter) for ui sync update
            if (isConnected()) connectionState.postValue(ConnectionState.CONNECTED)
        }
    }

    override fun setDevice(device: Parcelable) {
        this.device = device as BluetoothDevice
        RemoteDevice.device = device
    }

    override fun getSelectedDeviceCredentials() = (selectedDevice as BluetoothDevice?)?.let {
        it.name to it.address
    }

    override fun isConnected() = try {
        device?.let {
            val method = it.javaClass.getMethod("isConnected")
            method.invoke(it) as Boolean && socket != null
        }
    } catch (e: IllegalStateException) {
        Timber.e(e, "isConnected: illegal state exception")
        false
    } ?: false

    override fun isConnectionTypeSupported() = bluetoothAdapter != null

    override fun isHardwareEnabled() = bluetoothAdapter?.isEnabled ?: false

    override fun getBondedDevices() = bluetoothAdapter?.bondedDevices?.toList() as List<Parcelable>?

    override fun startScan() {
        if (bluetoothLEScanner != null && isBluetoothLEMode) startLEScan()
        else startDefaultScan()
    }

    private fun startDefaultScan() {
        if (!isScanning.value!!) {
            connectionTimeoutJob = Job()
            CoroutineScope(Dispatchers.IO + connectionTimeoutJob!!).launch {
                delay(SCAN_TIMEOUT)
                stopScan()
            }
            (receiver as BluetoothReceiver?)?.clearAvailableDevices()
            bluetoothAdapter?.startDiscovery()
            _isScanning.postValue(true)
        } else {
            stopScan()
        }
    }

    private fun startLEScan() {
        if (!_isScanning.value!!) {
            connectionTimeoutJob = Job()
            CoroutineScope(Dispatchers.IO + connectionTimeoutJob!!).launch {
                delay(SCAN_TIMEOUT)
                stopLEScan()
            }
            (receiver as BluetoothReceiver?)?.clearAvailableDevices()
            bluetoothLEScanner!!.startScan(leScanCallback)
            _isScanning.postValue(true)
        } else {
            stopLEScan()
        }
    }

    private fun stopLEScan() {
        bluetoothLEScanner?.stopScan(leScanCallback)
        _isScanning.postValue(false)
        connectionTimeoutJob?.let {
            if (it.isActive) it.cancel()
        }
    }

    override fun stopScan() {
        bluetoothAdapter?.cancelDiscovery()
        _isScanning.postValue(false)
        connectionTimeoutJob?.let {
            if (it.isActive) it.cancel()
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
            Timber.d("got output stream")
            true
        } catch (e: IOException) {
            Timber.e(e, "Failed to connect")
            connectionState.postValue(ConnectionState.DISCONNECTED)
            false
        }
    }

    override fun send(data: ByteArray) = try {
        socket?.outputStream!!.write(data)
        true
    } catch (e: IOException) {
        Timber.e(e, "Error occurred when sending data")
        CoroutineScope(Dispatchers.IO).launch {
            disconnect()
        }
        false
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            connectionState.postValue(ConnectionState.DISCONNECTING)
            socket?.close()
            socket = null
            true
        } catch (e: IOException) {
            Timber.e(e, "Could not close the client socket")
            false
        } finally {
            connectionState.postValue(ConnectionState.DISCONNECTED)
        }
    }

    override fun getConnectionType() = ConnectionType.BLUETOOTH

    fun isSelectedDevicePaired() =
        getBondedDevices()?.contains(RemoteDevice.device as BluetoothDevice?) ?: false

    fun changeBluetoothMode() {
        isBluetoothLEMode = !isBluetoothLEMode
    }

    private fun isBluetoothLEModeAvailable() = bluetoothLEScanner != null

    override fun isAdditionalModeSupported() = isBluetoothLEModeAvailable()

    @Suppress("UNCHECKED_CAST")
    override fun getAvailableDevices(): LiveData<List<Parcelable>> {
        val devices = (receiver as BluetoothReceiver).availableDevices
        return devices as LiveData<List<Parcelable>>
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
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Receiver deregistration failed")
        }
    }
}
