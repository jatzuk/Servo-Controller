package dev.jatzuk.servocontroller.mvp.devicesFragment.available

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.connection.receiver.BluetoothScanningReceiver
import dev.jatzuk.servocontroller.databinding.LayoutLottieAnimationViewButtonBinding
import dev.jatzuk.servocontroller.other.ACCESS_FINE_LOCATION_REQUEST_CODE
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

class AvailableDevicesFragmentPresenter @Inject constructor(
    private var view: AvailableDevicesFragmentContract.View?,
    private val connection: Connection,
    private val bluetoothScanningReceiver: BluetoothScanningReceiver
) : AvailableDevicesFragmentContract.Presenter, DevicesAdapter.OnSelectedDeviceClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var availableDevicesAdapter: DevicesAdapter
    private lateinit var lav: LottieAnimationView
    private lateinit var button: MaterialButton
    private var isSearching: Boolean = false

    private var previouslySelectedPosition = -1
    private var selectedItem = 0

    override fun onViewCreated(layoutScanAvailableDevices: LayoutLottieAnimationViewButtonBinding) {
        layoutScanAvailableDevices.apply {
            lav.visibility = View.GONE
            this@AvailableDevicesFragmentPresenter.lav = lav

            button.text = (view as Fragment).requireContext().getString(R.string.scan_devices)
            this@AvailableDevicesFragmentPresenter.button = button
        }
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        val devicesAdapter = DevicesAdapter(this).also {
            availableDevicesAdapter = it
        }
        recyclerView.apply {
            adapter = devicesAdapter
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
        }

        registerReceiver()
    }

    override fun onClick(position: Int) {
        val prevItem = recyclerView.findViewHolderForAdapterPosition(previouslySelectedPosition)
        prevItem?.let {
            if (previouslySelectedPosition != position && previouslySelectedPosition > -1) {
                (it as DevicesAdapter.ViewHolder).reset()
            }
        }

        selectedItem = position
        (recyclerView.findViewHolderForAdapterPosition(position) as DevicesAdapter.ViewHolder).setSelectedColor()
        (connection as BluetoothConnection).setDevice(availableDevicesAdapter.currentList[position])
        previouslySelectedPosition = position
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        (view as Fragment).requireContext()
            .registerReceiver(bluetoothScanningReceiver, intentFilter)
    }

    override fun onScanAvailableDevicesPressed() {
        val context = (view as Fragment).requireContext()
        isSearching = if (!isSearching) {
            checkPermission()
            view?.apply {
                showAnimation(R.raw.animation_bluetooth_scan, 1f)
                updateButton(context.getString(R.string.cancel))
            }
            bluetoothScanningReceiver.clearList()
            true
        } else {
            view?.apply {
                stopAnimation()
                updateButton(context.getString(R.string.scan_devices))
            }
            connection.stopScan()
            false
        }
    }

    override fun permissionGranted() {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> connection.startScan()
            ConnectionType.WIFI -> Unit //(connection as WifiConnection).getAvailableDevices() // TODO: 15/09/2020 wifi
        }
    }

    override fun getAvailableDevices() = bluetoothScanningReceiver.availableDevices

    override fun permissionDenied() {
        view?.apply {
            showToast(
                (view as Fragment).requireContext()
                    .getString(R.string.enable_connection_module_info)
            )
            stopAnimation()
        }
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

    override fun onDestroy() {
        (view as Fragment).requireContext().unregisterReceiver(bluetoothScanningReceiver)
        view = null
    }
}
