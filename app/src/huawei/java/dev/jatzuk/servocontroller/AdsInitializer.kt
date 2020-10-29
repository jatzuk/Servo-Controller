package dev.jatzuk.servocontroller

import android.content.Context
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import dev.jatzuk.servocontroller.utils.CommonAdsInitializer

class AdsInitializer : CommonAdsInitializer {

    override fun initializeAds(context: Context) {
        HwAds.init(context)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> provideAdRequest() = AdParam.Builder().build() as T
}
