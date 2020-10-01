package dev.jatzuk.servocontroller.ui

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import dev.jatzuk.servocontroller.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
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
