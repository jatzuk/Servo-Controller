package dev.jatzuk.servocontroller.mvp.devicesFragment.paired

import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.adapters.ParcelableDevicesAdapter
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.connection.WifiConnection
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

class PairedDevicesFragmentPresenter @Inject constructor(
    private var view: PairedDevicesFragmentContract.View?,
    private val connection: Connection
) : PairedDevicesFragmentContract.Presenter {

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = ParcelableDevicesAdapter(connection.getConnectionType()).also {
                it.submitList(getPairedDevices())
            }
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
        }
    }

    override fun getPairedDevices() = when (connection) {
        is BluetoothConnection -> connection.getBondedDevices()
        is WifiConnection -> connection.getBondedDevices()
        else -> throw IllegalArgumentException("Unsupported connection type")
    }

    override fun onDestroy() {
        view = null
    }
}
