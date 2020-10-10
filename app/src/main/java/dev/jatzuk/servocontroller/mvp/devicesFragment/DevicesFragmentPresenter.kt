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
import dev.jatzuk.servocontroller.ui.DevicesFragment
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

    override fun createTabLayoutIfNeeded(binding: FragmentDevicesBinding) {
        val fragment = view as Fragment

        val settingsSavedConnectionType = settingsHolder.connectionType
        if (connection.getConnectionType().name != settingsSavedConnectionType.name) {
            connection = ConnectionFactory.getConnection(
                fragment.requireContext(),
                settingsSavedConnectionType
            )
        }

        if (connection.getConnectionType() == ConnectionType.BLUETOOTH) {
            val viewPager = binding.viewPager.apply {
                adapter = FragmentAdapter(fragment)
            }
            TabLayoutMediator(binding.layoutTab, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> fragment.requireContext().getString(R.string.available_devices)
                    else -> fragment.requireContext().getString(R.string.paired_devices)
                }
            }.attach()
        } else {
            fragment.childFragmentManager.beginTransaction()
////            fragment.requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    binding.layoutConstraint.id,
                    AvailableDevicesFragment(),
                    "AvailableDevicesFragment"
                ).commit()
//            fragment.findNavController()
//                .navigate(R.id.action_devicesFragment_to_availableDevicesFragment)
        }
    }

    override fun onViewCreated() {
        if (connection.isConnectionTypeSupported()) {
            connection.connectionState.observe((view as DevicesFragment).viewLifecycleOwner) {
                Log.d(TAG, "onViewCreated: $it")
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

class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AvailableDevicesFragment()
        else -> PairedDevicesFragment()
    }
}
