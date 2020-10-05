package dev.jatzuk.servocontroller.mvp.devicesFragment

import androidx.fragment.app.Fragment
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.connection.ConnectionState
import dev.jatzuk.servocontroller.connection.WifiConnection
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_WIFI
import dev.jatzuk.servocontroller.ui.MainActivity
import javax.inject.Inject

private const val TAG = "DevicesFragmentPretr"

class DevicesFragmentPresenter @Inject constructor(
    private var view: DevicesFragmentContract.View?,
    private val connection: Connection,
) : DevicesFragmentContract.Presenter {

    override fun onViewCreated() {
        if (connection.isConnectionTypeSupported()) {
            connection.connectionState.observe((view as Fragment).viewLifecycleOwner) {
                when (it!!) {
                    ConnectionState.ON -> {
                        view?.apply {
                            updateTabLayoutVisibility(true)
                            stopAnimation()
                        }
                    }
                    ConnectionState.OFF -> {
                        view?.apply {
                            updateTabLayoutVisibility(false)
                            showAnimation(R.raw.bluetooth_enable)
                        }
                    }
                    ConnectionState.CONNECTED -> {
                        // TODO: 03/10/20 show button to disconnect first, then scan
                        view?.apply {
                            updateTabLayoutVisibility(true)
                            stopAnimation()
                        }
                    }
                    else -> {
                        // TODO: 11/09/2020 handle other stuff
                    }
                }
            }
        }
    }

    override fun getConnectionType() = connection.getConnectionType()

    override fun onEnableHardwareButtonPressed() {
        val requestCode = when (connection) {
            is BluetoothConnection -> REQUEST_ENABLE_BT
            is WifiConnection -> REQUEST_ENABLE_WIFI
            else -> -1
        }
        ((view as Fragment).requireActivity() as MainActivity)
            .enableHardwareContractLauncher.launch(requestCode)
    }

    override fun onRequestEnableHardwareReceived() {
        view?.apply {
            updateTabLayoutVisibility(true)
            stopAnimation()
        }
    }

    override fun onDestroy() {
        view = null
    }
}
