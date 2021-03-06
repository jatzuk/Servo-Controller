package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.databinding.LayoutToastBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import javax.inject.Inject

@AndroidEntryPoint
class DevicesFragment : Fragment(R.layout.fragment_devices), DevicesFragmentContract.View {

    private var binding: FragmentDevicesBinding? = null
    private var toastBinding: LayoutToastBinding? = null

    @Inject
    lateinit var presenter: DevicesFragmentContract.Presenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDevicesBinding.bind(view)
        toastBinding = LayoutToastBinding.inflate(layoutInflater)

        binding?.layoutEnableHardwareRequest?.button?.apply {
            setOnClickListener {
                presenter.onEnableHardwareButtonPressed()
            }
        }

        presenter.apply {
            createTabLayout(binding!!)
            onViewCreated()
        }
    }

    override fun updateTabLayoutVisibility(isVisible: Boolean) {
        binding?.apply {
            layoutLinear.visibility = if (isVisible) View.VISIBLE else View.GONE

            val hardwareRequestVisibility = if (isVisible) View.GONE else View.VISIBLE
            layoutEnableHardwareRequest.apply {
                lav.visibility = hardwareRequestVisibility
                button.visibility = hardwareRequestVisibility
            }
        }
    }

    override fun updateButtonText(text: String) {
        binding?.layoutEnableHardwareRequest?.button?.apply {
            this.text = getString(R.string.enable, text)
        }
    }

    override fun showAnimation(resourceId: Int, speed: Float, timeout: Long) {
        binding?.layoutEnableHardwareRequest?.lav?.apply {
            visibility = View.VISIBLE
            setAnimation(resourceId)
            this.speed = speed
            repeatCount = LottieDrawable.INFINITE
            enableMergePathsForKitKatAndAbove(true)
            playAnimation()
        }
    }

    override fun stopAnimation() {
        binding?.layoutEnableHardwareRequest?.lav?.apply {
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

    override fun onDestroyView() {
        binding = null
        toastBinding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
