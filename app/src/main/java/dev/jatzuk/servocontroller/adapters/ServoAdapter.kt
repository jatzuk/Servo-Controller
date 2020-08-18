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

class ServoAdapter : ListAdapter<Servo, ServoAdapter.ServoViewHolder>(ServoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ServoViewHolder.from(parent)

    override fun onBindViewHolder(holder: ServoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ServoViewHolder private constructor(
        val binding: ItemServoBinding,
        fragment: HomeFragment
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.servoView.onSetupClickListener = fragment
        }

        fun bind(item: Servo) {
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
            oldItem.servoOrder == newItem.servoOrder
    }
}
