package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

class DevicesAdapter :
    ListAdapter<BluetoothDevice, DevicesAdapter.ViewHolder>(DevicesDiffCallback()) {

    private var previouslySelectedPosition = -1
    private var selectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.apply {
            bind(item)
            itemView.setOnClickListener {
                val prevItem = (itemView.parent as RecyclerView).findViewHolderForAdapterPosition(
                    previouslySelectedPosition
                )
                prevItem?.let {
                    if (previouslySelectedPosition != position && previouslySelectedPosition > -1) {
                        (it as ViewHolder).reset()
                    }
                }

                selectedItem = position
                holder.onClicked(item)
                previouslySelectedPosition = position
            }
        }
    }

    fun getSelectedItem() = currentList[selectedItem]

    class ViewHolder private constructor(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BluetoothDevice) {
            binding.run {
                this.device = device
                tvName.text = device.name
                tvMacAddress.text = device.address
//                tvStatus.text = device.
//                ivStatus =
//                ivDeviceIcon =
                executePendingBindings()
            }
        }

        fun onClicked(device: BluetoothDevice) {
            binding.apply {
                this.device = device
                ivStatus.setColorFilter(Color.GREEN)
            }
        }

        fun reset() {
            val color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
            binding.ivStatus.setColorFilter(color)
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
