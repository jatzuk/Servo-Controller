package dev.jatzuk.servocontroller.connection.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.ConnectionState
import dev.jatzuk.servocontroller.utils.notifyDataSetChanged

class BluetoothReceiver(
    private val connection: BluetoothConnection
) : BroadcastReceiver() {

    private val _availableDevices = MutableLiveData<ArrayList<BluetoothDevice>>(ArrayList())
    val availableDevices: LiveData<ArrayList<BluetoothDevice>> get() = _availableDevices

    val isPairingProcess = MutableLiveData<Boolean>()

    override fun onReceive(context: Context, intent: Intent) {
        var state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)

        if (state == -1) {
            state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
        }

        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        connection.connectionState.postValue(ConnectionState.ON)
                    }
                    BluetoothAdapter.STATE_CONNECTING -> {
                        connection.connectionState.postValue(ConnectionState.CONNECTING)
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
                        connection.connectionState.postValue(ConnectionState.CONNECTED)
//                        HomeFragmentPresenter.companionView.setConnectionAnimationVisibility(false)
                    }
                    BluetoothAdapter.STATE_DISCONNECTING -> {
                        connection.connectionState.postValue(ConnectionState.DISCONNECTING)
                    }
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        connection.connectionState.postValue(ConnectionState.DISCONNECTED)

                    }
                    BluetoothAdapter.STATE_OFF -> {
                        connection.connectionState.postValue(ConnectionState.OFF)
                    }
                    else -> {
                        /* no-op */
                    }
                }
            }
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
            else -> {
                /* no-op */
            }
        }
    }

    fun clearAvailableDevices() {
        _availableDevices.value!!.clear()
    }
}
