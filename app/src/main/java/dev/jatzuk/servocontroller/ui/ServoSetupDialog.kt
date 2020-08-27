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
import javax.inject.Inject

private const val TAG = "ServoSetupDialog"
private const val POSITION_ARG_KEY = "POSITION_ARG_KEY"

@AndroidEntryPoint
class ServoSetupDialog : DialogFragment(), ServoSetupDialogContract.View {

    private var binding: DialogServoSettingsBinding? = null

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
            binding!!.servo = it
            binding!!.radioGroup.check(it.sendBehaviour.toResourceId())
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
            radioGroup.check(resourceId)
        }
    }

    override fun onStop() {
        with(binding!!) {
            presenter.processSave(
                arrayOf(
                    etCommand.text.toString(),
                    etTag.text.toString(),
                    radioGroup.checkedRadioButtonId.toString()
                )
            )
        }
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun closeDialogView() {
        dismiss()
    }

    companion object {

        fun newInstance(position: Int) = ServoSetupDialog().apply {
            arguments = Bundle().apply { putInt(POSITION_ARG_KEY, position) }
        }
    }
}
