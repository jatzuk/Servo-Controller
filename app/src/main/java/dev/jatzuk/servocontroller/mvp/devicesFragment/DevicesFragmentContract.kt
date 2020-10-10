package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.widget.Toast
import androidx.annotation.RawRes
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface DevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun onViewCreated()

        fun createTabLayout(binding: FragmentDevicesBinding)

        fun getConnectionType(): ConnectionType

        fun onEnableHardwareButtonPressed()

        fun onRequestEnableHardwareReceived()
    }

    interface View : BaseView<Presenter> {

        fun updateTabLayoutVisibility(isVisible: Boolean)

        fun updateButtonText(text: String)

        fun showAnimation(@RawRes resourceId: Int, speed: Float = 1f, timeout: Long = 0L)

        fun stopAnimation()

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
