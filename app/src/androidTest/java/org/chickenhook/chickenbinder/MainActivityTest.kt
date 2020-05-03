package org.chickenhook.chickenbinder

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun crashAndroidAPI29() {
        onView(withId(R.id.permissionTest)).perform(click())
    }


    @Test
    fun harmFulAppWarning() {
        onView(withId(R.id.packageManagerTest)).perform(click())
    }
}