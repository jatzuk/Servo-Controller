package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.FragmentPairedDevicesBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.paired.PairedDevicesFragmentContract
import javax.inject.Inject

@AndroidEntryPoint
class PairedDevicesFragment
    : Fragment(R.layout.fragment_paired_devices), PairedDevicesFragmentContract.View {

    private var binding: FragmentPairedDevicesBinding? = null

    @Inject
    lateinit var presenter: PairedDevicesFragmentContract.Presenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPairedDevicesBinding.bind(view)

        presenter.setupRecyclerView(binding!!.recyclerView)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
