package dev.jatzuk.servocontroller.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_WIFI
import kotlinx.coroutines.*
import javax.inject.Inject

private const val TAG = "DevicesFragment"

@AndroidEntryPoint
class DevicesFragment : Fragment(R.layout.fragment_devices), DevicesFragmentContract.View {

    private var binding: FragmentDevicesBinding? = null

    @Inject
    lateinit var presenter: DevicesFragmentContract.Presenter

    private lateinit var animationJob: CompletableJob

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDevicesBinding.bind(view)
        setupRecyclerViews()
        setupOnClickListeners()

        presenter.getAvailableDevices().observe(viewLifecycleOwner) {
            binding!!.recyclerViewAvailableDevices.updateAdapterDataSet(it)
        }

        binding?.layoutIncludedEnableHardwareRequest?.buttonConnectionToggle?.apply {
            text = getString(R.string.enable, presenter.getConnectionType().name)
            setOnClickListener {
                presenter.onEnableHardwareButtonPressed()
            }
        }

        presenter.onViewCreated()
    }

    private fun setupRecyclerViews() {
        with(presenter) {
            binding?.apply {
                setupPairedDevicesRecyclerView(this.recyclerViewPairedDevices)
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

    private fun RecyclerView.updateAdapterDataSet(devices: List<BluetoothDevice>) {
        (adapter as DevicesAdapter).apply {
            submitList(devices)
            notifyDataSetChanged()
        }
    }

    override fun updateRecyclerViewsVisibility(isVisible: Boolean) {
        val contentVisibility = if (isVisible) View.VISIBLE else View.GONE
        val hardwareRequestVisibility = if (isVisible) View.GONE else View.VISIBLE

        binding?.apply {
            layoutMotion.visibility = contentVisibility

            layoutIncludedEnableHardwareRequest.apply {
                connectionAnimationView.visibility = hardwareRequestVisibility
                buttonConnectionToggle.visibility = hardwareRequestVisibility
            }
        }
    }

    override fun showAnimation(
        resourceId: Int,
        speed: Float,
        timeout: Long,
        afterAnimationAction: (() -> Unit)?
    ) {
        if (::animationJob.isInitialized && animationJob.isActive) {
            animationJob.cancel()
        }

        binding?.layoutIncludedEnableHardwareRequest?.connectionAnimationView?.apply {
            visibility = View.VISIBLE
            setAnimation(resourceId)
            this.speed = speed
            repeatCount = LottieDrawable.INFINITE
            enableMergePathsForKitKatAndAbove(true)
            playAnimation()

            if (timeout > 0) {
                animationJob = Job()
                CoroutineScope(Dispatchers.Main + animationJob).launch {
                    delay(timeout)
                    stopAnimation()
                    afterAnimationAction?.invoke()
                }
            }
        }
    }

    override fun stopAnimation() {
        binding?.layoutIncludedEnableHardwareRequest?.connectionAnimationView?.apply {
            visibility = View.GONE
            cancelAnimation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.permissionGranted()
        } else {
            presenter.permissionDenied()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ENABLE_BT, REQUEST_ENABLE_WIFI -> presenter.onRequestEnableHardwareReceived()
//                REQUEST_ENABLE_WIFI -> presenter.onWIFIRequestEnableReceived()
            }
        } else {
            // FIXME: 02/09/2020 replace with animation?
            showToast(getString(R.string.enable_connection_module_info))
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
