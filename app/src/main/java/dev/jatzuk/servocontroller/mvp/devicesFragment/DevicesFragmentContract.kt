package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.widget.Toast
import androidx.annotation.RawRes
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface DevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun onViewCreated()

        fun getConnectionType(): ConnectionType

        fun onEnableHardwareButtonPressed()

        fun onRequestEnableHardwareReceived()
//
//        fun setupPairedDevicesRecyclerView(recyclerView: RecyclerView)
//
//        fun setupAvailableDevicesRecyclerView(recyclerView: RecyclerView)
//
//        fun getPairedDevices(): List<BluetoothDevice>?
//
//        fun getAvailableBluetoothDevices(): List<BluetoothDevice>?

//        fun getAvailableDevices(): LiveData<ArrayList<BluetoothDevice>>

//        fun scanAvailableDevicesPressed()

//        fun permissionGranted()

//        fun permissionDenied()
    }

    interface View : BaseView<Presenter> {

        fun updateTabLayoutVisibility(isVisible: Boolean)

        fun showAnimation(@RawRes resourceId: Int, speed: Float = 1f, timeout: Long = 0L)

        fun stopAnimation()

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
