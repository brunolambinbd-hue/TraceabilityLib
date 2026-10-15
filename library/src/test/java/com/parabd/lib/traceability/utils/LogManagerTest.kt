package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
        assertTrue(LogManager.isLoggable("TAG", android.util.Log.DEBUG))

        LogManager.forceLoggable = false
        assertFalse(LogManager.isLoggable("TAG", android.util.Log.DEBUG))
    }
}
