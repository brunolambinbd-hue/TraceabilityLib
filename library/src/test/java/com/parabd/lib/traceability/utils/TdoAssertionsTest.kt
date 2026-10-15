package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite for [TdoAssertions].
 *
 * Verifies assertion helper methods for breadcrumb emission and sequential execution path checking.
 */
class TdoAssertionsTest {

    /**
     * Resets breadcrumb buffer before each test.
     */
    @Before
    fun setUp() {
        BreadcrumbManager.clear()
    }

    /**
     * Cleans up breadcrumb buffer after each test.
     */
    @After
    fun tearDown() {
        BreadcrumbManager.clear()
    }

    /**
     * Verifies that [TdoAssertions.assertBreadcrumbEmitted] passes when matching breadcrumb exists.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbEmitted_Success() {
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Test event")
        TdoAssertions.assertBreadcrumbEmitted(TraceConstants.EXPECTED, "Test event")
    }

    /**
     * Verifies that [TdoAssertions.assertBreadcrumbEmitted] throws [IllegalStateException] when breadcrumb is missing.
     */
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

    /**
     * Verifies that [TdoAssertions.assertBreadcrumbSequence] passes when breadcrumbs match required sequence order.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbSequence_Success() {
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Step 1")
        BreadcrumbManager.addBreadcrumb(TraceConstants.BRANCH_ALT, "Step 2")

        TdoAssertions.assertBreadcrumbSequence(
            listOf(
                TraceConstants.EXPECTED to "Step 1",
                TraceConstants.BRANCH_ALT to "Step 2",
            ),
        )
    }

    /**
     * Verifies that [TdoAssertions.assertBreadcrumbSequence] throws [IllegalStateException] when sequence order is broken.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbSequence_OutOfOrder_ThrowsException() {
        BreadcrumbManager.addBreadcrumb(TraceConstants.BRANCH_ALT, "Step 2")
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Step 1")

        try {
            TdoAssertions.assertBreadcrumbSequence(
                listOf(
                    TraceConstants.EXPECTED to "Step 1",
                    TraceConstants.BRANCH_ALT to "Step 2",
                ),
            )
            fail("Expected IllegalStateException due to out-of-order sequence")
        } catch (_: IllegalStateException) {
            // Expected
        }
    }

    /**
     * Verifies that [TdoAssertions.assertBreadcrumbSequence] in strict mode throws [IllegalStateException] when extra breadcrumbs exist.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertBreadcrumbSequence_StrictMismatch_ThrowsException() {
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Step 1")
        BreadcrumbManager.addBreadcrumb(TraceConstants.BRANCH_ALT, "Step 2")
        BreadcrumbManager.addBreadcrumb(TraceConstants.EXPECTED, "Extra Step")

        try {
            TdoAssertions.assertBreadcrumbSequence(
                expectedSequence = listOf(
                    TraceConstants.EXPECTED to "Step 1",
                    TraceConstants.BRANCH_ALT to "Step 2",
                ),
                strict = true,
            )
            fail("Expected IllegalStateException due to strict size mismatch")
        } catch (_: IllegalStateException) {
            // Expected
        }
    }
}
