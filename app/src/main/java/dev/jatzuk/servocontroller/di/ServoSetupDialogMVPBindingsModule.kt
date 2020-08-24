package dev.jatzuk.servocontroller.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogContract
import dev.jatzuk.servocontroller.mvp.servoSetupDialog.ServoSetupDialogPresenter
import dev.jatzuk.servocontroller.ui.ServoSetupDialog

@Module
@InstallIn(FragmentComponent::class)
abstract class ServoSetupDialogMVPBindingsModule {

    @Binds
    abstract fun bindDialogFragmentView(dialogFragment: ServoSetupDialog): ServoSetupDialogContract.View

    @Binds
    abstract fun bindPresenter(presenter: ServoSetupDialogPresenter): ServoSetupDialogContract.Presenter
}
