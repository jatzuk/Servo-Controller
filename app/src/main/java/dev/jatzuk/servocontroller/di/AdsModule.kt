package dev.jatzuk.servocontroller.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dev.jatzuk.servocontroller.AdsInitializer
import dev.jatzuk.servocontroller.utils.CommonAdsInitializer

@Module
@InstallIn(ActivityComponent::class)
object AdsModule {

    @ActivityScoped
    @Provides
    fun provideAdsInitializer(): CommonAdsInitializer = AdsInitializer()
}
