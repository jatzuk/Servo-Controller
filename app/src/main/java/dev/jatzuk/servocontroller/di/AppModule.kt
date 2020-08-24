package dev.jatzuk.servocontroller.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.jatzuk.servocontroller.db.ServoDatabase
import dev.jatzuk.servocontroller.other.SERVOS_DATABASE_NAME
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME
import dev.jatzuk.servocontroller.utils.SettingsHolder
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideSettingsHolder(@ApplicationContext context: Context) = SettingsHolder(context)

    @Provides
    fun provideServoDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        ServoDatabase::class.java,
        SERVOS_DATABASE_NAME
    ).build()

    @Provides
    fun provideServoDao(db: ServoDatabase) = db.getServoDao()
}
