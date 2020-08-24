package dev.jatzuk.servocontroller.adapters

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText

@BindingAdapter("updateText")
fun TextInputEditText.updateText(text: String?) {
    setText(text)
}
