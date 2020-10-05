package dev.jatzuk.servocontroller.mvp.homeFragment

import androidx.fragment.app.Fragment
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.ConnectionState

interface Strategy {

    fun updateView()
}

interface ButtonTextProvider {

    fun provideButtonText(): String
}

abstract class AbstractConnectionStrategy(
    protected val presenter: HomeFragmentPresenter
) : Strategy, ButtonTextProvider {

    fun getContext() = (presenter.view as Fragment).requireContext()
}

class ConnectionStrategy {

    var currentStrategy: Strategy? = null
        set(value) {
            field = value.also { it?.updateView() }
        }
}

class UnsupportedConnectionTypeStrategy(
    presenter: HomeFragmentPresenter
) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        val context = (presenter.view as Fragment).requireContext()
        val connectionTypeString = context.getString(R.string.connection_type)
        val unsupportedString = context.getString(R.string.unsupported)
        presenter.view?.apply {
            showAnimation(R.raw.animation_connection_type_unsupported)
            updateSelectedDeviceHint(true, connectionTypeString to unsupportedString)
            updateConnectionButton(provideButtonText())
        }
    }

    override fun provideButtonText() = getContext().getString(R.string.change_connection_type)
}

class OffStrategy(presenter: HomeFragmentPresenter) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        presenter.view?.apply {
            setRecyclerViewVisibility(false)
            updateConnectionButton(provideButtonText())
            updateSelectedDeviceHint(false)
            showAnimation(R.raw.bluetooth_enable)
        }
    }

    override fun provideButtonText() =
        getContext().getString(R.string.enable, presenter.connection.getConnectionType().name)
}

class OnStrategy(
    presenter: HomeFragmentPresenter,
    private val isSelectedDeviceNull: Boolean
) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        presenter.view?.apply {
            if (isSelectedDeviceNull) {
                updateSelectedDeviceHint(
                    true,
                    getContext().getString(R.string.no_device_selected) to ""
                )
            } else {
                updateConnectionMenuIconVisibility(true)
                updateSelectedDeviceHint(
                    true,
                    presenter.connection.getSelectedDeviceCredentials()
                )
            }

            updateConnectionButton(provideButtonText())
            stopAnimation()
        }
    }

    override fun provideButtonText(): String {
        val resourceId = if (isSelectedDeviceNull) R.string.select_device else R.string.connect
        return getContext().getString(resourceId)
    }
}

class ConnectingStrategy(presenter: HomeFragmentPresenter) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        presenter.view?.apply {
            showAnimation(R.raw.bluetooth_loop)
            updateConnectionButton(provideButtonText())
            updateSelectedDeviceHint(true)
        }
    }

    override fun provideButtonText() = getContext().getString(R.string.cancel)
}

class ConnectedStrategy(
    presenter: HomeFragmentPresenter,
    private val shouldShowAnimation: Boolean = true
) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        presenter.view?.apply {
            if (shouldShowAnimation) {
                showAnimation(R.raw.bluetooth_connected, 1f, 1000) {
                    setRecyclerViewVisibility(true)
                }
            } else {
                stopAnimation()
                setRecyclerViewVisibility(true)
            }

            updateConnectionStateIcon(presenter.getIconBasedOnConnectionType())
            updateConnectionButton(provideButtonText(), false)
            updateSelectedDeviceHint(false)
        }
    }

    override fun provideButtonText() = getContext().getString(R.string.disconnect)
}

class DisconnectingStrategy(
    presenter: HomeFragmentPresenter
) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        presenter.view?.apply {
            setRecyclerViewVisibility(false)
            updateConnectionButton(provideButtonText())
            updateSelectedDeviceHint(true)
        }
    }

    override fun provideButtonText() = getContext().getString(R.string.connect)
}

class DisconnectedStrategy(
    presenter: HomeFragmentPresenter
) : AbstractConnectionStrategy(presenter) {

    override fun updateView() {
        presenter.view?.apply {
            showAnimation(R.raw.animation_failure, 0.5f, 1500) {
                presenter.connection.connectionState.postValue(ConnectionState.ON)
            }
            updateConnectionStateIcon(presenter.getIconBasedOnConnectionType())
            setRecyclerViewVisibility(false)
            updateConnectionButton(provideButtonText())
            updateSelectedDeviceHint(true)
        }
    }

    override fun provideButtonText() = getContext().getString(R.string.connect)
}
