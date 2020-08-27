package dev.jatzuk.servocontroller.mvp.servoSetupDialog

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import dev.jatzuk.servocontroller.other.Servo

interface ServoSetupDialogContract {

    interface Presenter {

        fun assignPositionFromArguments(position: Int)

        fun getLiveData(): LiveData<Servo>

        fun processSave(array: Array<String>)

        fun onRestoreDefaultsClicked()
    }

    interface View {

        fun assignView()

        fun closeDialogView()

        fun setValues(order: Int, command: String, @IdRes resourceId: Int)
    }
}
