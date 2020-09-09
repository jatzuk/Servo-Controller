package dev.jatzuk.servocontroller.mvp.devicesFragment

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.adapters.DevicesAdapter
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.Connection
import dev.jatzuk.servocontroller.other.ACCESS_FINE_LOCATION_REQUEST_CODE
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

private const val TAG = "DevicesFragmentPretr"

class DevicesFragmentPresenter @Inject constructor(
    private var view: DevicesFragmentContract.View?,
    private val connection: Connection,
//    private val bluetoothReceiver: BluetoothReceiver
) : DevicesFragmentContract.Presenter {

    private val receiver = Receiver()

    override fun setupPairedDevicesRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = DevicesAdapter().also {
                it.submitList(getPairedDevices()) // TODO: 08/09/2020 empty list info
            }
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
            setHasFixedSize(true)
        }
    }

    override fun getPairedDevices() = (connection as BluetoothConnection).getBondedDevices()

    override fun setupAvailableDevicesRecyclerView(
        recyclerView: RecyclerView,
    ) {
        Companion.view = view
        recyclerView.apply {
            addItemDecoration(BottomPaddingDecoration(recyclerView.context))
//            setHasFixedSize(true)
        }

        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        (view as Fragment).requireContext().registerReceiver(receiver, intentFilter)

        Log.d(TAG, "setupAvailableDevicesRecyclerView: registered receiver")
    }

    override fun getAvailableBluetoothDevices() =
        (connection as BluetoothConnection).getAvailableDevices()

    override fun scanAvailableDevicesPressed() {
//        availableDevicesAdapter.submitList()
        Log.d(TAG, "scanAvailableDevicesPressed: ")
        requestPermissions()
    }

    override fun onDestroy() {
        (view as Fragment).requireContext().unregisterReceiver(receiver)
        view = null
    }

    private fun requestPermissions() {
        val fragment = view as Fragment
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                fragment.requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION_REQUEST_CODE
            )
        } else {
            getAvailableBluetoothDevices()
        }
    }

    override fun permissionGranted() {
        getAvailableBluetoothDevices()
    }

    override fun permissionDenied() {
        view?.showToast("you need to grant permission for bt discovery")
    }

    companion object {

        val availableDevices = MutableLiveData<ArrayList<BluetoothDevice>>(ArrayList())
        var view: DevicesFragmentContract.View? = null
    }

    class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        Log.d(TAG, "onReceive: $it")
                        availableDevices.value!!.add(it)
                        view!!.updateAvailableDevicesList(availableDevices.value!!)
                    }
                }
            }
        }
    }
}
