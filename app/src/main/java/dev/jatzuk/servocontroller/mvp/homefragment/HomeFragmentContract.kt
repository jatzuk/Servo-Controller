package dev.jatzuk.servocontroller.mvp.homefragment

import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface HomeFragmentContract {

    interface Presenter : BasePresenter {

        fun isBluetoothSupported(): Boolean

        fun isBluetoothEnabled(): Boolean

        fun requestBluetoothIfNeeded()

        fun onServoSettingsTapped()

        fun buildDeviceList()

        fun sendCommand(data: ByteArray)

        fun disconnect()
    }

    interface View : BaseView<Presenter> {

        fun showServoSettingsDialog()
    }
}
