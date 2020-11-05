package dev.jatzuk.servocontroller.mvp.devicesFragment

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
import timber.log.Timber
import javax.inject.Inject

class DevicesFragmentPresenter @Inject constructor(
    private var view: DevicesFragmentContract.View?,
    private val connection: Connection,
) : DevicesFragmentContract.Presenter {

    override fun createTabLayout(binding: FragmentDevicesBinding) {
        val fragment = view as Fragment

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
                Timber.d("observed value has changed: $it")
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
                        view?.apply {
                            updateTabLayoutVisibility(true)
                            stopAnimation()
                        }
                    }
                    else -> {
                        /* no-op */
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
