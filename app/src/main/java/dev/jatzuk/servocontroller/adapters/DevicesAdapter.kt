package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothAdapter
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

        private var defaultColor = 0
        private var isPaired = false

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(device: BluetoothDevice) {
            binding.run {
                this.device = device
                tvName.text = device.name
                tvMacAddress.text = device.address

                updatePairedInfo(device)

                val stringResource = if (isPaired) R.string.paired else R.string.unpaired
                tvStatus.text = itemView.context.getString(stringResource)

                setDefaultColor()
                ivStatus.setColorFilter(defaultColor)

//                ivDeviceIcon =
                executePendingBindings()
            }
        }

        override fun onClick(view: View) {
            onSelectedDeviceClickListener?.onClick(layoutPosition)
        }

        fun setSelectedColor() {
            binding.apply {
                ivStatus.setColorFilter(Color.GREEN)
                tvStatus.text = itemView.context.getString(R.string.selected)
            }
        }

        fun reset() {
            binding.apply {
                ivStatus.setColorFilter(defaultColor)
                tvStatus.text = itemView.context.getString(R.string.paired)
            }
        }

        private fun setDefaultColor() {
            val colorRes = if (isPaired) R.color.colorPrimary else R.color.colorGrey
            defaultColor = ContextCompat.getColor(itemView.context, colorRes)
        }

        private fun updatePairedInfo(device: BluetoothDevice) {
            isPaired = BluetoothAdapter.getDefaultAdapter().bondedDevices.contains(device)
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
