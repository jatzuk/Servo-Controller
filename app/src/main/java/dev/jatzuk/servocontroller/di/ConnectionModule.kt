package dev.jatzuk.servocontroller.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dev.jatzuk.servocontroller.connection.ConnectionFactory
import dev.jatzuk.servocontroller.utils.SettingsHolder

@Module
@InstallIn(ActivityRetainedComponent::class)
object ConnectionModule {

    @ActivityRetainedScoped
    @Provides
    fun provideConnection(
        @ApplicationContext context: Context,
        settingsHolder: SettingsHolder
    ) = ConnectionFactory.getConnection(context, settingsHolder.connectionType)
}
