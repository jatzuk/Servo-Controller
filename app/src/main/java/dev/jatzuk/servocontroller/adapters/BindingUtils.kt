package dev.jatzuk.servocontroller.adapters

import androidx.databinding.BindingAdapter
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.other.WriteMode
import java.text.SimpleDateFormat
import java.util.*

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

@BindingAdapter("setDate")
fun MaterialTextView.setDate(date: Date?) {
    text = date?.let {
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        dateFormat.format(date)
    } ?: "No last connection info"
}
