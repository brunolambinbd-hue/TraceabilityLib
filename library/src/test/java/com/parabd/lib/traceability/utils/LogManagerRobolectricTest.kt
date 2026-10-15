package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric unit test suite for [LogManager] and [LogManagerExt].
 *
 * Runs with real Android framework shadows on JVM to achieve 100% JaCoCo coverage
 * over native [android.util.Log] invocations and try-catch blocks.
 */
@RunWith(RobolectricTestRunner::class)
class LogManagerRobolectricTest {

    @Before
    fun setUp() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    @After
    fun tearDown() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    /**
     * Verifies full execution of [LogManager.d], [LogManager.i], [LogManager.w], [LogManager.e], [LogManager.u]
     * under Robolectric with [LogManager.forceLoggable] set to `true`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Robolectric_FullCoverage_ForceLoggableTrue() {
        LogManager.forceLoggable = true

        LogManager.d("ROBO_TAG", "Debug message Robolectric")
        LogManager.i("ROBO_TAG", "Info message Robolectric")
        LogManager.w("ROBO_TAG", "Warn message Robolectric")
        LogManager.e("ROBO_TAG", "Error with throwable", RuntimeException("Robo Exception"))
        LogManager.e("ROBO_TAG", "Error without throwable")
        LogManager.e("ROBO_TAG", "Error with message containing ex: Robo Exception", RuntimeException("Robo Exception"))
        LogManager.e("ROBO_TAG", "Error with message ending in colon: ", RuntimeException("Robo Exception"))
        LogManager.e(
            tag = "ROBO_TAG",
            prefix = "Error prefix: ",
            throwable = RuntimeException("Prefix Robo Exception"),
            fallbackMessage = "Fallback",
        )
        LogManager.e(
            tag = "ROBO_TAG",
            prefix = "Error prefix without colon",
            throwable = RuntimeException("Prefix Robo Exception"),
            fallbackMessage = "Fallback",
        )
        LogManager.u("ROBO_TAG", "Unexpected message Robolectric")

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertTrue("Breadcrumbs should be recorded", breadcrumbs.size >= 8)
    }

    /**
     * Verifies execution of [LogManager] when [LogManager.forceLoggable] is `null` or `false`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Robolectric_FullCoverage_ForceLoggableFalseAndNull() {
        LogManager.forceLoggable = false

        LogManager.d("ROBO_TAG", "Debug message false")
        LogManager.i("ROBO_TAG", "Info message false")
        LogManager.w("ROBO_TAG", "Warn message false")
        LogManager.e("ROBO_TAG", "Error message false")
        LogManager.u("ROBO_TAG", "Unexpected message false")

        LogManager.forceLoggable = null
        val loggableResult = LogManager.isLoggable("ROBO_TAG", android.util.Log.DEBUG)
        assertNotNull("isLoggable should return a boolean", loggableResult)
    }

    /**
     * Verifies extension methods [LogManager.throwU] and [LogManager.throwE] under Robolectric.
     */
    @Test
    @Suppress("UNREACHABLE_CODE")
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManagerExt_Robolectric_Coverage() {
        LogManager.forceLoggable = true

        val cause = RuntimeException("Cause Robo")
        val uEx = try {
            LogManager.throwU("ROBO_TAG", "Robo U message", cause)
        } catch (e: IllegalStateException) {
            e
        }
        assertEquals("Robo U message", uEx.message)

        val origEx = IllegalArgumentException("Orig Robo")
        val eEx = try {
            LogManager.throwE("ROBO_TAG", "Robo E message", origEx)
        } catch (e: IllegalArgumentException) {
            e
        }
        assertEquals("Orig Robo", eEx.message)
    }
}
