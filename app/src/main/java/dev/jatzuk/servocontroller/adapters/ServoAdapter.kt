package dev.jatzuk.servocontroller.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.databinding.ItemSeekBarBinding
import dev.jatzuk.servocontroller.databinding.ItemServoBinding
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.other.ServoTexture
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.ServoView

private const val TAG = "ServoAdapter"

class ServoAdapter(
    private val textureType: ServoTexture
) : ListAdapter<Servo, ServoAdapter.AbstractViewHolder>(ServoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (textureType) {
        ServoTexture.TEXTURE -> ServoViewHolder.from(parent)
        ServoTexture.SEEKBAR -> SeekViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    abstract class AbstractViewHolder(
        protected val view: View,
        protected val fragment: HomeFragment
    ) : RecyclerView.ViewHolder(view) {

        abstract fun bind(servo: Servo)
    }

    class ServoViewHolder private constructor(
        private val binding: ItemServoBinding,
        fragment: HomeFragment
    ) : AbstractViewHolder(binding.root, fragment) {

        init {
            binding.servoView.onSetupClickListener = ServoViewOnClickListener()
        }

        override fun bind(servo: Servo) {
            binding.servoView.tag = servo.tag
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

    class SeekViewHolder private constructor(
        private val binding: ItemSeekBarBinding,
        fragment: HomeFragment
    ) : AbstractViewHolder(binding.root, fragment) {

        init {
            binding.ivSetup.setOnClickListener {
                fragment.presenter.onServoSettingsTapped(layoutPosition)
            }
        }

        override fun bind(servo: Servo) {
            binding.servo = servo
            binding.tvTag.text = servo.tag
            binding.executePendingBindings()
        }

        companion object {

            fun from(parent: ViewGroup): SeekViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSeekBarBinding.inflate(layoutInflater, parent, false)
                return SeekViewHolder(binding, parent.findFragment())
            }
        }
    }

    class ServoDiffCallback : DiffUtil.ItemCallback<Servo>() {

        override fun areItemsTheSame(oldItem: Servo, newItem: Servo) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Servo, newItem: Servo) =
            oldItem.hashCode() == newItem.hashCode()
    }
}
