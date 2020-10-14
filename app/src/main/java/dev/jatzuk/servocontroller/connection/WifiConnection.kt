package dev.jatzuk.servocontroller.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.connection.receiver.WifiReceiver
import dev.jatzuk.servocontroller.mvp.homeFragment.ConnectionStrategy
import kotlinx.coroutines.*

private const val TAG = "WifiConnection"
private const val SCAN_TIMEOUT = 10_000L

class WifiConnection(private val context: Context) : Connection {

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    val wifiP2pManager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    var channel: WifiP2pManager.Channel? = null

    override val connectionState = MutableLiveData(
        if (isHardwareEnabled()) ConnectionState.ON
        else ConnectionState.OFF
    )

    override val selectedDevice: Parcelable?
        get() = try {
            RemoteDevice.device as WifiP2pDevice?
        } catch (e: ClassCastException) {
            null
        }

    override val connectionStrategy = ConnectionStrategy()

    override var receiver: BroadcastReceiver? = null

    private val wifiP2pActionListener = object : WifiP2pManager.ActionListener {

        override fun onSuccess() {
//            _isScanning.postValue(true)
            isWifiP2pDirectModeEnabled = true
        }

        override fun onFailure(reason: Int) {
            _isScanning.postValue(false)
            isWifiP2pDirectModeEnabled = false
        }
    }

    private var connectionTimeoutJob: CompletableJob? = null

    private val _isScanning = MutableLiveData(false)
    override val isScanning: LiveData<Boolean> get() = _isScanning

    private var isWifiP2pMode = false
    private var isWifiP2pDirectModeEnabled = false // todo return to false

    init {
        channel = wifiP2pManager?.initialize(context, Looper.getMainLooper(), null)
        channel?.also {
            receiver = WifiReceiver(this, wifiManager, wifiP2pManager!!, it)
        }
    }

    override fun isConnected(): Boolean {
        return false //TODO("Not yet implemented")
    }

    override fun isConnectionTypeSupported(): Boolean {
//        val pm = context.packageManager
//        val features = pm.systemAvailableFeatures
//        for (feature in features) {
//            feature?.let {
//                if (it.name?.toLowerCase(Locale.getDefault()) == "android.hardware.wifi.direct") {
//                    return true
//                }
//            }
//        }

        return wifiManager.isP2pSupported
    }

    override fun isHardwareEnabled() = wifiManager.isWifiEnabled

    override suspend fun connect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun send(data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
//        TODO("Not yet implemented")
        channel?.close()
        return false
    }

    override fun getConnectionType() = ConnectionType.WIFI

    fun getBondedDevices() = MutableList<BluetoothDevice?>(0) { null } // fixme

    override fun getAvailableDevices(): LiveData<ArrayList<Parcelable>> {
        val wifiReceiver = (receiver as WifiReceiver)
        val devices =
            if (isWifiP2pMode) wifiReceiver.availableP2PDevices
            else wifiReceiver.availableWifiAccessPoints


        return devices as LiveData<ArrayList<Parcelable>>
    }

    override fun startScan() {
        if (isWifiP2pDirectModeEnabled && isWifiP2pMode) startWifiP2pScan()
        else startDefaultScan()
    }

    @SuppressLint("MissingPermission")
    private fun startWifiP2pScan() {
        if (!isScanning.value!!) {
            connectionTimeoutJob = Job()
            CoroutineScope(Dispatchers.IO + connectionTimeoutJob!!).launch {
                delay(SCAN_TIMEOUT)
                stopWifiP2pScan()
            }
            (receiver as WifiReceiver?)?.clearAvailableP2PDevices()
            wifiP2pManager?.discoverPeers(channel, wifiP2pActionListener)
            _isScanning.postValue(true)
        } else {
            stopWifiP2pScan()
        }
    }

    private fun stopWifiP2pScan() {
        wifiP2pManager?.stopPeerDiscovery(channel, wifiP2pActionListener)
        _isScanning.postValue(false)
    }

    @SuppressLint("MissingPermission")
    private fun startDefaultScan() {
        if (!isScanning.value!!) {
            connectionTimeoutJob = Job()
            CoroutineScope(Dispatchers.IO + connectionTimeoutJob!!).launch {
                delay(SCAN_TIMEOUT)
                stopDefaultScan()
            }
            (receiver as WifiReceiver?)?.clearAvailableWifiAccessPoints()
            wifiManager.startScan()
            _isScanning.postValue(true)
        } else {
            stopDefaultScan()
        }
    }

    private fun stopDefaultScan() {
        _isScanning.postValue(false)
    }

    override fun stopScan() {
        _isScanning.postValue(false)
        connectionTimeoutJob?.let {
            if (it.isActive) it.cancel()
        }
    }

    fun changeWifiMode() {
        isWifiP2pMode = !isWifiP2pMode
    }

    fun isWifiP2pModeEnabled() = isWifiP2pDirectModeEnabled

    override fun registerReceiver(context: Context) {
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        }
        context.registerReceiver(receiver, intentFilter)
    }

    override fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(receiver)
    }

    override fun checkIfPreviousDeviceStored(context: Context) {
//        TODO("Not yet implemented")

        val pair = RemoteDevice.loadFromSharedPreferences(context)
        if (selectedDevice == null) {
            pair?.let {
//                val bonded =
            }
        }
    }

    override fun getSelectedDeviceCredentials() = (selectedDevice as WifiP2pDevice?)?.let {
        it.deviceName to it.deviceAddress
    }
}
