package dev.jatzuk.servocontroller.adapters

import androidx.databinding.BindingAdapter
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.other.WriteMode

@BindingAdapter("updateText")
fun TextInputEditText.updateText(text: String?) {
    setText(text)
}

@BindingAdapter("setupSliderValues")
fun Slider.setupSlider(servo: Servo?) {
    servo?.let {
        val range = when (servo.writeMode) {
            WriteMode.WRITE -> 0f..180f
            WriteMode.WRITE_MICROSECONDS -> 1500f..2500f
        }

        valueFrom = range.start
        valueTo = range.endInclusive
        value = (range.start + range.endInclusive) / 2f
    }
}
