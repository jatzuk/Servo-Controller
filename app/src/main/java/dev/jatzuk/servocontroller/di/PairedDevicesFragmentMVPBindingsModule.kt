package dev.jatzuk.servocontroller.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.mvp.devicesFragment.paired.PairedDevicesFragmentContract
import dev.jatzuk.servocontroller.mvp.devicesFragment.paired.PairedDevicesFragmentPresenter
import dev.jatzuk.servocontroller.ui.PairedDevicesFragment

@Module
@InstallIn(FragmentComponent::class)
abstract class PairedDevicesFragmentMVPBindingsModule {

    @Binds
    abstract fun bindFragmentView(fragment: PairedDevicesFragment): PairedDevicesFragmentContract.View

    @Binds
    abstract fun bindPresenter(presenter: PairedDevicesFragmentPresenter): PairedDevicesFragmentContract.Presenter
}
