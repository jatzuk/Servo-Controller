package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.FragmentDevicesBinding
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import javax.inject.Inject

private const val TAG = "DevicesFragment"

@AndroidEntryPoint
class DevicesFragment : Fragment(R.layout.fragment_devices), DevicesFragmentContract.View {

    private var binding: FragmentDevicesBinding? = null

    @Inject
    lateinit var presenter: DevicesFragmentContract.Presenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDevicesBinding.bind(view)
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
