package dev.jatzuk.servocontroller.mvp.homefragment

import android.widget.Toast
import androidx.annotation.DrawableRes
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface HomeFragmentContract {

    interface Presenter : BasePresenter {

        fun optionsMenuCreated()

        fun onBTRequestEnableReceived()

        fun connectionIconPressed()

        fun isConnectionTypeSupported(): Boolean

        fun isConnected(): Boolean

        fun requestConnectionHardware()

        fun onServoSettingsTapped()

        fun buildDeviceList()

        fun connect(): Boolean

        fun sendData(data: ByteArray): Boolean

        fun disconnect(): Boolean
    }

    interface View : BaseView<Presenter> {

        fun showServoSettingsDialog()

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        fun updateConnectionStateIcon(@DrawableRes resourceId: Int)

        fun updateConnectionMenuIconVisibility(isVisible: Boolean)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
