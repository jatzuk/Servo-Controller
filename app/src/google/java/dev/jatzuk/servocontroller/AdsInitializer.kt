package dev.jatzuk.servocontroller

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dev.jatzuk.servocontroller.utils.CommonAdsInitializer

class AdsInitializer : CommonAdsInitializer {

    override fun initializeAds(context: Context) {
        MobileAds.initialize(context) { }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> provideAdRequest() = AdRequest.Builder().build() as T
}
