@file:Suppress("DEPRECATION")

package com.triple5.quizdown

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.triple5.quizdown.connector.OpenTrivia

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.triple5.quizdown", appContext.packageName)
    }

    @Test
    fun getCategories() {
        val openTrivia: OpenTrivia = OpenTrivia()
        val categories = openTrivia.getCategories()
        assertNotNull(categories)
        assertTrue(categories.isNotEmpty())
    }
}
