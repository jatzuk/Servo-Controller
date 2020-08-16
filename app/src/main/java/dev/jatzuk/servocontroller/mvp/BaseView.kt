package dev.jatzuk.servocontroller.mvp

interface BaseView<T> {

    fun setPresenter(presenter: T)
}
