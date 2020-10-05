package dev.jatzuk.servocontroller.ui

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.jatzuk.servocontroller.LottieAnimationViewDrawableMatcher.hasAnimationResource
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.launchFragmentInHiltContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@HiltAndroidTest
class HomeFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @Before
    fun setUp() {
        launchFragmentInHiltContainer<HomeFragment>()

        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun whenConnectionTypeUnsupported_thenShowChangeConnectionTypeTitleOnButton() {
        onView(withId(R.id.button))
            .check(matches(withText(context.getString(R.string.change_connection_type))))
    }

    @Test
    fun whenConnectionTypeUnsupported_thenShowUnsupportedAnimation() {
        onView(withId(R.id.lav))
            .check(matches(hasAnimationResource(R.raw.animation_connection_type_unsupported)))
    }
}
