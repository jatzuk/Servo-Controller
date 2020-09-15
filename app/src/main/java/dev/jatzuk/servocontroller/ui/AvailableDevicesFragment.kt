package dev.jatzuk.servocontroller.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
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
import dev.jatzuk.servocontroller.databinding.FragmentAvailableDevicesBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.available.AvailableDevicesFragmentContract
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AvailableDevicesFragment : Fragment(R.layout.fragment_available_devices),
    AvailableDevicesFragmentContract.View {

    private var binding: FragmentAvailableDevicesBinding? = null

    @Inject
    lateinit var presenter: AvailableDevicesFragmentContract.Presenter

    private lateinit var animationJob: CompletableJob

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAvailableDevicesBinding.bind(view)

        presenter.apply {
            onViewCreated(binding!!.layoutScanAvailableDevices)
            setupRecyclerView(binding!!.recyclerView)

            getAvailableDevices()!!.observe(viewLifecycleOwner) {
                binding!!.recyclerView.updateAdapterDataSet(it)
            }
        }

        binding?.layoutScanAvailableDevices?.button?.setOnClickListener {
            presenter.onScanAvailableDevicesPressed()
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

    override fun showAnimation(
        resourceId: Int,
        speed: Float,
        timeout: Long,
        afterAnimationAction: (() -> Unit)?
    ) {
        binding?.layoutScanAvailableDevices?.lav?.apply {
            visibility = View.VISIBLE
            setAnimation(resourceId)
            this.speed = speed
            repeatCount = LottieDrawable.INFINITE
            enableMergePathsForKitKatAndAbove(true)
            playAnimation()
        }

        if (timeout > 0) {
            animationJob = Job()
            CoroutineScope(Dispatchers.Main + animationJob).launch {
                delay(timeout)
                stopAnimation()
                afterAnimationAction?.invoke()
            }
        }
    }

    override fun stopAnimation() {
        binding?.layoutScanAvailableDevices?.lav?.apply {
            visibility = View.GONE
            cancelAnimation()
        }
    }

    override fun showToast(message: String, length: Int) {
        Toast.makeText(requireContext(), message, length).show()
    }

    override fun updateButton(text: String, isVisible: Boolean) {
        binding?.layoutScanAvailableDevices?.button?.apply {
            this.text = text
            visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun RecyclerView.updateAdapterDataSet(devices: List<BluetoothDevice>) {
        (adapter as DevicesAdapter).apply {
            submitList(devices)
            notifyDataSetChanged()
        }
    }

    override fun updateRecyclerViewItem(position: Int) {

    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
