package dev.jatzuk.servocontroller.adapters

import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.ItemDeviceBinding

abstract class AbstractAdapter<T : Parcelable>(
    private val onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, AbstractAdapter.AbstractDeviceViewHolder<T>>(diffUtilItemCallback) {

    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractDeviceViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
        return provideViewHolder(binding, onSelectedDeviceClickListener)
    }

    final override fun onBindViewHolder(holder: AbstractDeviceViewHolder<T>, position: Int) {
        holder.bind(getItem(position))
    }

    abstract fun provideViewHolder(
        binding: ItemDeviceBinding,
        onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ): AbstractDeviceViewHolder<T>

    abstract class AbstractDeviceViewHolder<T> protected constructor(
        protected val binding: ItemDeviceBinding,
        private val onSelectedDeviceClickListener: OnSelectedDeviceClickListener? = null
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        protected var defaultColor = 0

        init {
            itemView.setOnClickListener(this)
        }

        abstract fun bind(device: T)

        final override fun onClick(v: View?) {
            onSelectedDeviceClickListener?.onClick(layoutPosition)
        }

        fun setSelectedColor() {
            binding.apply {
                ivStatus.setColorFilter(Color.GREEN)
                tvStatus.text = itemView.context.getString(R.string.selected)
            }
        }

        fun resetColor() {
            binding.apply {
                ivStatus.setColorFilter(defaultColor)
                tvStatus.text = itemView.context.getString(R.string.paired)
            }
        }

        protected abstract fun provideDefaultColor()
    }

    abstract class AbstractDevicesDiffCallback<T> : DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

        abstract override fun areContentsTheSame(oldItem: T, newItem: T): Boolean
    }

    interface OnSelectedDeviceClickListener {

        fun onClick(position: Int)
    }
}
