package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun testMainActivityStarts() {
        try {
            val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
            assert(activity != null)
            println("Activity started successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
