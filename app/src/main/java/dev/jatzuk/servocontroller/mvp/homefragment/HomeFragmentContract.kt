package dev.jatzuk.servocontroller.mvp.homefragment

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView
import dev.jatzuk.servocontroller.other.Servo

interface HomeFragmentContract {

    interface Presenter : BasePresenter {

        fun optionsMenuCreated()

        fun notifyViewCreated()

        fun getRecyclerViewLayoutManager(): RecyclerView.LayoutManager

        fun onReadyToRequestServosList()

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

        fun submitServosList(servos: List<Servo>)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
