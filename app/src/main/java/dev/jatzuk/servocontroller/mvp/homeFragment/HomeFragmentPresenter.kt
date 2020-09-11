package dev.jatzuk.servocontroller.mvp.homeFragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.ServoAdapter
import dev.jatzuk.servocontroller.connection.*
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver
import dev.jatzuk.servocontroller.db.ServoDAO
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.other.ServoTexture
import dev.jatzuk.servocontroller.other.WriteMode
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.ServoSetupDialog
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import dev.jatzuk.servocontroller.utils.SettingsHolder
import kotlinx.coroutines.*
import javax.inject.Inject

private const val TAG = "HomeFragmentPresenter"
private const val IS_CONNECTION_ACTIVE_EXTRA = "IS_CONNECTION_ACTIVE_EXTRA"

class HomeFragmentPresenter @Inject constructor(
    private var view: HomeFragmentContract.View?,
    var settingsHolder: SettingsHolder,
    var connection: Connection,
    private val servoDAO: ServoDAO,
    private val bluetoothReceiver: BluetoothReceiver
) : HomeFragmentContract.Presenter {

    private val servos = mutableListOf<Servo>()

    private lateinit var connectionJob: CompletableJob
    private var isWasConnected = false

    override fun optionsMenuCreated() {
        if (!isConnectionTypeSupported()) {
            // TODO: 16/08/20 disable bluetooth views and functionality
            val message = "${settingsHolder.connectionType} module is not found on this device"
            Log.d(TAG, message)
            view?.apply {
                showToast(message)
                updateConnectionMenuIconVisibility(false)
            }
        }
        view?.updateConnectionStateIcon(getIconBasedOnConnectionType())
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = ServoAdapter(settingsHolder.texture)
            layoutManager = getRecyclerViewLayoutManager()
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
            setHasFixedSize(true)
        }
    }

    override fun getRecyclerViewLayoutManager(): RecyclerView.LayoutManager {
        val context = (view as HomeFragment).requireContext()
        val orientation = context.resources.configuration.orientation
        return when (settingsHolder.texture) {
            ServoTexture.TEXTURE -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (settingsHolder.servosCount < 3) LinearLayoutManager(context)
                    else GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                } else {
                    if (settingsHolder.servosCount < 2) LinearLayoutManager(context)
                    else GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                }
            }
            ServoTexture.SEEKBAR -> {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                } else {
                    LinearLayoutManager(context)
                }
            }
        }
    }

    override fun onViewCreated() {
        updateServoList()

        if (isConnectionTypeSupported()) {
            val context = (view as HomeFragment).requireContext()
            connection.connectionState.observe((view as HomeFragment).viewLifecycleOwner) {
                Log.d(TAG, "notifyViewCreated: connection state: $it")
                when (it!!) {
                    ConnectionState.ON -> {
                        view?.apply {
                            updateConnectionMenuIconVisibility(true)
                            stopAnimation()
                            updateConnectionButton(context.getString(R.string.connect))
                        }
                    }
                    ConnectionState.CONNECTING -> {
                        view?.apply {
                            showAnimation(R.raw.bluetooth_loop)
                            updateConnectionButton(context.getString(R.string.cancel))
                        }
                    }
                    ConnectionState.CONNECTED -> {
                        view?.apply {
                            if (isWasConnected) {
                                setRecyclerViewVisibility(true)
                                stopAnimation()
                            } else {
                                updateConnectionStateIcon(getIconBasedOnConnectionType())
                                showAnimation(R.raw.bluetooth_connected, 1f, 1000) {
                                    setRecyclerViewVisibility(true)
                                }
                            }
                            updateConnectionButton(context.getString(R.string.disconnect), false)
                        }
                    }
                    ConnectionState.DISCONNECTING -> {
                        view?.apply {
                            setRecyclerViewVisibility(false)
                        }
                    }
                    ConnectionState.DISCONNECTED -> {
                        view?.apply {
                            updateConnectionStateIcon(getIconBasedOnConnectionType())
                            setRecyclerViewVisibility(false)
                            showAnimation(R.raw.animation_failure, 0.5f, 2500)
                            updateConnectionButton(context.getString(R.string.connect))
                        }
                    }
                    ConnectionState.OFF -> {
                        view?.apply {
                            setRecyclerViewVisibility(false)
                            updateConnectionButton(
                                context.getString(
                                    R.string.enable,
                                    connection.getConnectionType().name
                                )
                            )
                            showAnimation(R.raw.bluetooth_enable)
                        }
                    }
                }
            }
        } else {
            // TODO: 03/09/2020 module not supported
        }
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        registerBroadcastReceiver()
        isWasConnected =
            savedInstanceState?.getBoolean(IS_CONNECTION_ACTIVE_EXTRA, false) ?: isConnected()
    }

    override fun onDestroyView() {
        unregisterBroadcastReceiver()
    }

    override fun isConnectionTypeSupported() = connection.isConnectionTypeSupported()

    override fun isConnectionModuleEnabled() = connection.isHardwareEnabled()

    override fun isConnected() = connection.isConnected()

    override fun requestConnectionHardware() {
        val fragment = view as Fragment
        when (connection) {
            is BluetoothConnection -> {
                val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                fragment.startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)

            }
            is WifiConnection -> {
                // TODO: 03/09/2020 handle wifi connection request enable
//                val enableWifiIntent = Intent()
//                fragment.startActivityForResult(enableWifiIntent, REQUEST_ENABLE_WIFI)
            }
        }
    }

    override fun onServoSettingsTapped(layoutPosition: Int) {
        showSetupDialog(layoutPosition)
    }

    override fun onFinalPositionDetected(layoutPosition: Int, angle: Int) {
        val servo = servos[layoutPosition]
        val command = servo.command

        val finalAngle = when (servo.writeMode) {
            WriteMode.WRITE -> angle
            WriteMode.WRITE_MICROSECONDS -> servo.convertRange(angle)
        }

        val data = "$command$finalAngle"
        Log.d(TAG, "onFinalPositionDetected: $data")
//        connection.send(data.toByteArray())
    }

    override fun buildDeviceList() {
        (connection as BluetoothConnection).buildDeviceList()
    }

    override fun sendData(data: ByteArray) = connection.send(data)

    override fun connect() {
        connectionJob = Job()
        CoroutineScope(Dispatchers.IO + connectionJob).launch {
            buildDeviceList()
            connection.connect()
        }
    }

    override fun disconnect() {
        if (::connectionJob.isInitialized && connectionJob.isActive) {
            connectionJob.cancel()
        }

        CoroutineScope(Dispatchers.IO).launch {
            connection.disconnect()
        }
    }

    override fun onDestroy() {
        view = null
    }

    private fun showSetupDialog(layoutPosition: Int) {
        ServoSetupDialog.newInstance(layoutPosition).apply {
            val fragment = (this@HomeFragmentPresenter.view as HomeFragment)
            onClosed {
                val servo = getUpdatedServo()
                servos[layoutPosition] = servo
                fragment.submitServosList(servos.toList())

                CoroutineScope(Dispatchers.IO).launch {
                    servoDAO.insertServo(servo)
                }
            }
            show(fragment.parentFragmentManager, "ServoSetupDialog")
        }
    }

    override fun connectionIconPressed() {
        when (connection.connectionState.value) {
            ConnectionState.ON, ConnectionState.DISCONNECTED -> {
                connect()
            }
            ConnectionState.CONNECTING -> {
                disconnect()
            }
            ConnectionState.OFF -> {
                val context = (view as HomeFragment).requireContext()
                val enableString =
                    context.getString(R.string.enable, connection.getConnectionType().name)
                view?.updateConnectionButton(enableString)
                requestConnectionHardware()
            }
            ConnectionState.CONNECTED -> {
                disconnect()
            }
            else -> {
                Log.d(TAG, "connectionIconPressed: nothing to do")
            }
        }
    }

    override fun connectionButtonPressed() {
        when (connection.connectionState.value) {
            ConnectionState.ON, ConnectionState.DISCONNECTED -> {
                connect()
            }
            ConnectionState.CONNECTING -> {
                disconnect()
            }
            ConnectionState.OFF -> {
                val context = (view as HomeFragment).requireContext()
                val enableString =
                    context.getString(R.string.enable, connection.getConnectionType().name)
                view?.updateConnectionButton(enableString)
                requestConnectionHardware()
            }
            else -> {
                Log.d(TAG, "connectionButtonPressed: nothing to do")
            }
        }
    }

    override fun requestConnectionHardwareButtonPressed() {
        requestConnectionHardware()
    }

    override fun onRequestEnableHardwareReceived() {
        connection.connectionState.postValue(ConnectionState.ON)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_CONNECTION_ACTIVE_EXTRA, connection.isConnected())
    }

    fun updateConnectionType() {

    }

    private fun getIconBasedOnConnectionType() = when (connection.getConnectionType()) {
        ConnectionType.BLUETOOTH -> {
            if (connection.connectionState.value == ConnectionState.CONNECTED) R.drawable.ic_bluetooth_connected
            else R.drawable.ic_bluetooth_disabled
        }
        ConnectionType.WIFI -> {
            if (connection.connectionState.value == ConnectionState.CONNECTED) R.drawable.ic_wifi_connected
            else R.drawable.ic_wifi_disabled
        }
    }

    private fun updateServoList() {
        servos.clear()
        CoroutineScope(Dispatchers.IO).launch {
            repeat(settingsHolder.servosCount) { i ->
                val servo = servoDAO.getServoByOrder(i)
                if (servo == null) {
                    servos.add(Servo(i))
                    return@repeat
                }
                servos.add(servo)
            }
            (view as HomeFragment).submitServosList(servos.toList())
        }
    }

    private fun registerBroadcastReceiver() {
        Log.d(TAG, "registerBroadcastReceiver: registered")
        val context = (view as HomeFragment).requireContext()
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothReceiver, intentFilter)


        // notify that hardware already turned on to synchronize state
        if (connection.isHardwareEnabled()
            && connection.connectionState.value != ConnectionState.CONNECTED
        ) {
            val intent = Intent().apply {
                action = BluetoothAdapter.ACTION_STATE_CHANGED
                putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_ON)
            }
            bluetoothReceiver.onReceive(context, intent)
        }
    }

    private fun unregisterBroadcastReceiver() {
        Log.d(TAG, "unregisterBroadcastReceiver: unregistered")
        (view as HomeFragment).requireContext().unregisterReceiver(bluetoothReceiver)
    }
}
