package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.connection.ConnectionState
import dev.jatzuk.servocontroller.connection.WifiConnection
import dev.jatzuk.servocontroller.connection.receiver.BluetoothScanningReceiver
import dev.jatzuk.servocontroller.other.ACCESS_FINE_LOCATION_REQUEST_CODE
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

private const val TAG = "DevicesFragmentPretr"

class DevicesFragmentPresenter @Inject constructor(
    private var view: DevicesFragmentContract.View?,
    private val connection: Connection,
    private val bluetoothScanningReceiver: BluetoothScanningReceiver
) : DevicesFragmentContract.Presenter {

    private lateinit var pairedDevicesAdapter: DevicesAdapter
    private lateinit var availableDevicesAdapter: DevicesAdapter

    override fun onViewCreated() {
        if (connection.isConnectionTypeSupported()) {
            connection.connectionState.observe((view as Fragment).viewLifecycleOwner) {
                when (it!!) {
                    ConnectionState.ON -> {
                        view?.apply {
                            updateRecyclerViewsVisibility(true)
                            stopAnimation()
                        }
                        pairedDevicesAdapter.submitList(getPairedDevices())
                    }
                    ConnectionState.OFF -> {
                        view?.apply {
                            updateRecyclerViewsVisibility(false)
                            showAnimation(R.raw.bluetooth_enable)
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
        val fragment = view as Fragment
        when (connection) {
            is BluetoothConnection -> {
                val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                fragment.startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)
            }
            is WifiConnection -> {
                // TODO: 11/09/2020 handle wifi connection request enable
//                val enableWifiIntent = Intent()
//                fragment.startActivityForResult(enableWifiIntent, REQUEST_ENABLE_WIFI)
            }
        }
    }

    override fun onRequestEnableHardwareReceived() {
        view?.apply {
            updateRecyclerViewsVisibility(true)
            stopAnimation()
        }
        pairedDevicesAdapter.submitList(getPairedDevices())
    }

    override fun setupPairedDevicesRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = DevicesAdapter().also { pairedDevicesAdapter = it }
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
//            setHasFixedSize(true)
        }
    }

    override fun getPairedDevices() = (connection as BluetoothConnection).getBondedDevices()

    override fun setupAvailableDevicesRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = DevicesAdapter().also { availableDevicesAdapter = it }
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
//            setHasFixedSize(true)
        }

        registerReceiver()
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        (view as Fragment).requireContext()
            .registerReceiver(bluetoothScanningReceiver, intentFilter)
    }

    override fun getAvailableDevices() = bluetoothScanningReceiver.availableDevices

    override fun getAvailableBluetoothDevices() =
        (connection as BluetoothConnection).getAvailableDevices()

    override fun scanAvailableDevicesPressed() {
        checkPermission()
    }

    private fun checkPermission() {
        val fragment = view as Fragment
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION_REQUEST_CODE
            )
        } else {
            permissionGranted()
        }
    }

    override fun permissionGranted() {
        getAvailableBluetoothDevices()
    }

    override fun permissionDenied() {
        view?.showToast(
            (view as Fragment).requireContext().getString(R.string.enable_connection_module_info)
        )
    }

    override fun onFoldButtonPressedAtAvailableDevicesLayout(
        motionLayout: MotionLayout,
        imageView: ImageView
    ) {
        motionLayout.apply {
            when (progress) {
                0f -> {
                    transitionToEnd()
                    imageView.setImageResource(R.drawable.ic_unfold_less)
                }
                1f -> {
                    transitionToStart()
                    imageView.setImageResource(R.drawable.ic_unfold_more)
                }
            }
        }
    }

    override fun onDestroy() {
        (view as Fragment).requireContext().unregisterReceiver(bluetoothScanningReceiver)
        view = null
    }
}
