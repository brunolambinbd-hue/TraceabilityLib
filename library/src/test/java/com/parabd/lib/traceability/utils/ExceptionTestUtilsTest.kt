package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [assertExceptionEmitted], [LogManager.throwU], and [LogManager.throwE] in [com.parabd.lib.traceability.utils].
 */
class ExceptionTestUtilsTest {

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
    fun testAssertExceptionEmitted_Success() {
        val ex = assertExceptionEmitted<IllegalStateException>("Critical UI Failure") {
            LogManager.throwU("TEST_TAG", "Critical UI Failure")
        }

        assertEquals("Critical UI Failure", ex.message)
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testAssertExceptionEmitted_RecoverySuccess() {
        val ex = assertExceptionEmitted<RuntimeException>(
            expectedSubstring = "I/O Error",
            expectedMarker = TraceConstants.RECOVERY
        ) {
            LogManager.throwE("TEST_TAG", "I/O Error", RuntimeException("I/O Error"))
        }

        assertEquals("I/O Error", ex.message)
    }

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
