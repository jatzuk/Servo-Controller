package dev.jatzuk.servocontroller.ui

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.launchMainActivityInHiltContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@HiltAndroidTest
class DrawerLayoutNavigationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        launchMainActivityInHiltContainer()

        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.LEFT)))
            .perform(DrawerActions.open())
    }

    @Test
    fun navigateFromHomeFragment_toDevicesFragment() {
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.devicesFragment))

        onView(withId(R.id.layout_constraint))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateFromHomeFragment_toSettingsFragment() {
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.settingsFragment))

        onView(withText(R.string.transmission_mode))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateFromDevicesFragment_toHomeFragment() {
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.devicesFragment))

        onView(withId(R.id.layout_constraint))
            .check(matches(isDisplayed()))

        pressBack()

        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateFromSettingsFragment_toHomeFragment() {
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.settingsFragment))

        onView(withText(R.string.transmission_mode))
            .check(matches(isDisplayed()))

        pressBack()

        onView(withId(R.id.drawer_content))
            .check(matches(isDisplayed()))
    }
}
