package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import dev.jatzuk.servocontroller.databinding.DialogServoSettingsBinding

class ServoSetupDialog : DialogFragment() {

    private var binding: DialogServoSettingsBinding? = null

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
//        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
//            .setTitle("Setup")
//            .setMessage("ffs")
//            .setIcon(R.drawable.ic_bluetooth)
//            .setPositiveButton("Apply") { _, _ -> }
//            .setNegativeButton("Cancel") { _, _ -> }
//            .create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogServoSettingsBinding.inflate(inflater, container, false)
        return binding!!.root

//        val view = inflater.inflate(R.layout.dialog_servo_settings, container, false)
//        dialog?.setTitle("Title")
//        return view
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
