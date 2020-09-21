package dev.jatzuk.servocontroller.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver

@Module
@InstallIn(ActivityRetainedComponent::class)
object ReceiverModule {

    private const val TAG = "ReceiverModule"

    @ActivityRetainedScoped
    @Provides
    fun provideBluetoothReceiver(connection: BluetoothConnection) = BluetoothReceiver(connection)
}
