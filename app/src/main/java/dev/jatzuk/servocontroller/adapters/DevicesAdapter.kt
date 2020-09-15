package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

class DevicesAdapter(
    private val onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
) : ListAdapter<BluetoothDevice, DevicesAdapter.ViewHolder>(DevicesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder.from(parent, onSelectedDeviceClickListener)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder private constructor(
        private val binding: ItemDeviceBinding,
        private val onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

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

        override fun onClick(view: View) {
            onSelectedDeviceClickListener?.onClick(layoutPosition)
        }

        fun setSelectedColor() {
            binding.ivStatus.setColorFilter(Color.GREEN)
        }

        fun reset() {
            val color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
            binding.ivStatus.setColorFilter(color)
        }

        companion object {

            fun from(
                parent: ViewGroup,
                onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
            ): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, onSelectedDeviceClickListener)
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

    interface OnSelectedDeviceClickListener {

        fun onClick(position: Int)
    }
}
