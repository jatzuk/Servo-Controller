package dev.jatzuk.servocontroller.mvp.devicesFragment.available

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
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
import dev.jatzuk.servocontroller.other.SELECTED_DEVICE_DATA_EXTRA
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME
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

            selectedItemPosition.value?.let {
                post {
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(it)
                    (viewHolder as DevicesAdapter.ViewHolder?)?.setSelectedColor()
                }
            }
        }

        registerReceiver()
    }

    override fun onClick(position: Int) {
        val device = availableDevicesAdapter.currentList[position]
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            bluetoothScanningReceiver.isPairingProcess.observe((view as Fragment).viewLifecycleOwner) {
                it?.let {
                    if (it) {
                        rebindItemAt(previouslySelectedItemPosition.value!!)
                        view?.showAnimation(R.raw.bluetooth_pairing)
                    } else {
                        val itemView = recyclerView.findViewHolderForAdapterPosition(position)
                        (itemView as DevicesAdapter.ViewHolder).bind(device)
                        updateSelectedItem(position)
                        view?.stopAnimation()

                        bluetoothScanningReceiver.isPairingProcess.removeObservers((view as Fragment).viewLifecycleOwner)
                        bluetoothScanningReceiver.isPairingProcess.postValue(null)
                    }
                } ?: view?.stopAnimation()
            }
            device.createBond()
        } else {
            previouslySelectedItemPosition.value?.let {
                val prevItem = recyclerView.findViewHolderForAdapterPosition(it)
                prevItem?.let { viewHolder ->
                    if (previouslySelectedItemPosition.value!! != position) {
                        (viewHolder as DevicesAdapter.ViewHolder).reset()
                    }
                }
            }

            updateSelectedItem(position)
        }
    }

    private fun updateSelectedItem(position: Int) {
        selectedItemPosition.value = position
        val viewHolder =
            (recyclerView.findViewHolderForAdapterPosition(position) as DevicesAdapter.ViewHolder)
        viewHolder.setSelectedColor()
        (connection as BluetoothConnection).setDevice(availableDevicesAdapter.currentList[position])
        previouslySelectedItemPosition.value = position
    }

    private fun rebindItemAt(position: Int) {
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)
        val previouslySelectedDevice = availableDevicesAdapter.currentList[position]
        (itemView as DevicesAdapter.ViewHolder).bind(previouslySelectedDevice)
    }

    private fun registerReceiver() {
        val context = (view as Fragment).requireContext()
        var intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(bluetoothScanningReceiver, intentFilter)
        intentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bluetoothScanningReceiver, intentFilter)
    }

    override fun onScanAvailableDevicesPressed() {
        isSearching = if (!isSearching) {
            checkPermission()
            bluetoothScanningReceiver.clearList()
            true
        } else {
            view?.apply {
                stopAnimation()
                updateButton((view as Fragment).requireContext().getString(R.string.scan_devices))
            }
            connection.stopScan()
            false
        }
    }

    override fun permissionGranted() {
        view?.apply {
            showAnimation(R.raw.bluetooth_scan)
            updateButton((view as Fragment).requireContext().getString(R.string.cancel))
        }
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
        val context = (view as Fragment).requireContext()
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val selectedDevice = connection.retrieveSelectedDeviceInfo()
        val isPaired = when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> {
                (connection as BluetoothConnection).isSelectedDevicePaired()
            }
            ConnectionType.WIFI -> {
                // TODO: 16/09/2020 WIFI
                false
            }
        }
        if (isPaired) {
            selectedDevice?.let {
                val deviceString = "${it.first}~${it.second}"
                sharedPreferences.edit().putString(
                    SELECTED_DEVICE_DATA_EXTRA,
                    deviceString
                ).apply()
            }
        }

        context.unregisterReceiver(bluetoothScanningReceiver)
        view = null
    }

    companion object {

        private val previouslySelectedItemPosition = MutableLiveData<Int>(null)
        private val selectedItemPosition = MutableLiveData<Int>(null)
    }
}
