package dev.jatzuk.servocontroller.connection.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.utils.notifyDataSetChanged

class BluetoothScanningReceiver : BroadcastReceiver() {

    private val _availableDevices = MutableLiveData<ArrayList<BluetoothDevice>>(ArrayList())
    val availableDevices: LiveData<ArrayList<BluetoothDevice>> get() = _availableDevices

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!_availableDevices.value!!.contains(it)) {
                        _availableDevices.value!!.add(it)
                        _availableDevices.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun clearList() {
        _availableDevices.value!!.clear()
    }
}
