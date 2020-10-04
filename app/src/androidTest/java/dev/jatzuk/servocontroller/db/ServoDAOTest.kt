package dev.jatzuk.servocontroller.db

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.jatzuk.servocontroller.other.Servo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@SmallTest
@HiltAndroidTest
class ServoDAOTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var db: ServoDatabase

    private lateinit var servosDao: ServoDAO

    @Before
    fun setUp() {
        hiltRule.inject()
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
