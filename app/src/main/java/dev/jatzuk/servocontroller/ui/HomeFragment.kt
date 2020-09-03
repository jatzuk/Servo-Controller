package dev.jatzuk.servocontroller.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.ServoAdapter
import dev.jatzuk.servocontroller.databinding.FragmentHomeBinding
import dev.jatzuk.servocontroller.mvp.homeFragment.HomeFragmentContract
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_WIFI
import dev.jatzuk.servocontroller.other.Servo
import kotlinx.coroutines.*
import javax.inject.Inject

private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), HomeFragmentContract.View {

    private var binding: FragmentHomeBinding? = null
    private lateinit var connectionIcon: MenuItem
    private lateinit var servoAdapter: ServoAdapter

    @Inject
    lateinit var presenter: HomeFragmentContract.Presenter

    private lateinit var job: CompletableJob

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        presenter.onCreateView()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        binding?.buttonConnectionToggle?.setOnClickListener {
            presenter.connectionButtonPressed()
        }

        setupRecyclerView()
        presenter.onViewCreated()
    }

    private fun setupRecyclerView() {
        presenter.setupRecyclerView(binding!!.recyclerView)
        servoAdapter = binding!!.recyclerView.adapter as ServoAdapter
    }

    override fun submitServosList(servos: List<Servo>) {
        servoAdapter.submitList(servos)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ENABLE_BT, REQUEST_ENABLE_WIFI -> presenter.onRequestEnableHardwareReceived()
//                REQUEST_ENABLE_WIFI -> presenter.onWIFIRequestEnableReceived()
            }
        } else {
            // FIXME: 02/09/2020
            showToast("You need to enable module first")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
        connectionIcon = menu.getItem(0)
        Log.d(TAG, "onCreateOptionsMenu: connection icon initialized")
        presenter.optionsMenuCreated()
    }

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
        Toast.makeText(requireContext(), message, length).show()
    }

    override fun updateConnectionMenuIconVisibility(isVisible: Boolean) {
        if (::connectionIcon.isInitialized) {
            connectionIcon.isVisible = isVisible
        }
    }

    override fun setRecyclerViewVisibility(isVisible: Boolean) {
        binding?.recyclerView?.updateVisibility(isVisible)
    }

    override fun updateConnectionButton(text: String, isVisible: Boolean) {
        binding?.buttonConnectionToggle?.apply {
            this.text = text
            updateVisibility(isVisible)
        }
    }

    override fun showAnimation(
        resourceId: Int,
        speed: Float,
        timeout: Long,
        afterTimeoutAction: (() -> Unit)?
    ) {
        if (::job.isInitialized) {
            job.cancel()
        }

        binding?.connectionAnimationView?.apply {
            visibility = View.VISIBLE
            setAnimation(resourceId)
            this.speed = speed
            repeatCount = LottieDrawable.INFINITE
            enableMergePathsForKitKatAndAbove(true)
            playAnimation()

            if (timeout > 0) {
                job = Job()
                CoroutineScope(Dispatchers.Main + job).launch {
                    delay(timeout)
                    stopAnimation()
                    afterTimeoutAction?.invoke()
                }
            }
        }
    }

    override fun stopAnimation() {
        binding?.connectionAnimationView?.apply {
            visibility = View.GONE
            cancelAnimation()
        }
    }

    private fun View.updateVisibility(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        binding = null
        presenter.onDestroyView()
        super.onDestroyView()
    }
}
