package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.core.content.ContextCompat
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.BluetoothDeviceAdapter.BluetoothDeviceViewHolder.Companion.fromBinding
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

class BluetoothDeviceAdapter(
    onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
) : AbstractAdapter<BluetoothDevice>(
    onSelectedDeviceClickListener,
    BluetoothDevicesDiffCallback()
) {

    override fun provideViewHolder(
        binding: ItemDeviceBinding,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener?
    ): AbstractDeviceViewHolder<BluetoothDevice> =
        fromBinding(binding, onSelectedDeviceClickListener)

    class BluetoothDeviceViewHolder private constructor(
        binding: ItemDeviceBinding,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ) : AbstractDeviceViewHolder<BluetoothDevice>(binding, onSelectedDeviceClickListener) {

        private var isPaired = false

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(device: BluetoothDevice) {
            binding.run {
                this.device = device
                tvName.text = device.name
                tvMacAddress.text = device.address

                updatePairedInfo(device)

                val stringResource = if (isPaired) R.string.paired else R.string.unpaired
                tvStatus.text = itemView.context.getString(stringResource)

                provideDefaultColor()
                ivStatus.setColorFilter(defaultColor)

                // TODO: 08/10/2020 replace icon
                ivDeviceIcon.setImageResource(android.R.drawable.stat_sys_data_bluetooth)

                executePendingBindings()
            }
        }

        fun reset() {
            resetColor()
            binding.tvStatus.text = itemView.context.getString(R.string.paired)
        }

        override fun provideDefaultColor() {
            val colorRes = if (isPaired) R.color.colorPrimary else R.color.colorGrey
            defaultColor = ContextCompat.getColor(itemView.context, colorRes)
        }

        private fun updatePairedInfo(device: BluetoothDevice) {
            isPaired = BluetoothAdapter.getDefaultAdapter().bondedDevices.contains(device)
        }

        companion object {

            fun fromBinding(
                binding: ItemDeviceBinding,
                onSelectedDeviceClickListener: OnSelectedDeviceClickListener?
            ) = BluetoothDeviceViewHolder(binding, onSelectedDeviceClickListener)
        }
    }

    class BluetoothDevicesDiffCallback : AbstractDevicesDiffCallback<BluetoothDevice>() {

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ) = oldItem.address == newItem.address
    }
}
