package dev.jatzuk.servocontroller.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.jatzuk.servocontroller.other.Servo
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ServoDatabaseTest {

    private lateinit var servosDao: ServoDAO
    private lateinit var db: ServoDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ServoDatabase::class.java).build()
        servosDao = db.getServoDao()

        servosDao.insertServo(Servo(0))
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun getServoFromDB_returnsServo() {
        val servoFromDB = servosDao.getServoByOrder(0)
        assertThat(servoFromDB).isNotNull()
    }

    @Test
    fun getDeletedServoFromDB_returnsNull() {
        var servo = servosDao.getServoByOrder(0)
        assertThat(servo).isNotNull()
        servosDao.deleteServo(servo!!)
        servo = servosDao.getServoByOrder(0)
        assertThat(servo).isNull()
    }
}
