package dev.jatzuk.servocontroller.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.bluetooth.BluetoothConnection
import dev.jatzuk.servocontroller.databinding.FragmentHomeBinding
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    private lateinit var bluetoothConnection: BluetoothConnection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        bluetoothConnection = BluetoothConnection(requireContext())

        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        binding!!.apply {
            connect.setOnClickListener {
                bluetoothConnection.buildDeviceList()
            }

            sendData.setOnClickListener {
                val data = "pos: ${servoView.positionInDegrees}\n"
                bluetoothConnection.sendData(data.toByteArray())
            }

            disconnect.setOnClickListener {
                bluetoothConnection.disconnect()
            }
        }
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
