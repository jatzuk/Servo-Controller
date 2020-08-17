package dev.jatzuk.servocontroller.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.mvp.homefragment.HomeFragmentContract
import dev.jatzuk.servocontroller.mvp.homefragment.HomeFragmentPresenter
import dev.jatzuk.servocontroller.ui.HomeFragment

@Module
@InstallIn(FragmentComponent::class)
abstract class HomeFragmentMVPBindingsModule {

    @Binds
    abstract fun bindFragmentView(fragment: HomeFragment): HomeFragmentContract.View

    @Binds
    abstract fun bindPresenter(presenter: HomeFragmentPresenter): HomeFragmentContract.Presenter
}
