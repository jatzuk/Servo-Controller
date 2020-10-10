package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.*
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_WIFI
import dev.jatzuk.servocontroller.ui.AvailableDevicesFragment
import dev.jatzuk.servocontroller.ui.MainActivity
import dev.jatzuk.servocontroller.ui.PairedDevicesFragment
import dev.jatzuk.servocontroller.utils.SettingsHolder
import javax.inject.Inject

private const val TAG = "DevicesFragmentPretr"

class DevicesFragmentPresenter @Inject constructor(
    private var view: DevicesFragmentContract.View?,
    private val settingsHolder: SettingsHolder,
    private var connection: Connection,
) : DevicesFragmentContract.Presenter {

    override fun createTabLayout(binding: FragmentDevicesBinding) {
        val fragment = view as Fragment

        val settingsSavedConnectionType = settingsHolder.connectionType
        if (connection.getConnectionType().name != settingsSavedConnectionType.name) {
            connection = ConnectionFactory.getConnection(
                fragment.requireContext(),
                settingsSavedConnectionType
            )
        }

        val viewPager = binding.viewPager.apply {
            adapter = FragmentAdapter(fragment, getConnectionType())
        }
        TabLayoutMediator(binding.layoutTab, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> fragment.requireContext().getString(R.string.available_devices)
                else -> fragment.requireContext().getString(R.string.paired_devices)
            }
        }.attach()
    }

    override fun onViewCreated() {
        if (connection.isConnectionTypeSupported()) {
            view?.updateButtonText(getConnectionType().name)
            connection.connectionState.observe(((view as Fragment).requireActivity())) {
                Log.d(TAG, "observed value has changed: $it")
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

class FragmentAdapter(
    fragment: Fragment,
    private val connectionType: ConnectionType
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = when (connectionType) {
        ConnectionType.BLUETOOTH -> 2
        ConnectionType.WIFI -> 1
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AvailableDevicesFragment()
        else -> PairedDevicesFragment()
    }
}
