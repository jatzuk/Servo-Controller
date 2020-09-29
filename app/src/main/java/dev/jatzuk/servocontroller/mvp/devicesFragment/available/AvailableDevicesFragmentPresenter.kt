package dev.jatzuk.servocontroller.mvp.devicesFragment.available

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
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
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.connection.ServerDevice
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver
import dev.jatzuk.servocontroller.databinding.LayoutLottieAnimationViewButtonBinding
import dev.jatzuk.servocontroller.other.ACCESS_FINE_LOCATION_REQUEST_CODE
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

class AvailableDevicesFragmentPresenter @Inject constructor(
    private var view: AvailableDevicesFragmentContract.View?,
    private val connection: Connection,
) : AvailableDevicesFragmentContract.Presenter, DevicesAdapter.OnSelectedDeviceClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var availableDevicesAdapter: DevicesAdapter
    private lateinit var lav: LottieAnimationView
    private lateinit var button: MaterialButton

    override fun onViewCreated(layoutScanAvailableDevices: LayoutLottieAnimationViewButtonBinding) {
        layoutScanAvailableDevices.apply {
            lav.visibility = View.GONE
            this@AvailableDevicesFragmentPresenter.lav = lav

            button.text = (view as Fragment).requireContext().getString(R.string.scan_devices)
            this@AvailableDevicesFragmentPresenter.button = button
        }

        (connection as BluetoothConnection).isScanning.observe((view as Fragment).viewLifecycleOwner) {
            val context = (view as Fragment).requireContext()
            if (it) {
                view?.apply {
                    showAnimation(R.raw.bluetooth_scan)
                    updateButton(context.getString(R.string.cancel))
                }
            } else {
                view?.apply {
                    stopAnimation()
                    updateButton(
                        context.getString(R.string.scan_devices)
                    )
                }
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> {
                if ((connection as BluetoothConnection).isBluetoothLEModeAvailable()) {
                    inflater.inflate(R.menu.bluetooth_scan_menu, menu)
                }
            }
            ConnectionType.WIFI -> Unit
        }
    }

    override fun onClick(position: Int) {
        val device = availableDevicesAdapter.currentList[position]
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            (connection.receiver as BluetoothReceiver).isPairingProcess.observe((view as Fragment).viewLifecycleOwner) {
                it?.let {
                    if (it) {
                        previouslySelectedItemPosition.value?.let { prevIndex ->
                            rebindItemAt(prevIndex)
                        }
                        view?.showAnimation(R.raw.bluetooth_pairing)
                    } else {
                        val itemView = recyclerView.findViewHolderForAdapterPosition(position)
                        (itemView as DevicesAdapter.ViewHolder).bind(device)
                        updateSelectedItem(position)
                        view?.stopAnimation()

                        (connection.receiver as BluetoothReceiver).isPairingProcess.removeObservers(
                            (view as Fragment).viewLifecycleOwner
                        )
                        (connection.receiver as BluetoothReceiver).isPairingProcess.postValue(null)
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
        connection.registerReceiver((view as Fragment).requireContext())
    }

    override fun onConnectionIconPressed() {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> (connection as BluetoothConnection).changeBluetoothMode()
            ConnectionType.WIFI -> Unit /* no-op */
        }
    }

    override fun onScanAvailableDevicesPressed() {
        checkPermission()
    }

    override fun permissionGranted() {
        when (connection.getConnectionType()) {
            ConnectionType.BLUETOOTH -> connection.startScan()
            ConnectionType.WIFI -> Unit //(connection as WifiConnection).getAvailableDevices() // TODO: 15/09/2020 wifi
        }
    }

    override fun getAvailableDevices(): LiveData<ArrayList<BluetoothDevice>> =
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
        ServerDevice.writeToSharedPreferences((view as Fragment).requireContext(), connection)
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
