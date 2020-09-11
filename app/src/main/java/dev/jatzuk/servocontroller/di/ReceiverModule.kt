package dev.jatzuk.servocontroller.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dev.jatzuk.servocontroller.connection.receiver.BluetoothReceiver
import dev.jatzuk.servocontroller.connection.receiver.BluetoothScanningReceiver

@Module
@InstallIn(ActivityRetainedComponent::class)
object ReceiverModule {

    private const val TAG = "ReceiverModule"

    @ActivityRetainedScoped
    @Provides
    fun provideBluetoothReceiver() = BluetoothReceiver()

    @ActivityRetainedScoped
    @Provides
    fun provideBluetoothScanningReceiver() = BluetoothScanningReceiver()
}
