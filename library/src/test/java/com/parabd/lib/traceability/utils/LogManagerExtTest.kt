package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [LogManager] extension functions defined in LogManagerExt.kt.
 */
class LogManagerExtTest {

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
     * Verifies that [LogManager.throwU] logs an UNEXPECTED breadcrumb and throws [IllegalStateException].
     */
    @Test
    @Suppress("UNREACHABLE_CODE")
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testThrowU_EmitsBreadcrumbAndThrowsIllegalStateException() {
        val causeException = RuntimeException("Root cause")
        val exception = try {
            LogManager.throwU("TEST_TAG", "Unexpected anomaly", causeException)
        } catch (e: IllegalStateException) {
            e
        }

        assertEquals("Unexpected anomaly", exception.message)
        assertSame(causeException, exception.cause)

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(TraceConstants.UNEXPECTED) && it.contains("Unexpected anomaly") }
        assertTrue("Expected UNEXPECTED breadcrumb for 'Unexpected anomaly'", found)
    }

    /**
     * Verifies that [LogManager.throwE] logs a RECOVERY breadcrumb and rethrows the provided exception instance.
     */
    @Test
    @Suppress("UNREACHABLE_CODE")
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testThrowE_EmitsBreadcrumbAndRethrowsException() {
        val originalException = IllegalArgumentException("Invalid argument")
        val exception = try {
            LogManager.throwE("TEST_TAG", "Failed to process", originalException)
        } catch (e: IllegalArgumentException) {
            e
        }

        assertSame(originalException, exception)

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(TraceConstants.RECOVERY) && it.contains("Failed to process") }
        assertTrue("Expected RECOVERY breadcrumb for 'Failed to process'", found)
    }

    /**
     * Verifies that [LogManager.e] with prefix formats raw exception message properly.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testE_WithPrefix_FormatsMessageWithExceptionDetail() {
        val exception = RuntimeException("Network timeout")
        LogManager.e("TEST_TAG", prefix = "Fetch failed", throwable = exception)

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(TraceConstants.RECOVERY) && it.contains("Fetch failed: Network timeout") }
        assertTrue("Expected RECOVERY breadcrumb formatted with prefix and detail", found)
    }

    /**
     * Verifies that [LogManager.e] with prefix already ending with space or colon does not duplicate delimiter.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testE_WithPrefixEndingWithSpace_FormatsWithoutDuplicateColon() {
        val exception = RuntimeException("Database error")
        LogManager.e("TEST_TAG", prefix = "Save failed: ", throwable = exception)

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(TraceConstants.RECOVERY) && it.contains("Save failed: Database error") }
        assertTrue("Expected RECOVERY breadcrumb without duplicated colon", found)
    }

    /**
     * Verifies that [LogManager.e] uses the provided fallback message when exception message is blank or null.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testE_WithBlankExceptionMessage_UsesFallbackMessage() {
        val blankException = RuntimeException("   ")
        LogManager.e("TEST_TAG", prefix = "Sync error", throwable = blankException, fallbackMessage = "Custom fallback")

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(TraceConstants.RECOVERY) && it.contains("Sync error: Custom fallback") }
        assertTrue("Expected RECOVERY breadcrumb using fallback message", found)
    }
}
