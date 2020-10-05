package dev.jatzuk.servocontroller.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.activity.result.contract.ActivityResultContract
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_WIFI

class EnableConnectionHardwareContract : ActivityResultContract<Int, Boolean>() {

    override fun createIntent(context: Context, input: Int): Intent {
        val action = when (input) {
            REQUEST_ENABLE_BT -> BluetoothAdapter.ACTION_REQUEST_ENABLE
            REQUEST_ENABLE_WIFI -> WifiManager.ACTION_PICK_WIFI_NETWORK
            else -> return Intent()
        }
        return Intent(action)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
}
