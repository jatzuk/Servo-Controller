package dev.jatzuk.servocontroller.other

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.jatzuk.servocontroller.R

@Entity(tableName = "servos_database")
data class Servo(
    @PrimaryKey(autoGenerate = false)
    var servoOrder: Int,
    var command: String = "${servoOrder + 1}$DEFAULT_COMMAND_PATTERN$ADDITIONAL_COMMAND_KEY",
    var tag: String = "${servoOrder + 1}",
    var servoRange: ServoRange = ServoRange.MICROSECONDS,
    var sendBehaviour: SendBehaviour = SendBehaviour.ON_RELEASE
) {

    companion object {

        const val DEFAULT_COMMAND_PATTERN = '#'
        const val ADDITIONAL_COMMAND_KEY = 'A'
    }
}

enum class SendBehaviour {
    ON_RELEASE, ON_BUTTON_CLICK;

    fun toResourceId() = when (this) {
        ON_BUTTON_CLICK -> R.id.rb_send_on_button_click
        ON_RELEASE -> R.id.rb_send_on_release
    }
}

enum class ServoRange {
    MILLIS, MICROSECONDS
}
