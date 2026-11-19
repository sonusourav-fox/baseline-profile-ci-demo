package com.example.baselineprofiledemo.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE_NAME = "com.example.baselineprofiledemo"

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.desc("home_title")), 5_000)
    }

    @Test
    fun homeFeedScroll() = rule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = false,
    ) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.desc("home_feed")), 5_000)

        repeat(3) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.75).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.25).toInt(),
                20,
            )
            device.waitForIdle()
        }
    }
}
