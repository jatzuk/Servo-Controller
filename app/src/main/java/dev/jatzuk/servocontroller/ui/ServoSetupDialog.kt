package dev.jatzuk.servocontroller.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.databinding.DialogServoSettingsBinding
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogContract
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogPresenter
import dev.jatzuk.servocontroller.other.Servo
import javax.inject.Inject

private const val POSITION_ARG_KEY = "POSITION_ARG_KEY"

@AndroidEntryPoint
class ServoSetupDialog : DialogFragment(), ServoSetupDialogContract.View {

    private var binding: DialogServoSettingsBinding? = null
    private lateinit var callback: () -> Unit

    @Inject
    lateinit var presenter: ServoSetupDialogContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.assignPositionFromArguments(requireArguments().getInt(POSITION_ARG_KEY))
        assignView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogServoSettingsBinding.inflate(inflater, container, false)

        presenter.getLiveData().observe(viewLifecycleOwner) {
            binding!!.apply {
                servo = it
                radioGroupWriteMode.check(it.writeMode.toResourceId())
                radioGroupSendMode.check(it.sendMode.toResourceId())
            }
        }

        binding!!.buttonRestoreDefaults.setOnClickListener {
            presenter.onRestoreDefaultsClicked()
        }

        requireDialog().window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding!!.root
    }

    override fun assignView() {
        (presenter as ServoSetupDialogPresenter).view = this
    }

    override fun setValues(order: Int, command: String, @IdRes resourceId: Int) {
        binding?.apply {
            etCommand.setText(command)
            etTag.setText(order.toString())
            radioGroupSendMode.check(resourceId)
        }
    }

    override fun onStop() {
        with(binding!!) {
            presenter.processSave(
                arrayOf(
                    etCommand.text.toString(),
                    etTag.text.toString(),
                    radioGroupWriteMode.checkedRadioButtonId.toString(),
                    radioGroupSendMode.checkedRadioButtonId.toString()
                )
            )
        }
        presenter.invokeOnStopCallback(callback)
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun closeDialogView() {
        dismiss()
    }

    fun onClosed(callback: () -> Unit) {
        this.callback = callback
    }

    fun getUpdatedServo(): Servo {
        return presenter.getLiveData().value!!
    }

    companion object {

        fun newInstance(position: Int) = ServoSetupDialog().apply {
            arguments = Bundle().apply { putInt(POSITION_ARG_KEY, position) }
        }
    }
}
