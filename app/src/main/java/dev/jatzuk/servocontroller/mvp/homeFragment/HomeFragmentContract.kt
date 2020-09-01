package dev.jatzuk.servocontroller.mvp.homeFragment

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

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getRecyclerViewLayoutManager(): RecyclerView.LayoutManager

        fun onBTRequestEnableReceived()

        fun connectionIconPressed()

        fun connectionButtonPressed()

        fun isConnectionTypeSupported(): Boolean

        fun isConnected(): Boolean

        fun requestConnectionHardware()

        fun onServoSettingsTapped(layoutPosition: Int)

        fun onFinalPositionDetected(layoutPosition: Int, angle: Int)

        fun buildDeviceList()

        suspend fun connect(): Boolean

        fun sendData(data: ByteArray): Boolean

        fun disconnect(): Boolean
    }

    interface View : BaseView<Presenter> {

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        fun updateConnectionStateIcon(@DrawableRes resourceId: Int)

        fun updateConnectionMenuIconVisibility(isVisible: Boolean)

        fun submitServosList(servos: List<Servo>)

        fun showConnectionAnimation(isVisible: Boolean)

        fun setConnectionButtonVisibility(isVisible: Boolean)

        fun setConnectionButtonText(text: String)

        fun showConnectionFailedAnimation()

        override fun assignPresenter(presenter: Presenter) {}
    }
}
