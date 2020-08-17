package dev.jatzuk.servocontroller.mvp

interface BaseView<T> {

    fun assignPresenter(presenter: T)
}
