package dev.jatzuk.servocontroller.mvp.devicesFragment.paired

import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface PairedDevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getPairedDevices(): List<Parcelable>?
    }

    interface View : BaseView<Presenter> {

        override fun assignPresenter(presenter: Presenter) {}
    }
}
