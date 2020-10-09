package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothDevice
import android.net.wifi.ScanResult
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Parcelable
import dev.jatzuk.servocontroller.adapters.ParcelableDevicesAdapter.ParcelableViewHolder.Companion.fromBinding
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

class ParcelableDevicesAdapter(
    connectionType: ConnectionType? = null,
    onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
) : AbstractAdapter<Parcelable>(
    connectionType,
    onSelectedDeviceClickListener,
    ParcelableDevicesDiffCallback()
) {

    override fun provideViewHolder(
        binding: ItemDeviceBinding,
        connectionType: ConnectionType?,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener?
    ): AbstractDeviceViewHolder<Parcelable> =
        fromBinding(binding, connectionType, onSelectedDeviceClickListener)

    class ParcelableViewHolder private constructor(
        binding: ItemDeviceBinding,
        connectionType: ConnectionType?,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ) : AbstractDeviceViewHolder<Parcelable>(
        binding,
        connectionType,
        onSelectedDeviceClickListener
    ) {

        companion object {

            fun fromBinding(
                binding: ItemDeviceBinding,
                connectionType: ConnectionType?,
                onSelectedDeviceClickListener: OnSelectedDeviceClickListener?
            ) = ParcelableViewHolder(binding, connectionType, onSelectedDeviceClickListener)
        }
    }

    class ParcelableDevicesDiffCallback : AbstractDevicesDiffCallback<Parcelable>() {

        override fun areContentsTheSame(oldItem: Parcelable, newItem: Parcelable) = when (oldItem) {
            is BluetoothDevice -> oldItem.address == (newItem as BluetoothDevice).address
            is WifiP2pDevice -> oldItem.deviceAddress == (newItem as WifiP2pDevice).deviceAddress
            is ScanResult -> oldItem.BSSID == (newItem as ScanResult).BSSID
            else -> false
        }
    }
}
