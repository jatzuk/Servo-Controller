package dev.jatzuk.servocontroller.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.ui.ServoSetupDialog

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun provideHomeFragment(fragment: Fragment) = fragment as HomeFragment

    @Provides
    fun provideServoSetupDialog(fragment: Fragment) = fragment as ServoSetupDialog
}
