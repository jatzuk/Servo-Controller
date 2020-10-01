package dev.jatzuk.servocontroller

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.platform.app.InstrumentationRegistry
import com.airbnb.lottie.LottieAnimationView
import org.hamcrest.Description

object LottieAnimationViewDrawableMatcher {

    fun hasAnimationResource(resourceId: Int) =
        object : BoundedMatcher<View, LottieAnimationView>(LottieAnimationView::class.java) {

            override fun describeTo(description: Description?) {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                val resourceName = context.resources.getResourceEntryName(resourceId)
                description?.appendText("$resourceName with id: $resourceId")
            }

            override fun matchesSafely(item: LottieAnimationView?) = item?.let {
                val field = it::class.java.getDeclaredField("animationResId")
                field.isAccessible = true
                field.get(it) == resourceId
            } ?: false
        }
}
