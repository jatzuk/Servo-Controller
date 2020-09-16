package dev.jatzuk.servocontroller.mvp.devicesFragment.available

import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.databinding.LayoutLottieAnimationViewButtonBinding
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface AvailableDevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun onViewCreated(layoutScanAvailableDevices: LayoutLottieAnimationViewButtonBinding)

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getAvailableDevices(): LiveData<ArrayList<BluetoothDevice>>?

        fun onScanAvailableDevicesPressed()

        fun permissionGranted()

        fun permissionDenied()
    }

    interface View : BaseView<Presenter> {

        fun showAnimation(
            @RawRes resourceId: Int,
            speed: Float = 1f,
            timeout: Long = 0L,
            afterAnimationAction: (() -> Unit)? = null
        )

        fun stopAnimation()

        fun updateButton(text: String, isVisible: Boolean = true)

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        override fun assignPresenter(presenter: Presenter) {}
    }
}