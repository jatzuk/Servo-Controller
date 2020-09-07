package dev.jatzuk.servocontroller.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentPresenter
import dev.jatzuk.servocontroller.ui.DevicesFragment

@Module
@InstallIn(FragmentComponent::class)
abstract class DevicesFragmentMVPBindingsModule {

    @Binds
    abstract fun bindFragmentView(fragment: DevicesFragment): DevicesFragmentContract.View

    @Binds
    abstract fun bindPresenter(presenter: DevicesFragmentPresenter): DevicesFragmentContract.Presenter
}
