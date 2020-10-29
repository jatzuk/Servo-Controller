package dev.jatzuk.servocontroller.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.db.ServoDAO
import dev.jatzuk.servocontroller.other.Servo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ServosModel @Inject constructor(private val servoDAO: ServoDAO) {

    private val _servos = MutableLiveData(ArrayList<Servo>())
    val servos: LiveData<ArrayList<Servo>> get() = _servos

    fun getServoAt(index: Int) = _servos.value!![index]

    fun saveServoAt(index: Int, servo: Servo) {
        _servos.value!![index] = servo
        CoroutineScope(Dispatchers.IO).launch {
            servoDAO.insertServo(servo)
        }
    }

    fun loadServosFromDB(requestSize: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val servoList = ArrayList<Servo>()
            repeat(requestSize) { i ->
                var servo = servoDAO.getServoByOrder(i)
                if (servo == null) {
                    servo = Servo(i)
                    servoDAO.insertServo(servo)
                }
                servoList.add(servo)
            }
            _servos.postValue(servoList)
        }
    }
}
