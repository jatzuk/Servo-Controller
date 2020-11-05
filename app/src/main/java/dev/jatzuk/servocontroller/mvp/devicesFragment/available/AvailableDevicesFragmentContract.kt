package dev.jatzuk.servocontroller.mvp.devicesFragment.available

import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.RawRes
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.databinding.LayoutLottieAnimationViewButtonBinding
import dev.jatzuk.servocontroller.mvp.BasePresenter
import dev.jatzuk.servocontroller.mvp.BaseView

interface AvailableDevicesFragmentContract {

    interface Presenter : BasePresenter {

        fun onViewCreated(layoutScanAvailableDevices: LayoutLottieAnimationViewButtonBinding)

        fun setupRecyclerView(recyclerView: RecyclerView)

        fun getAvailableDevices(): LiveData<List<Parcelable>>

        fun onScanAvailableDevicesPressed()

        fun onConnectionIconPressed()

        fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)

        fun permissionGranted()

        fun permissionDenied()

        fun onStop()
    }

    interface View : BaseView<Presenter> {

        fun showAnimation(
            @RawRes resourceId: Int,
            speed: Float = 1f,
            timeout: Long = 0L,
            afterAnimationAction: (() -> Unit)? = null
        )

        fun stopAnimation()

        fun updateButton(text: String, isVisible: Boolean = true)

        override fun assignPresenter(presenter: Presenter) {}
    }
}
