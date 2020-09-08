package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

class DevicesAdapter :
    ListAdapter<BluetoothDevice, DevicesAdapter.ViewHolder>(DevicesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder private constructor(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BluetoothDevice) {
            binding.run {
                this.device = device
                tvName.text = device.name
                tvMacAddress.text = device.address
//                ivStatus =
//                ivDeviceIcon =
                executePendingBindings()
            }
        }

        companion object {

            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class DevicesDiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {

        override fun areItemsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ) = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ) = oldItem.address == newItem.address
    }
}
