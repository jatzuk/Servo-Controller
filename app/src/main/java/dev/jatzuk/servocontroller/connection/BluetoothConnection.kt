package dev.jatzuk.servocontroller.connection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import androidx.core.content.ContextCompat
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

class BluetoothConnection(context: Context) : Connection {

    private val applicationContext = context.applicationContext
    private val bluetoothManager =
        applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLEScanner
        get() = if (hasScanPermission()) bluetoothAdapter?.bluetoothLeScanner else null
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

    override val connectionState = MutableLiveData(currentConnectionState())

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

    @SuppressLint("MissingPermission")
    override fun getSelectedDeviceCredentials(): Pair<String, String>? {
        if (!hasConnectPermission()) return null
        return (selectedDevice as BluetoothDevice?)?.let { it.name to it.address }
    }

    @SuppressLint("MissingPermission")
    override fun isConnected(): Boolean {
        if (!hasConnectPermission()) return false
        return try {
            device?.let {
                val method = it.javaClass.getMethod("isConnected")
                method.invoke(it) as Boolean && socket != null
            } ?: false
        } catch (e: ReflectiveOperationException) {
            Timber.e(e, "isConnected: reflection failed")
            false
        } catch (e: IllegalStateException) {
            Timber.e(e, "isConnected: illegal state")
            false
        } catch (e: SecurityException) {
            Timber.e(e, "isConnected: Bluetooth permission unavailable")
            false
        }
    }

    override fun isConnectionTypeSupported() = bluetoothAdapter != null

    @SuppressLint("MissingPermission")
    override fun isHardwareEnabled(): Boolean {
        if (!hasConnectPermission()) return false
        return bluetoothAdapter?.isEnabled ?: false
    }

    @SuppressLint("MissingPermission")
    override fun getBondedDevices(): List<Parcelable>? {
        if (!hasConnectPermission()) return emptyList()
        return bluetoothAdapter?.bondedDevices?.toList()
    }

    override fun startScan() {
        if (bluetoothLEScanner != null && isBluetoothLEMode) startLEScan()
        else startDefaultScan()
    }

    @SuppressLint("MissingPermission")
    private fun startDefaultScan() {
        if (!hasScanPermission()) return
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

    @SuppressLint("MissingPermission")
    private fun startLEScan() {
        if (!hasScanPermission()) return
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

    @SuppressLint("MissingPermission")
    private fun stopLEScan() {
        if (hasScanPermission()) bluetoothLEScanner?.stopScan(leScanCallback)
        _isScanning.postValue(false)
        connectionTimeoutJob?.let {
            if (it.isActive) it.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        if (hasScanPermission()) bluetoothAdapter?.cancelDiscovery()
        _isScanning.postValue(false)
        connectionTimeoutJob?.let {
            if (it.isActive) it.cancel()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("MissingPermission")
    override suspend fun connect() = withContext(Dispatchers.IO) {
        if (!hasConnectPermission()) return@withContext false
        try {
            val selectedDevice = device ?: return@withContext false
            socket = selectedDevice.createInsecureRfcommSocketToServiceRecord(
                UUID.fromString(UUIDString)
            )
            if (hasScanPermission()) bluetoothAdapter?.cancelDiscovery()
            connectionState.postValue(ConnectionState.CONNECTING)
            socket?.connect()
            connectionState.postValue(ConnectionState.CONNECTED)
            Timber.d("got output stream")
            true
        } catch (e: IOException) {
            Timber.e(e, "Failed to connect")
            connectionState.postValue(ConnectionState.DISCONNECTED)
            false
        } catch (e: SecurityException) {
            Timber.e(e, "Bluetooth permission unavailable while connecting")
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

    @SuppressLint("MissingPermission")
    fun isSelectedDevicePaired(): Boolean {
        if (!hasConnectPermission()) return false
        val device = RemoteDevice.device as? BluetoothDevice ?: return false
        return getBondedDevices()?.contains(device) ?: false
    }

    fun changeBluetoothMode() {
        isBluetoothLEMode = !isBluetoothLEMode
    }

    private fun isBluetoothLEModeAvailable() = bluetoothLEScanner != null

    override fun isAdditionalModeSupported() = isBluetoothLEModeAvailable()

    @SuppressLint("MissingPermission")
    fun refreshConnectionState() {
        connectionState.value = currentConnectionState()
    }

    fun hasConnectionPermission() = hasConnectPermission()

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

    @SuppressLint("MissingPermission")
    private fun currentConnectionState(): ConnectionState {
        if (!hasConnectPermission()) return ConnectionState.OFF
        return when (bluetoothAdapter?.state) {
            BluetoothAdapter.STATE_ON -> ConnectionState.ON
            else -> ConnectionState.OFF
        }
    }

    private fun hasConnectPermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

    private fun hasScanPermission() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        else -> true
    }
}
