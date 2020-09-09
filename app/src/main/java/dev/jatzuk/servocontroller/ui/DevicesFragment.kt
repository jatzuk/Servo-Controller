package dev.jatzuk.servocontroller.ui

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import dev.jatzuk.servocontroller.other.ACCESS_FINE_LOCATION_REQUEST_CODE
import javax.inject.Inject

private const val TAG = "DevicesFragment"

@AndroidEntryPoint
class DevicesFragment : Fragment(R.layout.fragment_devices), DevicesFragmentContract.View {

    private var binding: FragmentDevicesBinding? = null

    @Inject
    lateinit var presenter: DevicesFragmentContract.Presenter

    private val devicesAdapter: DevicesAdapter = DevicesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDevicesBinding.bind(view)
        setupRecyclerViews()
        setupOnClickListeners()
    }

    private fun setupRecyclerViews() {
        with(presenter) {
            binding?.apply {
                setupPairedDevicesRecyclerView(this.recyclerViewPairedDevices)
                this.recyclerViewAvailableDevices.adapter = devicesAdapter
                setupAvailableDevicesRecyclerView(this.recyclerViewAvailableDevices)
            }
        }
    }

    private fun setupOnClickListeners() {
        binding?.apply {
            ivScanDevices.setOnClickListener {
                presenter.scanAvailableDevicesPressed()
            }
        }
    }

    override fun updateAvailableDevicesList(devices: List<BluetoothDevice>) {
        Log.d(TAG, "updateAvailableDevicesList: $devices")
        devicesAdapter.submitList(devices)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ACCESS_FINE_LOCATION_REQUEST_CODE -> presenter.permissionGranted()
            }
        } else {
            presenter.permissionDenied()
        }
    }

    override fun showToast(message: String, length: Int) {
        Toast.makeText(requireContext(), message, length).show()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
