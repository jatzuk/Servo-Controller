package dev.jatzuk.servocontroller.db

import androidx.room.*
import dev.jatzuk.servocontroller.other.Servo

@Dao
interface ServoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertServo(servo: Servo)

    @Delete
    fun deleteServo(servo: Servo)

    @Query("SELECT * FROM servos_database WHERE servoOrder =:order")
    fun getServoByOrder(order: Int): Servo?
}
