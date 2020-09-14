package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.bluetooth.BluetoothDevice
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface DevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun onViewCreated()

        fun getConnectionType(): ConnectionType

        fun onEnableHardwareButtonPressed()

        fun onRequestEnableHardwareReceived()

        fun setupPairedDevicesRecyclerView(recyclerView: RecyclerView)

        fun setupAvailableDevicesRecyclerView(recyclerView: RecyclerView)

        fun getPairedDevices(): List<BluetoothDevice>?

        fun getAvailableBluetoothDevices(): List<BluetoothDevice>?

        fun getAvailableDevices(): LiveData<ArrayList<BluetoothDevice>>

        fun scanAvailableDevicesPressed()

        fun permissionGranted()

        fun permissionDenied()

        fun onFoldButtonPressedAtAvailableDevicesLayout(
            motionLayout: MotionLayout,
            imageView: ImageView
        )
    }

    interface View : BaseView<Presenter> {

        fun updateRecyclerViewsVisibility(isVisible: Boolean)

        fun showAnimation(
            @RawRes resourceId: Int,
            speed: Float = 1f,
            timeout: Long = 0L,
            afterAnimationAction: (() -> Unit)? = null
        )

        fun stopAnimation()

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
