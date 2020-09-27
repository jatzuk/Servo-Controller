package dev.jatzuk.servocontroller.mvp.homeFragment

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView
import dev.jatzuk.servocontroller.other.Servo

interface HomeFragmentContract {

    interface Presenter : BasePresenter {

        fun optionsMenuCreated()

        fun onViewCreated()

        fun onStart()

        fun onDestroyView()

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getRecyclerViewLayoutManager(): RecyclerView.LayoutManager

        fun onRequestEnableHardwareReceived()

        fun connectionIconPressed()

        fun connectionButtonPressed()

        fun requestConnectionHardwareButtonPressed()

        fun isConnectionTypeSupported(): Boolean

        fun isConnectionModuleEnabled(): Boolean

        fun isConnected(): Boolean

        fun requestConnectionHardware()

        fun onServoSettingsTapped(layoutPosition: Int)

        fun onFinalPositionDetected(layoutPosition: Int, angle: Int)

        fun buildDeviceList()

        fun connect()

        fun sendData(data: ByteArray): Boolean

        fun disconnect()
    }

    interface View : BaseView<Presenter> {

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        fun updateConnectionStateIcon(@DrawableRes resourceId: Int)

        fun updateConnectionMenuIconVisibility(isVisible: Boolean)

        fun updateSelectedDeviceHint(isVisible: Boolean = true, pair: Pair<String, String>? = null)

        fun submitServosList(servos: List<Servo>)

        fun setRecyclerViewVisibility(isVisible: Boolean)

        fun showAnimation(
            @RawRes resourceId: Int,
            speed: Float = 1f,
            timeout: Long = 0L,
            afterTimeoutAction: (() -> Unit)? = null
        )

        fun stopAnimation()

        fun updateConnectionButton(text: String, isVisible: Boolean = true)

        fun navigateTo(id: Int)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
