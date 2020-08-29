package dev.jatzuk.servocontroller.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.databinding.ItemServoBinding
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.ServoView

private const val TAG = "ServoAdapter"

class ServoAdapter : ListAdapter<Servo, ServoAdapter.ServoViewHolder>(ServoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ServoViewHolder.from(parent)

    override fun onBindViewHolder(holder: ServoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ServoViewHolder private constructor(
        private val binding: ItemServoBinding,
        private val fragment: HomeFragment
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.servoView.onSetupClickListener = ServoViewOnClickListener()
        }

        fun bind(item: Servo) {
            binding.servoView.tag = item.tag
        }

        private inner class ServoViewOnClickListener : ServoView.OnSetupClickListener {

            override fun onSetupAreaClicked() {
                fragment.presenter.onServoSettingsTapped(layoutPosition)
            }

            override fun onFinalPositionDetected(position: Int) {
                fragment.presenter.onFinalPositionDetected(layoutPosition, position)
            }
        }

        companion object {

            fun from(parent: ViewGroup): ServoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemServoBinding.inflate(layoutInflater, parent, false)
                return ServoViewHolder(binding, parent.findFragment())
            }
        }
    }

    class ServoDiffCallback : DiffUtil.ItemCallback<Servo>() {

        override fun areItemsTheSame(oldItem: Servo, newItem: Servo) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Servo, newItem: Servo) =
            oldItem.hashCode() == newItem.hashCode()
    }
}
