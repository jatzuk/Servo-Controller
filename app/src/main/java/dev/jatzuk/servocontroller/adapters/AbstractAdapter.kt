package dev.jatzuk.servocontroller.adapters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.net.wifi.ScanResult
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

abstract class AbstractAdapter<T : Parcelable>(
    private val connectionType: ConnectionType? = null,
    private val onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, AbstractAdapter.AbstractDeviceViewHolder<T>>(diffUtilItemCallback) {

    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractDeviceViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
        return provideViewHolder(binding, connectionType, onSelectedDeviceClickListener)
    }

    final override fun onBindViewHolder(holder: AbstractDeviceViewHolder<T>, position: Int) {
        holder.bind(getItem(position))
    }

    abstract fun provideViewHolder(
        binding: ItemDeviceBinding,
        connectionType: ConnectionType? = null,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ): AbstractDeviceViewHolder<T>

    abstract class AbstractDeviceViewHolder<T> protected constructor(
        protected val binding: ItemDeviceBinding,
        connectionType: ConnectionType?,
        private val onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val bindingStrategy = connectionType?.let {
            when (it) {
                ConnectionType.BLUETOOTH -> BluetoothDeviceBindingStrategy()
                ConnectionType.WIFI -> WifiScanResultDeviceBindingStrategy()
            }
        }

        protected var defaultColor = 0

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(device: T) {
            bindingStrategy?.bind(device as Parcelable)
        }

        fun reset() {
            bindingStrategy?.reset()
        }

        final override fun onClick(v: View?) {
            onSelectedDeviceClickListener?.onClick(layoutPosition)
        }

        fun setSelectedColor() {
            bindingStrategy?.setSelected()
        }

        inner class BluetoothDeviceBindingStrategy : CommonBindingStrategy() {

            override var defaultColor = 0

            private var isPaired = false

            override fun bind(device: Parcelable) {
                device as BluetoothDevice
                binding.apply {
                    tvName.text = device.name
                    tvMacAddress.text = device.address
                }
            }

            override fun updateInfo(device: Parcelable) {
                isPaired = BluetoothAdapter.getDefaultAdapter().bondedDevices.contains(device)
                val colorRes = if (isPaired) R.color.colorPrimary else R.color.colorGrey
                defaultColor = ContextCompat.getColor(itemView.context, colorRes)
            }
        }

        inner class WifiP2PDeviceBindingStrategy : CommonBindingStrategy() {

            override var defaultColor = 0

            override fun bind(device: Parcelable) {
                device as WifiP2pDevice
                binding.apply {
                    tvName.text = device.deviceName
                    tvMacAddress.text = device.deviceAddress

                    tvStatus.text = "wifi p2p device"
                }

            }

            override fun updateInfo(device: Parcelable) {

            }
        }

        inner class WifiScanResultDeviceBindingStrategy : CommonBindingStrategy() {

            override var defaultColor = 0

            override fun bind(device: Parcelable) {
                device as ScanResult
                binding.apply {
                    tvName.text = device.SSID
                    tvMacAddress.text = device.BSSID

                    tvStatus.text = "WIFI STATUS"
//                    ivStatus.setColorFilter()

//                    ivDeviceIcon.setImageResource()
                }
            }

            override fun updateInfo(device: Parcelable) {
            }

        }

        abstract inner class CommonBindingStrategy : BindingStrategy {

            override var defaultColor: Int = 0

            abstract override fun bind(device: Parcelable)

            final override fun setSelected() {
                binding.apply {
                    tvStatus.text = itemView.context.getString(R.string.selected)
                    ivStatus.setColorFilter(Color.GREEN)
                }
            }

            abstract override fun updateInfo(device: Parcelable)

            final override fun reset() {
                binding.apply {
                    tvStatus.text = "some common text here"
                    ivStatus.setColorFilter(defaultColor)
                }
            }
        }
    }

    abstract class AbstractDevicesDiffCallback<T> : DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

        abstract override fun areContentsTheSame(oldItem: T, newItem: T): Boolean
    }

    interface OnSelectedDeviceClickListener {

        fun onClick(position: Int)
    }

    interface BindingStrategy {

        var defaultColor: Int

        fun bind(device: Parcelable)

        fun setSelected()

        fun updateInfo(device: Parcelable)

        fun reset()
    }
}
