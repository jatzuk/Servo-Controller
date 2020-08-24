package dev.jatzuk.servocontroller.mvp.servoSetupDialog

import androidx.lifecycle.LiveData
import dev.jatzuk.servocontroller.other.Servo

interface ServoSetupDialogContract {

    interface Presenter {

        fun assignPositionFromArguments(position: Int)

        fun getLiveData(): LiveData<Servo>

        fun processSave(array: Array<String>)
    }

    interface View
}
