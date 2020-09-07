package dev.jatzuk.servocontroller.mvp.devicesFragment

import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

class DevicesFragmentPresenter @Inject constructor(
    private var view: DevicesFragmentContract.View?,
    private val connection: Connection
) : DevicesFragmentContract.Presenter {

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = DevicesAdapter().also {
                it.submitList(getPairedDevices()) // TODO: 08/09/2020 empty list info
            }
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
            setHasFixedSize(true)
        }

    }

    override fun getPairedDevices() = (connection as BluetoothConnection).getBondedDevices()

    override fun onDestroy() {
        view = null
    }
}
