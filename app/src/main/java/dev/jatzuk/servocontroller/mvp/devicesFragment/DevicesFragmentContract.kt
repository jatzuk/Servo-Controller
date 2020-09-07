package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.bluetooth.BluetoothDevice
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface DevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getPairedDevices(): List<BluetoothDevice>?
    }

    interface View : BaseView<Presenter> {

        override fun assignPresenter(presenter: Presenter) {}
    }
}
