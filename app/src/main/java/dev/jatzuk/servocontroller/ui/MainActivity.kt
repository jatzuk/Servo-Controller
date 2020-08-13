package dev.jatzuk.servocontroller.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.bluetooth.BluetoothConnection
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var switchView: SwitchView
    private lateinit var bluetoothConnection: BluetoothConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchView = findViewById(R.id.switchView)

        bluetoothConnection = BluetoothConnection(this)

        if (!bluetoothConnection.bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        connect.setOnClickListener {
            bluetoothConnection.buildDeviceList()
        }

        sendData.setOnClickListener {
            val data = "pos: ${switchView.positionInDegrees}\n"
            bluetoothConnection.sendData(data.toByteArray())
        }

        disconnect.setOnClickListener {
            bluetoothConnection.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ENABLE_BT -> {
                    // TODO: 13/08/2020 bluetooth enabled
                    Toast.makeText(this, "BT enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
