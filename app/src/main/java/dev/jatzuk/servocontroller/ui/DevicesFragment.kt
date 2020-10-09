package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import javax.inject.Inject

private const val TAG = "DevicesFragment"

@AndroidEntryPoint
class DevicesFragment : Fragment(R.layout.fragment_devices), DevicesFragmentContract.View {

    private var binding: FragmentDevicesBinding? = null

    @Inject
    lateinit var presenter: DevicesFragmentContract.Presenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDevicesBinding.bind(view)

        binding?.layoutEnableHardwareRequest?.button?.apply {
            text = getString(R.string.enable, presenter.getConnectionType().name)
            setOnClickListener {
                presenter.onEnableHardwareButtonPressed()
            }
        }

        presenter.apply {
            createTabLayoutIfNeeded(binding!!)
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
