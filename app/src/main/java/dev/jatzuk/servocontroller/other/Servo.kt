package dev.jatzuk.servocontroller.other

import androidx.annotation.IdRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.jatzuk.servocontroller.R

@Entity(tableName = "servos_database")
data class Servo(
    @PrimaryKey(autoGenerate = false)
    var servoOrder: Int,
    var tag: String = "${servoOrder + 1}",
    var writeMode: WriteMode = WriteMode.WRITE,
    var sendMode: SendMode = SendMode.ON_RELEASE,
    var command: String = "${servoOrder + 1}$DEFAULT_COMMAND_PATTERN$ADDITIONAL_COMMAND_KEY"
) {

    fun convertRange(value: Int, from: IntRange = 0..180, to: IntRange = 1500..2500) =
        to.first + (value - from.first) * (to.last - to.first) / (from.last - from.first)

    companion object {

        const val DEFAULT_COMMAND_PATTERN = '#'
        const val ADDITIONAL_COMMAND_KEY = 'F' // F - fast, S - slow
    }
}

enum class SendMode {
    ON_RELEASE, ON_BUTTON_CLICK;

    fun toResourceId() = when (this) {
        ON_BUTTON_CLICK -> R.id.rb_send_on_button_click
        ON_RELEASE -> R.id.rb_send_on_release
    }

    companion object {

        fun fromResourceId(@IdRes resourceId: Int) = when (resourceId) {
            R.id.rb_send_on_release -> ON_RELEASE
            R.id.rb_send_on_button_click -> ON_BUTTON_CLICK
            else -> throw IllegalArgumentException("Can not find associated enum with given $resourceId")
        }
    }
}

enum class WriteMode {
    WRITE, WRITE_MICROSECONDS;

    fun toResourceId() = when (this) {
        WRITE -> R.id.rb_mode_write
        WRITE_MICROSECONDS -> R.id.rb_mode_write_microseconds
    }

    companion object {

        fun fromResourceId(@IdRes resourceId: Int) = when (resourceId) {
            R.id.rb_mode_write -> WRITE
            R.id.rb_mode_write_microseconds -> WRITE_MICROSECONDS
            else -> throw IllegalArgumentException("Can not find associated enum with given $resourceId")
        }
    }
}
