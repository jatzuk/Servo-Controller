package dev.jatzuk.servocontroller.mvp.homeFragment

import android.content.res.Configuration
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.ServoAdapter
import dev.jatzuk.servocontroller.connection.*
import dev.jatzuk.servocontroller.model.ServosModel
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_WIFI
import dev.jatzuk.servocontroller.other.ServoTexture
import dev.jatzuk.servocontroller.other.WriteMode
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.MainActivity
import dev.jatzuk.servocontroller.ui.ServoSetupDialog
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import dev.jatzuk.servocontroller.utils.SettingsHolder
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class HomeFragmentPresenter @Inject constructor(
    var view: HomeFragmentContract.View?,
    private val settingsHolder: SettingsHolder,
    private val servosModel: ServosModel,
    var connection: Connection
) : HomeFragmentContract.Presenter {

    private lateinit var connectionJob: CompletableJob

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = ServoAdapter(settingsHolder.texture)
            layoutManager = getRecyclerViewLayoutManager()
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
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
        val activity = (view as Fragment).requireActivity()

        if (settingsHolder.shouldKeepScreenOn) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        val settingsSavedConnectionType = settingsHolder.connectionType
        if (connection.getConnectionType().name != settingsSavedConnectionType.name) {
            connection =
                ConnectionFactory.getConnection(activity.baseContext, settingsSavedConnectionType)
        }

        if (isConnectionTypeSupported()) {
            servosModel.servos.observe((view as HomeFragment).viewLifecycleOwner) {
                view?.submitServosList(it)
            }
            servosModel.loadServosFromDB(settingsHolder.servosCount)
            registerBroadcastReceiver()
            connection.checkIfPreviousDeviceStored(activity.baseContext)
        }
    }

    override fun optionsMenuCreated() {
        if (!isConnectionTypeSupported()) {
            val message = "${settingsHolder.connectionType} module is not found on this device"
            Timber.d(message)
            view?.apply {
                showToast(message)
                updateConnectionMenuIconVisibility(false)
            }
        }
        view?.updateConnectionStateIcon(getIconBasedOnConnectionType())
    }

    override fun onStart() {
        if (isConnectionTypeSupported()) {
            connection.connectionState.observe((view as HomeFragment).viewLifecycleOwner) {
                connection.connectionStrategy.currentStrategy = when (it!!) {
                    ConnectionState.ON -> {
                        OnStrategy(this, connection.selectedDevice == null)
                    }
                    ConnectionState.CONNECTING -> {
                        ConnectingStrategy(this)
                    }
                    ConnectionState.CONNECTED -> {
                        val shouldShowConnectedAnimation =
                            connection.connectionStrategy.currentStrategy !is ConnectedStrategy
                        ConnectedStrategy(this, shouldShowConnectedAnimation)
                    }
                    ConnectionState.DISCONNECTING -> {
                        DisconnectingStrategy(this)
                    }
                    ConnectionState.DISCONNECTED -> {
                        DisconnectedStrategy(this)
                    }
                    ConnectionState.OFF -> {
                        OffStrategy(this)
                    }
                }
            }
        } else {
            connection.connectionStrategy.currentStrategy = UnsupportedConnectionTypeStrategy(this)
        }
    }

    override fun onDestroyView() {
        unregisterBroadcastReceiver()
    }

    override fun isConnectionTypeSupported() = connection.isConnectionTypeSupported()

    override fun isConnectionModuleEnabled() = connection.isHardwareEnabled()

    override fun isConnected() = connection.isConnected()

    override fun requestConnectionHardware() {
        val requestCode = when (connection) {
            is BluetoothConnection -> REQUEST_ENABLE_BT
            is WifiConnection -> REQUEST_ENABLE_WIFI
            else -> -1
        }
        ((view as Fragment).requireActivity() as MainActivity)
            .enableHardwareContractLauncher.launch(requestCode)
    }

    override fun onServoSettingsTapped(layoutPosition: Int) {
        showSetupDialog(layoutPosition)
    }

    override fun onFinalPositionDetected(layoutPosition: Int, angle: Int) {
        val servo = servosModel.getServoAt(layoutPosition)
        val command = servo.command

        val finalAngle = when (servo.writeMode) {
            WriteMode.WRITE -> angle
            WriteMode.WRITE_MICROSECONDS -> servo.convertRange(angle)
        }

        val data = "$command$finalAngle"
        connection.send(data.toByteArray())
    }

    override fun sendData(data: ByteArray) = connection.send(data)

    override fun connect() {
        if (connection.selectedDevice == null) {
            view?.navigateTo(R.id.devicesFragment)
        } else {
            connectionJob = Job()
            CoroutineScope(Dispatchers.IO + connectionJob).launch {
                connection.connect()
            }
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
                servosModel.saveServoAt(layoutPosition, servo)
                this@HomeFragmentPresenter.view?.updateDataSetAt(layoutPosition)
            }
            show(fragment.parentFragmentManager, "ServoSetupDialog")
        }
    }

    override fun connectionIconPressed() {
        when (connection.connectionState.value) {
            ConnectionState.ON, ConnectionState.DISCONNECTED -> {
                connect()
            }
            ConnectionState.CONNECTING, ConnectionState.CONNECTED -> {
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
                Timber.d("connectionIconPressed: nothing to do")
            }
        }
    }

    override fun connectionButtonPressed() {
        if (!isConnectionTypeSupported()) {
            view?.navigateTo(R.id.action_homeFragment_to_settingsFragment)
        } else {
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
                    Timber.d("connectionButtonPressed: nothing to do")
                }
            }
        }
    }

    override fun requestConnectionHardwareButtonPressed() {
        requestConnectionHardware()
    }

    override fun onRequestEnableHardwareReceived() {
        connection.connectionState.postValue(ConnectionState.ON)
    }

    fun getIconBasedOnConnectionType() = when (connection.getConnectionType()) {
        ConnectionType.BLUETOOTH -> {
            if (connection.connectionState.value == ConnectionState.CONNECTED) R.drawable.ic_bluetooth_connected
            else R.drawable.ic_bluetooth_disabled
        }
        ConnectionType.WIFI -> {
            if (connection.connectionState.value == ConnectionState.CONNECTED) R.drawable.ic_wifi_connected
            else R.drawable.ic_wifi_disabled
        }
    }

    private fun registerBroadcastReceiver() {
        connection.registerReceiver((view as HomeFragment).requireContext())
    }

    private fun unregisterBroadcastReceiver() {
        connection.unregisterReceiver((view as HomeFragment).requireContext())
    }
}
