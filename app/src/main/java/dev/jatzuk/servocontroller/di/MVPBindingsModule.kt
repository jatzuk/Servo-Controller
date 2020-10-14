package dev.jatzuk.servocontroller.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentContract
import dev.jatzuk.servocontroller.mvp.devicesFragment.DevicesFragmentPresenter
import dev.jatzuk.servocontroller.mvp.devicesFragment.available.AvailableDevicesFragmentContract
import dev.jatzuk.servocontroller.mvp.devicesFragment.available.AvailableDevicesFragmentPresenter
import dev.jatzuk.servocontroller.mvp.devicesFragment.paired.PairedDevicesFragmentContract
import dev.jatzuk.servocontroller.mvp.devicesFragment.paired.PairedDevicesFragmentPresenter
import dev.jatzuk.servocontroller.mvp.homeFragment.HomeFragmentContract
import dev.jatzuk.servocontroller.mvp.homeFragment.HomeFragmentPresenter
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogContract
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogPresenter
import dev.jatzuk.servocontroller.ui.*

@Module
@InstallIn(FragmentComponent::class)
abstract class MVPBindingsModule {

    @FragmentScoped
    @Binds
    abstract fun bindAvailableDevicesFragmentView(
        fragment: AvailableDevicesFragment
    ): AvailableDevicesFragmentContract.View

    @FragmentScoped
    @Binds
    abstract fun bindAvailableDevicesFragmentPresenter(
        presenter: AvailableDevicesFragmentPresenter
    ): AvailableDevicesFragmentContract.Presenter

    @FragmentScoped
    @Binds
    abstract fun bindDevicesFragmentView(
        fragment: DevicesFragment
    ): DevicesFragmentContract.View

    @FragmentScoped
    @Binds
    abstract fun bindDevicesFragmentPresenter(
        presenter: DevicesFragmentPresenter
    ): DevicesFragmentContract.Presenter


    @FragmentScoped
    @Binds
    abstract fun bindHomeFragmentView(
        fragment: HomeFragment
    ): HomeFragmentContract.View

    @FragmentScoped
    @Binds
    abstract fun bindHomeFragmentPresenter(
        presenter: HomeFragmentPresenter
    ): HomeFragmentContract.Presenter


    @FragmentScoped
    @Binds
    abstract fun bindPairedDevicesFragmentView(
        fragment: PairedDevicesFragment
    ): PairedDevicesFragmentContract.View

    @FragmentScoped
    @Binds
    abstract fun bindPairedDevicesFragmentPresenter(
        presenter: PairedDevicesFragmentPresenter
    ): PairedDevicesFragmentContract.Presenter

    @FragmentScoped
    @Binds
    abstract fun bindServoSetupDialogFragmentView(
        dialogFragment: ServoSetupDialog
    ): ServoSetupDialogContract.View

    @FragmentScoped
    @Binds
    abstract fun bindServoSetupDialogFragmentPresenter(
        presenter: ServoSetupDialogPresenter
    ): ServoSetupDialogContract.Presenter
}
