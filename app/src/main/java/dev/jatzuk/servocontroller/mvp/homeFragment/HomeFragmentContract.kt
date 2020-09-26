package dev.jatzuk.servocontroller.mvp.homeFragment

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView
import dev.jatzuk.servocontroller.other.Servo

interface HomeFragmentContract {

    interface Strategy {

        fun updateView()
    }

    class ConnectionStrategy(presenter: HomeFragmentPresenter) {

        var currentStrategy: Strategy = OffStrategy(presenter)
            set(value) {
                field = value.also { it.updateView() }
            }
    }

    class UnsupportedConnectionTypeStrategy(
        private val presenter: HomeFragmentPresenter,

        ) : Strategy {

        override fun updateView() {
            val context = (presenter.view as Fragment).requireContext()
            val connectionTypeString = context.getString(R.string.connection_type)
            val unsupportedString = context.getString(R.string.unsupported)
            presenter.view?.apply {
                showAnimation(R.raw.animation_connection_type_unsupported)
                updateSelectedDeviceHint(true, connectionTypeString to unsupportedString)
                updateConnectionButton(
                    (this as Fragment).requireContext().getString(R.string.change_connection_type)
                )
            }
        }
    }

    class OffStrategy(private val presenter: HomeFragmentPresenter) : Strategy {

        private val buttonText =
            (presenter.view as Fragment).requireContext()
                .getString(R.string.enable, presenter.connection.getConnectionType().name)

        override fun updateView() {
            presenter.view?.apply {
                setRecyclerViewVisibility(false)
                updateConnectionButton(buttonText)
                updateSelectedDeviceHint(false)
                showAnimation(R.raw.bluetooth_enable)
            }

        }
    }

    class OnStrategy(
        private val presenter: HomeFragmentPresenter,
        private val isSelectedDeviceNull: Boolean
    ) : Strategy {

        private var buttonText: String = if (isSelectedDeviceNull) {
            (presenter.view as Fragment).requireContext().getString(R.string.no_device_selected)
        } else (presenter.view as Fragment).requireContext().getString(R.string.connect)

        override fun updateView() {
            val context = (presenter.view as Fragment).requireContext()
            val buttonTextStringResource =
                if (isSelectedDeviceNull) R.string.no_device_selected else R.string.connect
            buttonText = context.getString(buttonTextStringResource)

            presenter.view?.apply {
                if (isSelectedDeviceNull) {
                    updateSelectedDeviceHint(
                        true,
                        context.getString(R.string.no_device_selected) to ""
                    )
                } else {
                    updateConnectionMenuIconVisibility(true)
                    updateSelectedDeviceHint(
                        true,
                        presenter.connection.getSelectedDeviceCredentials()
                    )
                }

                updateConnectionButton(buttonText)
                stopAnimation()
            }
        }
    }

    class ConnectingStrategy(private val presenter: HomeFragmentPresenter) : Strategy {

        private val buttonText =
            (presenter.view as Fragment).requireContext().getString(R.string.cancel)

        override fun updateView() {
            presenter.view?.apply {
                showAnimation(R.raw.bluetooth_loop)
                updateConnectionButton(buttonText)
                updateSelectedDeviceHint(true)
            }
        }
    }

    class ConnectedStrategy(
        private val presenter: HomeFragmentPresenter,
        private val isWasConnected: Boolean
    ) : Strategy {

        private val buttonText =
            (presenter.view as Fragment).requireContext().getString(R.string.disconnect)

        override fun updateView() {
            presenter.view?.apply {
                if (isWasConnected) {
                    setRecyclerViewVisibility(true)
                    stopAnimation()
                } else {
                    updateConnectionStateIcon(presenter.getIconBasedOnConnectionType())
                    showAnimation(R.raw.bluetooth_connected, 1f, 1000) {
                        setRecyclerViewVisibility(true)
                    }
                }
                updateConnectionButton(buttonText, false)
                updateSelectedDeviceHint(false)
            }

        }
    }

    class DisconnectingStrategy(private val presenter: HomeFragmentPresenter) : Strategy {

        private val buttonText =
            (presenter.view as Fragment).requireContext().getString(R.string.connect)

        override fun updateView() {
            presenter.view?.apply {
                setRecyclerViewVisibility(false)
                updateConnectionButton(buttonText)
                updateSelectedDeviceHint(true)
            }
        }
    }

    class DisconnectedStrategy(private val presenter: HomeFragmentPresenter) : Strategy {

        private val buttonText =
            (presenter.view as Fragment).requireContext().getString(R.string.connect)

        override fun updateView() {
            presenter.view?.apply {
                updateConnectionStateIcon(presenter.getIconBasedOnConnectionType())
                setRecyclerViewVisibility(false)
                showAnimation(R.raw.animation_failure, 0.5f, 2500)
                updateConnectionButton(buttonText)
                updateSelectedDeviceHint(true)
            }
        }
    }

    interface Presenter : BasePresenter {

        fun optionsMenuCreated()

        fun onViewCreated(savedInstanceState: Bundle?)

        fun onStart()

        fun onDestroyView()

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getRecyclerViewLayoutManager(): RecyclerView.LayoutManager

        fun onRequestEnableHardwareReceived()

        fun connectionIconPressed()

        fun connectionButtonPressed()

        fun requestConnectionHardwareButtonPressed()

        fun isConnectionTypeSupported(): Boolean

        fun isConnectionModuleEnabled(): Boolean

        fun isConnected(): Boolean

        fun requestConnectionHardware()

        fun onServoSettingsTapped(layoutPosition: Int)

        fun onFinalPositionDetected(layoutPosition: Int, angle: Int)

        fun buildDeviceList()

        fun connect()

        fun sendData(data: ByteArray): Boolean

        fun disconnect()

        fun onSaveInstanceState(outState: Bundle)
    }

    interface View : BaseView<Presenter> {

        fun showToast(message: String, length: Int = Toast.LENGTH_SHORT)

        fun updateConnectionStateIcon(@DrawableRes resourceId: Int)

        fun updateConnectionMenuIconVisibility(isVisible: Boolean)

        fun updateSelectedDeviceHint(isVisible: Boolean = true, pair: Pair<String, String>? = null)

        fun submitServosList(servos: List<Servo>)

        fun setRecyclerViewVisibility(isVisible: Boolean)

        fun showAnimation(
            @RawRes resourceId: Int,
            speed: Float = 1f,
            timeout: Long = 0L,
            afterTimeoutAction: (() -> Unit)? = null
        )

        fun stopAnimation()

        fun updateConnectionButton(text: String, isVisible: Boolean = true)

        fun navigateTo(id: Int)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
