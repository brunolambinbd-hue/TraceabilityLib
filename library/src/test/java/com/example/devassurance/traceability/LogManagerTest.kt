package com.example.devassurance.traceability

import com.parabd.lib.traceability.CoveredRequirement
import com.parabd.lib.traceability.utils.BreadcrumbManager
import com.parabd.lib.traceability.utils.LogManager
import com.parabd.lib.traceability.utils.TraceConstants
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [LogManager] verifying unconditional breadcrumb emission and conditional logcat calls.
 */
class LogManagerTest {

    private fun assertBreadcrumbEmitted(expectedTag: String, substring: String) {
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(expectedTag) && it.contains(substring) }
        assertTrue(
            "Breadcrumb [$expectedTag] contenant '$substring' manquant. Derniers logs:\n${breadcrumbs.takeLast(10).joinToString("\n")}",
            found
        )
    }

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

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Debug_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.d("TEST_TAG", "Debug message test")

        // 1. Le breadcrumb doit être émis MÊME LORSQUE isLoggable vaut false !
        assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Debug message test")
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Info_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.i("TEST_TAG", "Info message test")

        assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Info message test")
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Warn_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.w("TEST_TAG", "Warning message test")

        assertBreadcrumbEmitted(TraceConstants.BRANCH_ALT, "Warning message test")
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Error_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        val exception = RuntimeException("Test Error")
        LogManager.e("TEST_TAG", "Error message test", exception)

        assertBreadcrumbEmitted(TraceConstants.RECOVERY, "Error message test (Test Error)")
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Unexpected_EmitsBreadcrumbUnconditionally() {
        LogManager.forceLoggable = false
        LogManager.u("TEST_TAG", "Unexpected anomaly test")

        assertBreadcrumbEmitted(TraceConstants.UNEXPECTED, "Unexpected anomaly test")
    }

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
