package dev.jatzuk.servocontroller.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.jatzuk.servocontroller.other.Servo

@Database(
    entities = [Servo::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ServoDatabase : RoomDatabase() {

    abstract fun getServoDao(): ServoDAO
}
