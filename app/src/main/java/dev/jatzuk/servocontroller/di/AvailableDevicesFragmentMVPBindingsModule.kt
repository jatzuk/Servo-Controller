package dev.jatzuk.servocontroller.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.mvp.devicesFragment.available.AvailableDevicesFragmentContract
import dev.jatzuk.servocontroller.mvp.devicesFragment.available.AvailableDevicesFragmentPresenter
import dev.jatzuk.servocontroller.ui.AvailableDevicesFragment

@Module
@InstallIn(FragmentComponent::class)
abstract class AvailableDevicesFragmentMVPBindingsModule {

    @Binds
    abstract fun bindFragment(fragment: AvailableDevicesFragment): AvailableDevicesFragmentContract.View

    @Binds
    abstract fun bindPresenter(presenter: AvailableDevicesFragmentPresenter): AvailableDevicesFragmentContract.Presenter
}
