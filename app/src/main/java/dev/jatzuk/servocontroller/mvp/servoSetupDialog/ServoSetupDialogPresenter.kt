package dev.jatzuk.servocontroller.mvp.servoSetupDialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.db.ServoDAO
import dev.jatzuk.servocontroller.other.SendMode
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.other.WriteMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ServoSetupDialogPresenter @Inject constructor(
    private val servoDAO: ServoDAO
) : ServoSetupDialogContract.Presenter {

    lateinit var view: ServoSetupDialogContract.View
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val servoLiveData = MutableLiveData<Servo>()
    private var position = 1

    override fun assignPositionFromArguments(position: Int) {
        this.position = position
        coroutineScope.launch {
            servoLiveData.postValue(
                servoDAO.getServoByOrder(this@ServoSetupDialogPresenter.position)
                    ?: Servo(this@ServoSetupDialogPresenter.position)
            )
        }
    }

    override fun getLiveData() = servoLiveData as LiveData<Servo>

    override fun processSave(array: Array<String>) {
        servoLiveData.value!!.apply {
            command = array[0]
            tag = array[1]
            writeMode = WriteMode.fromResourceId(array[2].toInt())
            sendMode = SendMode.fromResourceId(array[3].toInt())
        }
    }

    override fun invokeOnStopCallback(callback: () -> Unit) {
        callback.invoke()
    }

    override fun onRestoreDefaultsClicked() {
        val order = servoLiveData.value!!.servoOrder + 1
        val command = "$order${Servo.DEFAULT_COMMAND_PATTERN}${Servo.ADDITIONAL_COMMAND_KEY}"
        view.setValues(order, command, R.id.rb_send_on_release)
    }
}
