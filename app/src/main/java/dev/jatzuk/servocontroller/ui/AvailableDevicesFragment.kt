package dev.jatzuk.servocontroller.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.FragmentAvailableDevicesBinding
import dev.jatzuk.servocontroller.databinding.LayoutToastBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.available.AvailableDevicesFragmentContract
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AvailableDevicesFragment : Fragment(R.layout.fragment_available_devices),
    AvailableDevicesFragmentContract.View {

    private var binding: FragmentAvailableDevicesBinding? = null
    private var toastBinding: LayoutToastBinding? = null

    @Inject
    lateinit var presenter: AvailableDevicesFragmentContract.Presenter

    private lateinit var animationJob: CompletableJob

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAvailableDevicesBinding.bind(view)
        toastBinding = LayoutToastBinding.inflate(layoutInflater)

        presenter.apply {
            onViewCreated(binding!!.layoutScanAvailableDevices)
            setupRecyclerView(binding!!.recyclerView)
        }

        binding?.layoutScanAvailableDevices?.button?.setOnClickListener {
            presenter.onScanAvailableDevicesPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        presenter.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ic_scan_type -> {
                presenter.onConnectionIconPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
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
        try {
            toastBinding?.textView?.text = message
            Toast(requireContext()).apply {
                view = toastBinding!!.root
                setGravity(Gravity.TOP, 0, MainActivity.toastOffset)
                duration = length
                show()
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), message, length).show()
        }
    }

    override fun updateButton(text: String, isVisible: Boolean) {
        binding?.layoutScanAvailableDevices?.button?.apply {
            this.text = text
            visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        binding = null
        toastBinding = null
        super.onDestroyView()
    }
}
