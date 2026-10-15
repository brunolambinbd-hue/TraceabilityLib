package com.parabd.lib.traceability.utils

import android.util.Log
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mockStatic

/**
 * Unit test suite for [LogManager].
 *
 * Verifies dual-channel logging behavior, ensuring unconditional flight recorder breadcrumb emission
 * to [BreadcrumbManager] and conditional Logcat output filtering via [LogManager.forceLoggable].
 */
class LogManagerTest {

    /**
     * Asserts that a breadcrumb containing [expectedTag] and [substring] was emitted.
     *
     * @param expectedTag The expected TDO marker tag (e.g. [TraceConstants.EXPECTED]).
     * @param substring The expected substring within the breadcrumb message.
     */
    private fun assertBreadcrumbEmitted(expectedTag: String, substring: String) {
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(expectedTag) && it.contains(substring) }
        assertTrue("Breadcrumb [$expectedTag] containing '$substring' missing in Flight Log", found)
    }

    /**
     * Resets breadcrumb buffer and loggable override flag before each test.
     */
    @Before
    fun setUp() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    /**
     * Cleans up breadcrumb buffer and loggable override flag after each test.
     */
    @After
    fun tearDown() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    /**
     * Direct unit tests for helper function [safeLogcat].
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testSafeLogcat_DirectHelper() {
        var executed = false
        safeLogcat {
            executed = true
        }
        assertTrue("safeLogcat should execute the action lambda", executed)

        var catchExecuted = false
        safeLogcat {
            catchExecuted = true
            throw RuntimeException("Simulated Logcat failure")
        }
        assertTrue("safeLogcat should swallow exception without crashing", catchExecuted)
    }

    /**
     * Direct unit tests for helper function [formatThrowableMessage].
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testFormatThrowableMessage_DirectHelper() {
        val exValid = RuntimeException("Server crash")
        val exBlank = RuntimeException("   ")
        val exNull = RuntimeException()

        assertEquals("Load failed: Server crash", formatThrowableMessage("Load failed:", exValid))
        assertEquals("Load failed: Server crash", formatThrowableMessage("Load failed: ", exValid))
        assertEquals("Load failed (Server crash)", formatThrowableMessage("Load failed", exValid))
        assertEquals("Load failed", formatThrowableMessage("Load failed", exBlank))
        assertEquals("Load failed", formatThrowableMessage("Load failed", exNull))
        assertEquals("Load failed with Server crash", formatThrowableMessage("Load failed with Server crash", exValid))
    }

    /**
     * Verifies that [LogManager.d] emits a breadcrumb unconditionally even when [LogManager.forceLoggable] is `false`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Debug_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.d("TEST_TAG", "Debug message test")

        assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Debug message test")
    }

    /**
     * Verifies that [LogManager.i] emits a breadcrumb unconditionally even when [LogManager.forceLoggable] is `false`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Info_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.i("TEST_TAG", "Info message test")

        assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Info message test")
    }

    /**
     * Verifies that [LogManager.w] emits a breadcrumb with [TraceConstants.BRANCH_ALT] unconditionally.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Warn_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.w("TEST_TAG", "Warning message test")

        assertBreadcrumbEmitted(TraceConstants.BRANCH_ALT, "Warning message test")
    }

    /**
     * Verifies that [LogManager.e] emits a breadcrumb with [TraceConstants.RECOVERY] and formatted exception message.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Error_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        val exception = RuntimeException("Test Error")
        LogManager.e("TEST_TAG", "Error message test", exception)

        assertBreadcrumbEmitted(TraceConstants.RECOVERY, "Error message test (Test Error)")
    }

    /**
     * Verifies that [LogManager.u] emits a breadcrumb with [TraceConstants.UNEXPECTED] unconditionally.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Unexpected_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.u("TEST_TAG", "Unexpected anomaly test")

        assertBreadcrumbEmitted(TraceConstants.UNEXPECTED, "Unexpected anomaly test")
    }

    /**
     * Verifies that [LogManager.forceLoggable] overrides loggability status.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_LoggableFlag_OverridesStatus() {
        LogManager.forceLoggable = true
        assertTrue(LogManager.isLoggable("TAG", Log.DEBUG))

        LogManager.forceLoggable = false
        assertFalse(LogManager.isLoggable("TAG", Log.DEBUG))
    }

    /**
     * Verifies that [LogManager.d], [LogManager.i], [LogManager.w], [LogManager.e], [LogManager.u] execute
     * Logcat output blocks safely when [LogManager.forceLoggable] is `true`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_WhenForceLoggableTrue_ExecutesLogcatSafely() {
        LogManager.forceLoggable = true

        LogManager.d("TEST_TAG", "Debug enabled test")
        LogManager.i("TEST_TAG", "Info enabled test")
        LogManager.w("TEST_TAG", "Warn enabled test")
        LogManager.e("TEST_TAG", "Error enabled test", RuntimeException("Exception detail"))
        LogManager.e("TEST_TAG", "Error without throwable")
        LogManager.e("TEST_TAG", "Error containing detail: Exception detail", RuntimeException("Exception detail"))
        LogManager.e("TEST_TAG", "Error ending with colon: ", RuntimeException("Exception detail"))
        LogManager.e(
            tag = "TEST_TAG",
            prefix = "Error overload: ",
            throwable = RuntimeException("Overload exception"),
            fallbackMessage = "Fallback",
        )
        LogManager.u("TEST_TAG", "Unexpected enabled test")

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertTrue("Breadcrumbs should be recorded when forceLoggable is true", breadcrumbs.isNotEmpty())
    }

    /**
     * Verifies that all defensive `catch (_: Throwable)` blocks in [LogManager] handle native
     * Logcat exceptions gracefully without interrupting breadcrumb emission.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_CatchBlocks_WhenLogcatThrows() {
        mockStatic(Log::class.java).use { mockLog ->
            mockLog.`when`<Boolean> { Log.isLoggable(anyString(), anyInt()) }
                .thenThrow(RuntimeException("Logcat isLoggable failure"))
            mockLog.`when`<Int> { Log.d(anyString(), anyString()) }
                .thenThrow(RuntimeException("Logcat d failure"))
            mockLog.`when`<Int> { Log.i(anyString(), anyString()) }
                .thenThrow(RuntimeException("Logcat i failure"))
            mockLog.`when`<Int> { Log.w(anyString(), anyString()) }
                .thenThrow(RuntimeException("Logcat w failure"))
            mockLog.`when`<Int> { Log.e(anyString(), anyString()) }
                .thenThrow(RuntimeException("Logcat e failure"))
            mockLog.`when`<Int> { Log.e(anyString(), anyString(), any(Throwable::class.java)) }
                .thenThrow(RuntimeException("Logcat e with throwable failure"))

            // 1. Triggers catch in isLoggable AND catch in d, i, w, e, u when forceLoggable is null
            LogManager.forceLoggable = null

            LogManager.d("TAG", "Debug catch test")
            LogManager.i("TAG", "Info catch test")
            LogManager.w("TAG", "Warn catch test")
            LogManager.e("TAG", "Error without throwable catch test")
            LogManager.e("TAG", "Error with throwable catch test", RuntimeException("Sub error"))
            LogManager.e("TAG", "Error prefix catch test", RuntimeException("Sub error"), "Fallback")
            LogManager.u("TAG", "Unexpected catch test")

            // 2. Triggers catches when forceLoggable = true as well
            LogManager.forceLoggable = true

            LogManager.d("TAG", "Debug forced catch test")
            LogManager.i("TAG", "Info forced catch test")
            LogManager.w("TAG", "Warn forced catch test")
            LogManager.e("TAG", "Error forced catch test")
            LogManager.u("TAG", "Unexpected forced catch test")

            val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
            assertTrue("Breadcrumbs should still be recorded even when Logcat throws", breadcrumbs.size >= 10)
        }
    }
}
