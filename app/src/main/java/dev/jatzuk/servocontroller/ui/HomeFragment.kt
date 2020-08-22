package dev.jatzuk.servocontroller.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.adapters.ServoAdapter
import dev.jatzuk.servocontroller.databinding.FragmentHomeBinding
import dev.jatzuk.servocontroller.mvp.homefragment.HomeFragmentContract
import dev.jatzuk.servocontroller.other.REQUEST_ENABLE_BT
import dev.jatzuk.servocontroller.other.Servo
import dev.jatzuk.servocontroller.utils.BottomPaddingDecoration
import javax.inject.Inject

private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment
    : Fragment(R.layout.fragment_home), ServoView.OnSetupClickListener, HomeFragmentContract.View {

    private var binding: FragmentHomeBinding? = null
    private lateinit var connectionIcon: MenuItem
    private lateinit var servoAdapter: ServoAdapter

    @Inject
    lateinit var presenter: HomeFragmentContract.Presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()

        if (presenter.isConnectionTypeSupported() && !presenter.isConnected()) {
            presenter.requestConnectionHardware()
        }

        presenter.notifyViewCreated()
    }

    private fun setupRecyclerView() {
        binding!!.recyclerView.apply {
            servoAdapter = ServoAdapter()
            adapter = servoAdapter
            layoutManager = presenter.getRecyclerViewLayoutManager()
            addItemDecoration(BottomPaddingDecoration(requireContext()))
        }
        presenter.onReadyToRequestServosList() // FIXME: 18/08/2020 ???
    }

    override fun submitServosList(servos: List<Servo>) {
        servoAdapter.submitList(servos)
    }

    override fun onSetupAreaClicked() {
        showServoSettingsDialog()
    }

    override fun onFinalPositionDetected(position: Int) {
        presenter.onFinalPositionDetected(position)
    }

    override fun showServoSettingsDialog() {
        presenter.onServoSettingsTapped()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ENABLE_BT -> presenter.onBTRequestEnableReceived()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
        connectionIcon = menu.getItem(0)
        presenter.optionsMenuCreated()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.ic_connection_action -> {
            presenter.connectionIconPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun updateConnectionStateIcon(@DrawableRes resourceId: Int) {
        connectionIcon.setIcon(resourceId)
    }

    override fun showToast(message: String, length: Int) {
        Toast.makeText(requireContext(), message, length).show()
    }

    override fun updateConnectionMenuIconVisibility(isVisible: Boolean) {
        connectionIcon.isVisible = isVisible
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
