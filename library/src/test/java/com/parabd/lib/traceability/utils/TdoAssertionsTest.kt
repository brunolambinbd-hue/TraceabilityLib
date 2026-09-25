package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [TdoAssertions] in [com.parabd.lib.traceability.utils].
 */
class TdoAssertionsTest {

    @Before
    fun setUp() {
        BreadcrumbManager.clear()
    }

    @After
    fun tearDown() {
        BreadcrumbManager.clear()
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbEmitted_Success() {
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Test event")
        TdoAssertions.assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Test event")
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbEmitted_Missing_ThrowsException() {
        try {
            TdoAssertions.assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Missing event")
            fail("Expected IllegalStateException to be thrown")
        } catch (_: IllegalStateException) {
            // Expected exception
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbSequence_Success() {
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Step 1")
        BreadcrumbManager.addBreadcrumb(TraceConstants.BRANCH_ALT, "Step 2")

        TdoAssertions.assertBreadcrumbSequence(
            listOf(
                TraceConstants.EXPECTED to "Step 1",
                TraceConstants.BRANCH_ALT to "Step 2"
            )
        )
    }
}
