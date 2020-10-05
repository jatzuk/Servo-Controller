package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.tabs.TabLayoutMediator
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

        val viewPager = binding!!.viewPager.apply {
            adapter = FragmentAdapter(this@DevicesFragment)
        }
        TabLayoutMediator(binding!!.layoutTab, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.available_devices)
                else -> getString(R.string.paired_devices)
            }
        }.attach()

        presenter.onViewCreated()
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

    class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> AvailableDevicesFragment()
            else -> PairedDevicesFragment()
        }
    }
}
