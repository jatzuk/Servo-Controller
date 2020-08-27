package dev.jatzuk.servocontroller.mvp.servoSetupDialog

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.db.ServoDAO
import dev.jatzuk.servocontroller.other.SendBehaviour
import dev.jatzuk.servocontroller.other.Servo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ServoSetupDialogPresenter @Inject constructor(
    private val servoDAO: ServoDAO
) : ServoSetupDialogContract.Presenter {

    lateinit var view: ServoSetupDialogContract.View
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val servoLivaData = MutableLiveData<Servo>()
    private var position = 1

    override fun assignPositionFromArguments(position: Int) {
        this.position = position
        coroutineScope.launch {
            servoLivaData.postValue(
                servoDAO.getServoByOrder(this@ServoSetupDialogPresenter.position)
                    ?: Servo(this@ServoSetupDialogPresenter.position)
            )
        }
    }

    override fun getLiveData() = servoLivaData as LiveData<Servo>

    override fun processSave(array: Array<String>) {
        servoLivaData.value!!.apply {
            command = array[0]
            tag = array[1]
            sendBehaviour = associateResourceIdWithEnum(array[2].toInt())
        }

        coroutineScope.launch {
            servoDAO.insertServo(servoLivaData.value!!)
        }
    }

    override fun onRestoreDefaultsClicked() {
        val order = servoLivaData.value!!.servoOrder + 1
        val command = "$order${Servo.DEFAULT_COMMAND_PATTERN}${Servo.ADDITIONAL_COMMAND_KEY}"
        view.setValues(order, command, R.id.rb_send_on_release)
    }

    private fun associateResourceIdWithEnum(@IdRes resourceId: Int) = when (resourceId) {
        R.id.rb_send_on_release -> SendBehaviour.ON_RELEASE
        R.id.rb_send_on_button_click -> SendBehaviour.ON_BUTTON_CLICK
        else -> throw IllegalArgumentException("Can not find associated enum with given $resourceId")
    }
}
