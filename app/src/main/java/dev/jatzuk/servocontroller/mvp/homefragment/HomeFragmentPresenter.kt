package dev.jatzuk.servocontroller.mvp.homefragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
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
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT_1
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.ServoSetupDialog
import dev.jatzuk.servocontroller.utils.SettingsHolder
import javax.inject.Inject

private const val TAG = "HomeFragmentPresenter"

class HomeFragmentPresenter @Inject constructor(
    private var view: HomeFragmentContract.View?,
    var settingsHolder: SettingsHolder,
    var connection: Connection
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
        return if (servos.size > 2) GridLayoutManager((view as HomeFragment).requireContext(), 2)
        else LinearLayoutManager((view as HomeFragment).requireContext())
    }

    override fun onReadyToRequestServosList() {
        (view as HomeFragment).submitServosList(servos)
    }

    override fun notifyViewCreated() {
        Log.d(TAG, "notifyViewCreated:")
        updateServoList()
    }

    override fun isConnectionTypeSupported() = connection.isConnectionTypeSupported()

    override fun isConnected() = connection.isConnected()

    override fun requestConnectionHardware() {
        if (connection is BluetoothConnection) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            (view as Fragment).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_1)
        } else {
            // TODO: 17/08/2020 request enable wifi
        }
    }

    override fun onServoSettingsTapped() {
        showSetupDialog()
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

    private fun showSetupDialog() {
        ServoSetupDialog().show((view as Fragment).parentFragmentManager, "ServoSetupDialog")
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
        val size = settingsHolder.servosCount
        Log.d(TAG, "updateServoList: ${size}")
        repeat(size) {
            servos.add(Servo(it, "command#$it", "tag$it"))
        }
    }
}
