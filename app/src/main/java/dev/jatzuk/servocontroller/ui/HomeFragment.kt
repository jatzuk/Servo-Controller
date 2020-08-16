package dev.jatzuk.servocontroller.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.FragmentHomeBinding
import dev.jatzuk.servocontroller.mvp.homefragment.HomeFragmentContract
import dev.jatzuk.servocontroller.mvp.homefragment.HomeFragmentPresenter
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT

class HomeFragment
    : Fragment(R.layout.fragment_home), ServoView.OnSetupClickListener, HomeFragmentContract.View {

    private var binding: FragmentHomeBinding? = null

    private lateinit var presenter: HomeFragmentContract.Presenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setPresenter(HomeFragmentPresenter(this))
        setupOnClickListeners()

        if (!presenter.isBluetoothSupported()) {
            Toast.makeText(
                requireContext(),
                "Bluetooth not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: 16/08/20 disable bluetooth views and functionality
        }

        if (!presenter.isBluetoothEnabled()) {
            presenter.requestBluetoothIfNeeded()
        }
    }

    private fun setupOnClickListeners() {
        binding!!.apply {
            connect.setOnClickListener {
                presenter.buildDeviceList()
            }

            sendData.setOnClickListener {
                val data = "pos: ${servoView.positionInDegrees}\n"
                presenter.sendCommand(data.toByteArray())
            }

            disconnect.setOnClickListener {
                presenter.disconnect()
            }

            servoView.onSetupClickListener = this@HomeFragment
        }
    }

    override fun onClick() {
        showServoSettingsDialog()
    }

    override fun showServoSettingsDialog() {
        presenter.onServoSettingsTapped()
    }

    override fun setPresenter(presenter: HomeFragmentContract.Presenter) {
        this.presenter = presenter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ENABLE_BT -> {
                    // TODO: 13/08/2020 bluetooth enabled
                    Toast.makeText(requireContext(), "BT enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
