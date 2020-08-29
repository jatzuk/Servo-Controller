package dev.jatzuk.servocontroller.db

import androidx.room.TypeConverter
import dev.jatzuk.servocontroller.other.SendMode
import dev.jatzuk.servocontroller.other.WriteMode

class Converters {

    @TypeConverter
    fun SendMode.toInt() = ordinal

    @TypeConverter
    fun Int.toSendBehaviour() = SendMode.values()[this]

    @TypeConverter
    fun WriteMode.toInt() = ordinal

    @TypeConverter
    fun Int.toServoRange() = WriteMode.values()[this]
}
