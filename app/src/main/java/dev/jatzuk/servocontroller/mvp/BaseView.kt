package dev.jatzuk.servocontroller.mvp

import android.widget.Toast

interface BaseView<T> {

    fun assignPresenter(presenter: T)

    fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {}
}
