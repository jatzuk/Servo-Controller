package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.databinding.DialogServoSettingsBinding
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogContract
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogServoSettingsBinding.inflate(inflater, container, false)
        presenter.getLiveData().observe(viewLifecycleOwner) {
            binding!!.servo = it
        }
        return binding!!.root
    }

    override fun onStop() {
        presenter.processSave(
            arrayOf(
                binding!!.etCommand.text.toString(),
                binding!!.etTag.text.toString()
            )
        )
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {

        fun newInstance(position: Int) = ServoSetupDialog().apply {
            arguments = Bundle().apply { putInt(POSITION_ARG_KEY, position) }
        }
    }
}
