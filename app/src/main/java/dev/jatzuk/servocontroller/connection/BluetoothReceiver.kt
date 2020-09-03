package dev.jatzuk.servocontroller.connection

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "BluetoothReceiver"

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
        Log.d(TAG, "onReceive: state: $state")

        Log.d(TAG, "onReceive: action ${intent.action}")
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "onReceive: posting on value")
                        BluetoothConnection.connection.connectionState.postValue(ConnectionState.ON)
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        BluetoothConnection.connection.connectionState.postValue(ConnectionState.OFF)
                    }
                    BluetoothAdapter.STATE_CONNECTING -> {
                        Log.d(TAG, "onReceive: connecting")
//                        HomeFragmentPresenter.companionView.showAnimation(true)
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
//                        HomeFragmentPresenter.companionView.setConnectionAnimationVisibility(false)
                    }
                    BluetoothAdapter.STATE_DISCONNECTING -> {

                    }
                    BluetoothAdapter.STATE_DISCONNECTED -> {

                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {
                        Log.d(TAG, "onReceive: turning on")
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        Log.d(TAG, "onReceive: turning off")
                    }
                    else -> {
                        Log.d(TAG, "onReceive: state - $state")
                    }
                }
            }
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                Log.d(TAG, "onReceive: connecting")
            }
            else -> {
                Log.d(TAG, "onReceive: ffffffffffffffff")
            }
        }
    }
}
