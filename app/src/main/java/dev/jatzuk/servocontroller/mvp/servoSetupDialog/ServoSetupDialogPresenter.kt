package dev.jatzuk.servocontroller.mvp.servoSetupDialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.db.ServoDAO
import dev.jatzuk.servocontroller.other.Servo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ServoSetupDialogPresenter @Inject constructor(

    private val servoDAO: ServoDAO
) : ServoSetupDialogContract.Presenter {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val servoLivaData = MutableLiveData<Servo>()
    private var position = 1
    private var isResetting = false

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
        }

        coroutineScope.launch {
            if (isResetting) servoDAO.deleteServo(servoLivaData.value!!)
            else servoDAO.insertServo(servoLivaData.value!!)
        }
    }
}
