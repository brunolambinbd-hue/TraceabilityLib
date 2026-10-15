package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite for [assertExceptionEmitted].
 *
 * Verifies combined assertion of exception class type, exception message content, and flight recorder trace emission.
 */
class ExceptionTestUtilsTest {

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
     * Verifies that [assertExceptionEmitted] passes when exception type, message, and breadcrumb trace match.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertExceptionEmitted_Success() {
        val ex = assertExceptionEmitted<IllegalStateException>("Critical UI Failure") {
            LogManager.throwU("TEST_TAG", "Critical UI Failure")
        }

        assertEquals("Critical UI Failure", ex.message)
    }

    /**
     * Verifies that [assertExceptionEmitted] works with custom markers such as [TraceConstants.RECOVERY].
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertExceptionEmitted_RecoverySuccess() {
        val ex = assertExceptionEmitted<RuntimeException>(
            expectedSubstring = "I/O Error",
            expectedMarker = TraceConstants.RECOVERY,
        ) {
            LogManager.throwE("TEST_TAG", "I/O Error", RuntimeException("I/O Error"))
        }

        assertEquals("I/O Error", ex.message)
    }

    /**
     * Verifies that [assertExceptionEmitted] fails when thrown exception is of wrong type.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertExceptionEmitted_WrongExceptionType_Fails() {
        try {
            assertExceptionEmitted<IllegalStateException>("Error") {
                throw IllegalArgumentException("Error")
            }
            fail("Should fail on wrong exception type")
        } catch (_: AssertionError) {
            // Expected
        }
    }

    /**
     * Verifies that [assertExceptionEmitted] fails when exception message does not contain expected substring.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertExceptionEmitted_WrongMessage_Fails() {
        try {
            assertExceptionEmitted<IllegalStateException>("Expected Message") {
                LogManager.throwU("TEST_TAG", "Different Message")
            }
            fail("Should fail on wrong message")
        } catch (_: AssertionError) {
            // Expected
        }
    }

    /**
     * Verifies that [assertExceptionEmitted] fails when exception is thrown without emitting flight recorder trace.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertExceptionEmitted_MissingBreadcrumbTrace_Fails() {
        try {
            assertExceptionEmitted<IllegalStateException>("Untraced Message") {
                throw IllegalStateException("Untraced Message")
            }
            fail("Should fail on missing breadcrumb trace")
        } catch (_: AssertionError) {
            // Expected
        }
    }
}
