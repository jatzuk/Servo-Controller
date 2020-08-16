package dev.jatzuk.servocontroller.mvp.homefragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.fragment.app.Fragment
import dev.jatzuk.servocontroller.bluetooth.BluetoothConnection
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.ui.ServoSetupDialog

class HomeFragmentPresenter(
    private var view: HomeFragmentContract.View?
) : HomeFragmentContract.Presenter {

    private val bluetoothConnection = BluetoothConnection()

    override fun isBluetoothSupported() = bluetoothConnection.bluetoothAdapter != null

    override fun isBluetoothEnabled() = bluetoothConnection.bluetoothAdapter!!.isEnabled

    override fun requestBluetoothIfNeeded() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        (view as Fragment).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    override fun onServoSettingsTapped() {
        showSetupDialog()
    }

    override fun sendCommand(data: ByteArray) {
        bluetoothConnection.sendData(data)
    }

    override fun buildDeviceList() {
        bluetoothConnection.buildDeviceList()
    }

    override fun disconnect() {
        bluetoothConnection.disconnect()
    }

    override fun onDestroy() {
        view = null
    }

    private fun showSetupDialog() {
        ServoSetupDialog().show((view as Fragment).parentFragmentManager, "ServoSetupDialog")
    }
}
