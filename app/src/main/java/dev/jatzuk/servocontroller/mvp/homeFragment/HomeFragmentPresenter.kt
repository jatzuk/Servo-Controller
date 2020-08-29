package dev.jatzuk.servocontroller.mvp.homeFragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.db.ServoDAO
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.ServoSetupDialog
import dev.jatzuk.servocontroller.utils.SettingsHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeFragmentPresenter"

class HomeFragmentPresenter @Inject constructor(
    private var view: HomeFragmentContract.View?,
    var settingsHolder: SettingsHolder,
    var connection: Connection,
    private val servoDAO: ServoDAO
) : HomeFragmentContract.Presenter {

    private val servos = mutableListOf<Servo>()

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
        view?.updateConnectionStateIcon(getIconBasedOnConnectionState())
    }

    override fun getRecyclerViewLayoutManager(): RecyclerView.LayoutManager {
        val context = (view as HomeFragment).requireContext()
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (settingsHolder.servosCount < 3) LinearLayoutManager(context)
            else GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        } else {
            if (settingsHolder.servosCount < 2) LinearLayoutManager(context)
            else GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        }
    }

    override fun notifyViewCreated() {
        updateServoList()
    }

    override fun isConnectionTypeSupported() = connection.isConnectionTypeSupported()

    override fun isConnected() = connection.isConnected()

    override fun requestConnectionHardware() {
        if (connection is BluetoothConnection) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            (view as Fragment).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            // TODO: 17/08/2020 request enable wifi
        }
    }

    override fun onServoSettingsTapped(layoutPosition: Int) {
        showSetupDialog(layoutPosition)
    }

    override fun onFinalPositionDetected(layoutPosition: Int, position: Int) {
        val data = "#$layoutPosition$position"
        Log.d(TAG, "onFinalPositionDetected: ${servos[layoutPosition]}")
//        connection.send(data.toByteArray())
    }

    override fun buildDeviceList() {
        (connection as BluetoothConnection).buildDeviceList()
    }

    override fun sendData(data: ByteArray) = connection.send(data)

    override fun connect(): Boolean {
        // FIXME: 17/08/2020 replace with user selected device
        buildDeviceList()
        return connection.connect()
    }

    override fun disconnect(): Boolean {
        val isSuccess = connection.disconnect()
        val message = if (isSuccess) "Device disconnected" else "Failed to disconnect device"
        view?.showToast(message, Toast.LENGTH_LONG)
        return isSuccess
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
        if (!isConnected()) {
            val isSuccessConnection = connect()
            if (isSuccessConnection) {
                val message = "Device connected"
                Log.d(TAG, message)
                view?.showToast(message)
            } else {
                if (settingsHolder.connectionType == ConnectionType.BLUETOOTH) R.drawable.ic_bluetooth_disabled
                else R.drawable.ic_wifi_disabled

                val message = "Could not connect to a selected device"
                Log.d(TAG, message)
                view?.showToast(message, Toast.LENGTH_LONG)
            }
        } else {
            disconnect()
            val message = "Device disconnected"
            Log.d(TAG, message)
            view?.showToast(message)
        }

        view?.updateConnectionStateIcon(getIconBasedOnConnectionState())
    }

    override fun onBTRequestEnableReceived() {
        view?.apply {
            showToast("BT enabled")
            updateConnectionMenuIconVisibility(true)
        }
    }

    fun updateConnectionType() {

    }

    private fun getIconBasedOnConnectionState() = when (settingsHolder.connectionType) {
        ConnectionType.BLUETOOTH -> {
            if (isConnected()) R.drawable.ic_bluetooth_connected
            else R.drawable.ic_bluetooth_disabled
        }
        ConnectionType.WIFI -> {
            if (isConnected()) R.drawable.ic_wifi_connected
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
}
