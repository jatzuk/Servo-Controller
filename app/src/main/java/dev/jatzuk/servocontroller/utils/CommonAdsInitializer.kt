package dev.jatzuk.servocontroller.utils

import android.content.Context

interface CommonAdsInitializer {

    fun initializeAds(context: Context)

    fun <T : Any> provideAdRequest(): T
}
