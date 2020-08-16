package dev.jatzuk.servocontroller.other

data class ServoSetup(
    val servoOrder: Int,
    var command: String = "$servoOrder#P",
    var sendType: SendType = SendType.ON_RELEASE
)

enum class SendType {
    ON_RELEASE, ON_CLICK
}
