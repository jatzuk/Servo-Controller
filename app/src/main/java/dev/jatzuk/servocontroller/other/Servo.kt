package dev.jatzuk.servocontroller.other

import androidx.room.Entity
import androidx.room.PrimaryKey

private const val DEFAULT_COMMAND_PATTERN = '#'
private const val ADDITIONAL_COMMAND_KEY = 'A'

@Entity(tableName = "servos_database")
data class Servo(
    @PrimaryKey(autoGenerate = false)
    var servoOrder: Int,
    var command: String = "${servoOrder + 1}$DEFAULT_COMMAND_PATTERN$ADDITIONAL_COMMAND_KEY",
    var tag: String = "${servoOrder + 1}",
    var servoRange: ServoRange = ServoRange.MICROSECONDS,
    var sendBehaviour: SendBehaviour = SendBehaviour.ON_RELEASE
)

enum class SendBehaviour {
    ON_RELEASE, ON_CLICK
}

enum class ServoRange {
    MILLIS, MICROSECONDS
}
