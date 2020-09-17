package dev.jatzuk.servocontroller.connection.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.utils.notifyDataSetChanged

private const val TAG = "BluetoothScanningRec"

class BluetoothScanningReceiver : BroadcastReceiver() {

    private val _availableDevices = MutableLiveData<ArrayList<BluetoothDevice>>(ArrayList())
    val availableDevices: LiveData<ArrayList<BluetoothDevice>> get() = _availableDevices

    val isPairingProcess = MutableLiveData<Boolean>()

    override fun onReceive(context: Context, intent: Intent) {
        val device =
            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                device?.let {
                    if (!_availableDevices.value!!.contains(it)) {
                        _availableDevices.value!!.add(it)
                        _availableDevices.notifyDataSetChanged()
                    }
                }
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                device?.let {
                    isPairingProcess.value = when (it.bondState) {
                        BluetoothDevice.BOND_BONDING -> true
                        BluetoothDevice.BOND_BONDED -> false
                        else -> null
                    }
                }
            }
        }
    }

    fun clearList() {
        _availableDevices.value!!.clear()
    }
}
