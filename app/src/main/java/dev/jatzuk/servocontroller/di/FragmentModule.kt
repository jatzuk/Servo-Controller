package dev.jatzuk.servocontroller.di

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.ConnectionFactory
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.ui.HomeFragment

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun bindFragment(fragment: Fragment): HomeFragment = fragment as HomeFragment

    @Provides
    fun provideConnectionType(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): ConnectionType {
        val connectionString = sharedPreferences.getString(
            context.getString(R.string.key_connection_type), ConnectionType.BLUETOOTH.name
        )!!
        return if (connectionString.startsWith('B')) ConnectionType.BLUETOOTH
        else ConnectionType.WIFI
    }

    @Provides
    fun provideConnection(
        connectionType: ConnectionType
    ) = ConnectionFactory.getConnection(connectionType)
}
