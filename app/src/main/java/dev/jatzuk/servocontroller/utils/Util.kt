package dev.jatzuk.servocontroller.utils

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.notifyDataSetChanged() {
    this.value = value
}
