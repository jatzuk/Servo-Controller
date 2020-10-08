package dev.jatzuk.servocontroller.adapters

import android.net.wifi.p2p.WifiP2pDevice
import androidx.core.content.ContextCompat
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.WifiDeviceAdapter.WifiDeviceViewHolder.Companion.fromBinding
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

class WifiDeviceAdapter(
    onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null,
) : AbstractAdapter<WifiP2pDevice>(onSelectedDeviceClickListener, WifiDevicesDiffCallback()) {

    override fun provideViewHolder(
        binding: ItemDeviceBinding,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener?
    ): AbstractDeviceViewHolder<WifiP2pDevice> = fromBinding(binding, onSelectedDeviceClickListener)

    class WifiDeviceViewHolder private constructor(
        binding: ItemDeviceBinding,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ) : AbstractDeviceViewHolder<WifiP2pDevice>(binding, onSelectedDeviceClickListener) {

        override fun bind(device: WifiP2pDevice) {
            binding.run {
//                this.device = device
                tvName.text = device.deviceName
                tvMacAddress.text = device.deviceAddress

//                val stringResource = // TODO: 08/10/2020
                tvStatus.text = "WIFI status"

                provideDefaultColor()

                executePendingBindings()
            }
        }

        override fun provideDefaultColor() {
            val colorRes = R.color.colorPrimary
            defaultColor = ContextCompat.getColor(itemView.context, colorRes)
        }

        companion object {

            fun fromBinding(
                binding: ItemDeviceBinding,
                onSelectedDeviceClickListener: OnSelectedDeviceClickListener?
            ) = WifiDeviceViewHolder(binding, onSelectedDeviceClickListener)
        }
    }

    class WifiDevicesDiffCallback : AbstractDevicesDiffCallback<WifiP2pDevice>() {

        override fun areContentsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice) =
            oldItem.deviceAddress == newItem.deviceAddress
    }
}
