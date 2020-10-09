package dev.jatzuk.servocontroller.mvp.devicesFragment.available

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.AbstractAdapter
import dev.jatzuk.servocontroller.adapters.ParcelableDevicesAdapter
import dev.jatzuk.servocontroller.connection.*
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver
import dev.jatzuk.servocontroller.databinding.LayoutLottieAnimationViewButtonBinding
import dev.jatzuk.servocontroller.other.ACCESS_FINE_LOCATION_REQUEST_CODE
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

private const val TAG = "AvailableDevicesFragmen"

class AvailableDevicesFragmentPresenter @Inject constructor(
    private var view: AvailableDevicesFragmentContract.View?,
    private val connection: Connection,
) : AvailableDevicesFragmentContract.Presenter, AbstractAdapter.OnSelectedDeviceClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var availableDevicesAdapter: AbstractAdapter<out Parcelable>
    private lateinit var lav: LottieAnimationView
    private lateinit var button: MaterialButton

    override fun onViewCreated(layoutScanAvailableDevices: LayoutLottieAnimationViewButtonBinding) {
        layoutScanAvailableDevices.apply {
            lav.visibility = View.GONE
            this@AvailableDevicesFragmentPresenter.lav = lav

            button.text = (view as Fragment).requireContext().getString(R.string.scan_devices)
            this@AvailableDevicesFragmentPresenter.button = button
        }

        val availableDevices = when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> getAvailableDevices<BluetoothDevice>()
            ConnectionType.WIFI -> getAvailableDevices<WifiP2pDevice>()
        }

        availableDevices.observe((view as Fragment).viewLifecycleOwner) {
            recyclerView.updateAdapterDataSet(it)
        }

        when (connection) {
            is BluetoothConnection -> observeBluetoothConnection()
            is WifiConnection -> observeWifiConnection()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> RecyclerView.updateAdapterDataSet(devices: List<T>) {
        (adapter as AbstractAdapter<*>).apply {
            submitList(devices as List<Nothing>?)
            notifyDataSetChanged()
        }
    }

    private fun observeBluetoothConnection() {
        val fragment = (view as Fragment)
        (connection as BluetoothConnection).isScanning.observe(fragment.viewLifecycleOwner) {
            if (it) {
                view?.apply {
                    showAnimation(R.raw.bluetooth_scan)
                    updateButton(fragment.requireContext().getString(R.string.cancel))
                }
            } else {
                view?.apply {
                    stopAnimation()
                    updateButton(
                        fragment.requireContext().getString(R.string.scan_devices)
                    )
                }
            }
        }
    }

    private fun observeWifiConnection() {
        val fragment = (view as Fragment)
        (connection as WifiConnection).isScanning.observe(fragment.viewLifecycleOwner) {
            if (it) {
                view?.apply {
                    showAnimation(R.raw.bluetooth_scan) // TODO: 07/10/2020 wifi
                    updateButton(fragment.requireContext().getString(R.string.cancel))
                }
            } else {
                view?.apply {
                    stopAnimation()
                    updateButton(
                        fragment.requireContext().getString(R.string.scan_devices)
                    )
                }
            }
        }
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView

        val devicesAdapter = ParcelableDevicesAdapter(connection.getConnectionType(), this)
        availableDevicesAdapter = devicesAdapter
        recyclerView.apply {
            adapter = devicesAdapter
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))

            selectedItemPosition.value?.let {
                post {
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(it)
                    (viewHolder as AbstractAdapter.AbstractDeviceViewHolder<*>?)?.setSelectedColor()
                }
            }
        }

        registerReceiver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> {
                if ((connection as BluetoothConnection).isBluetoothLEModeAvailable()) {
                    inflater.inflate(R.menu.bluetooth_scan_menu, menu)
                }
            }
            ConnectionType.WIFI -> {
                if ((connection as WifiConnection).isWifiP2pModeEnabled()) {
                    inflater.inflate(R.menu.wifi_scan_menu, menu)
                }
            }
        }
    }

    override fun onClick(position: Int) {
        when (connection) {
            is BluetoothConnection -> {
                val device = availableDevicesAdapter.currentList[position] as BluetoothDevice
                val bluetoothReceiver = connection.receiver as BluetoothReceiver

                if (device.bondState == BluetoothDevice.BOND_NONE) {
                    bluetoothReceiver.isPairingProcess.observe((view as Fragment).viewLifecycleOwner) {
                        it?.let {
                            if (it) {
                                previouslySelectedItemPosition.value?.let { prevIndex ->
                                    rebindItemAt(prevIndex)
                                }
                                view?.showAnimation(R.raw.bluetooth_pairing)
                            } else {
                                val itemView =
                                    recyclerView.findViewHolderForAdapterPosition(position)
                                (itemView as ParcelableDevicesAdapter.ParcelableViewHolder).bind(
                                    device
                                )
                                updateSelectedItem(position)
                                view?.stopAnimation()

                                bluetoothReceiver.isPairingProcess
                                    .removeObservers((view as Fragment).viewLifecycleOwner)
                                bluetoothReceiver.isPairingProcess.postValue(null)
                            }
                        } ?: view?.stopAnimation()
                    }
                    device.createBond()
                } else {
                    previouslySelectedItemPosition.value?.let {
                        val prevItem = recyclerView.findViewHolderForAdapterPosition(it)
                        prevItem?.let { viewHolder ->
                            if (previouslySelectedItemPosition.value!! != position) {
                                (viewHolder as ParcelableDevicesAdapter.ParcelableViewHolder).reset()
                            }
                        }
                    }

                    updateSelectedItem(position)
                }
            }
            is WifiConnection -> {
                previouslySelectedItemPosition.value?.let {
                    val prevItem = recyclerView.findViewHolderForAdapterPosition(it)
                    prevItem?.let { viewHolder ->
                        if (previouslySelectedItemPosition.value!! != position) {
                            (viewHolder as ParcelableDevicesAdapter.ParcelableViewHolder).reset()
                        }
                    }
                }

                selectedItemPosition.value = position
                val viewHolder =
                    (recyclerView.findViewHolderForAdapterPosition(position) as ParcelableDevicesAdapter.ParcelableViewHolder)
                viewHolder.setSelectedColor()
                previouslySelectedItemPosition.value = position
            }
        }
    }

    private fun updateSelectedItem(position: Int) {
        selectedItemPosition.value = position
        val viewHolder =
            (recyclerView.findViewHolderForAdapterPosition(position) as ParcelableDevicesAdapter.ParcelableViewHolder)
        viewHolder.setSelectedColor()
        (connection as BluetoothConnection).setDevice(availableDevicesAdapter.currentList[position] as BluetoothDevice)
        previouslySelectedItemPosition.value = position
    }

    private fun rebindItemAt(position: Int) {
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)
        val previouslySelectedDevice = availableDevicesAdapter.currentList[position]
        (itemView as ParcelableDevicesAdapter.ParcelableViewHolder).bind(previouslySelectedDevice as BluetoothDevice)
    }

    private fun registerReceiver() {
        connection.registerReceiver((view as Fragment).requireContext())
    }

    override fun onConnectionIconPressed() {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> (connection as BluetoothConnection).changeBluetoothMode()
            ConnectionType.WIFI -> (connection as WifiConnection).changeWifiMode()
        }
    }

    override fun onScanAvailableDevicesPressed() {
        checkPermission()
    }

    override fun permissionGranted() {
        connection.startScan()
    }

    override fun <T> getAvailableDevices(): LiveData<ArrayList<T>> =
        connection.getAvailableDevices()

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

    override fun onStop() {
        RemoteDevice.writeToSharedPreferences((view as Fragment).requireContext(), connection)
    }

    override fun onDestroy() {
        connection.unregisterReceiver((view as Fragment).requireContext())
        view = null
    }

    companion object {

        private val previouslySelectedItemPosition = MutableLiveData<Int>(null)
        private val selectedItemPosition = MutableLiveData<Int>(null)
    }
}
