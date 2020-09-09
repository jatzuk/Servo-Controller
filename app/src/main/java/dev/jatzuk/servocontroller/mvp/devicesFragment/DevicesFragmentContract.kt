package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface DevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun setupPairedDevicesRecyclerView(recyclerView: RecyclerView)

        fun setupAvailableDevicesRecyclerView(recyclerView: RecyclerView)

        fun getPairedDevices(): List<BluetoothDevice>?

        fun getAvailableBluetoothDevices(): List<BluetoothDevice>?

        fun scanAvailableDevicesPressed()

        fun permissionGranted()

        fun permissionDenied()
    }

    interface View : BaseView<Presenter> {

        fun updateAvailableDevicesList(devices: List<BluetoothDevice>)

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
