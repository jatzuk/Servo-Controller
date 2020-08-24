package dev.jatzuk.servocontroller.db

import androidx.room.TypeConverter
import dev.jatzuk.servocontroller.other.SendBehaviour
import dev.jatzuk.servocontroller.other.ServoRange

class Converters {

    @TypeConverter
    fun SendBehaviour.toInt() = ordinal

    @TypeConverter
    fun Int.toSendBehaviour() = SendBehaviour.values()[this]

    @TypeConverter
    fun ServoRange.toInt() = ordinal

    @TypeConverter
    fun Int.toServoRange() = ServoRange.values()[this]
}
