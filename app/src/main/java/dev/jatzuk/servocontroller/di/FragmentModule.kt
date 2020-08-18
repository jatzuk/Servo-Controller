package dev.jatzuk.servocontroller.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.jatzuk.servocontroller.connection.ConnectionFactory
import dev.jatzuk.servocontroller.ui.HomeFragment
import dev.jatzuk.servocontroller.utils.SettingsHolder

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun bindFragment(fragment: Fragment): HomeFragment = fragment as HomeFragment

    @Provides
    fun provideConnection(
        settingsHolder: SettingsHolder
    ) = ConnectionFactory.getConnection(settingsHolder.connectionType)
}
