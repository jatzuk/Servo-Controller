package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.ServoAdapter
import dev.jatzuk.servocontroller.databinding.FragmentHomeBinding
import dev.jatzuk.servocontroller.databinding.LayoutToastBinding
import dev.jatzuk.servocontroller.mvp.homeFragment.HomeFragmentContract
import dev.jatzuk.servocontroller.other.Servo
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), HomeFragmentContract.View {

    private var binding: FragmentHomeBinding? = null
    private var toastBinding: LayoutToastBinding? = null

    private lateinit var connectionIcon: MenuItem
    private lateinit var servoAdapter: ServoAdapter

    @Inject
    lateinit var presenter: HomeFragmentContract.Presenter

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
        binding = FragmentHomeBinding.bind(view)
        toastBinding = LayoutToastBinding.inflate(layoutInflater)

        binding?.layoutIncludedEnableHardwareRequest?.button?.setOnClickListener {
            presenter.connectionButtonPressed()
        }

        setupRecyclerView()
        presenter.onViewCreated()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    private fun setupRecyclerView() {
        presenter.setupRecyclerView(binding!!.recyclerView)
        servoAdapter = binding!!.recyclerView.adapter as ServoAdapter
    }

    override fun submitServosList(servos: List<Servo>) {
        servoAdapter.submitList(servos)
    }

    override fun updateDataSetAt(index: Int) {
        servoAdapter.notifyItemChanged(index)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
        connectionIcon = menu.getItem(0)
        presenter.optionsMenuCreated()
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.ic_connection_action -> {
            presenter.connectionIconPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun updateConnectionStateIcon(@DrawableRes resourceId: Int) {
        if (::connectionIcon.isInitialized) {
            connectionIcon.setIcon(resourceId)
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

    override fun updateConnectionMenuIconVisibility(isVisible: Boolean) {
        if (::connectionIcon.isInitialized) {
            connectionIcon.isVisible = isVisible
        }
    }

    override fun updateNavigationMenuItemAvailability(isAvailable: Boolean, index: Int) {
        (requireActivity() as NavigationMenuAvailabilitySwitcher)
            .updateNavigationMenuItemAvailability(isAvailable, index)
    }

    override fun setRecyclerViewVisibility(isVisible: Boolean) {
        binding?.recyclerView?.updateVisibility(isVisible)
    }

    override fun updateConnectionButton(text: String, isVisible: Boolean) {
        binding?.layoutIncludedEnableHardwareRequest?.button?.apply {
            this.text = text
            updateVisibility(isVisible)
        }
    }

    override fun updateSelectedDeviceHint(isVisible: Boolean, pair: Pair<String, String>?) {
        binding?.apply {
            pair?.let {
                tvSelectedDeviceName.text = it.first
                tvSelectedDeviceMacAddress.text = it.second
            }
            layoutLinear.updateVisibility(isVisible)
        }
    }

    override fun showAnimation(
        resourceId: Int,
        speed: Float,
        timeout: Long,
        afterTimeoutAction: (() -> Unit)?
    ) {
        if (::animationJob.isInitialized && animationJob.isActive) {
            animationJob.cancel()
        }

        binding?.layoutIncludedEnableHardwareRequest?.lav?.apply {
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
                    afterTimeoutAction?.invoke()
                }
            }
        }
    }

    override fun stopAnimation() {
        binding?.layoutIncludedEnableHardwareRequest?.lav?.apply {
            visibility = View.GONE
            cancelAnimation()
        }
    }

    override fun navigateTo(@IdRes id: Int) {
        findNavController().navigate(id)
    }

    private fun View.updateVisibility(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        binding = null
        toastBinding = null
        super.onDestroyView()
    }

    interface NavigationMenuAvailabilitySwitcher {

        fun updateNavigationMenuItemAvailability(isVisible: Boolean, index: Int)
    }
}
