package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

/**
 * Unified Exception Observability Assertion for Unit & Integration Tests.
 *
 * Placed in [com.parabd.lib.traceability.utils] alongside [LogManager] and [BreadcrumbManager] to serve
 * as the unified Exception Observability Assertion API for the Traceability Library.
 *
 * Verifies 3 things in a single assertion:
 * 1. The expected exception class [T] is thrown by [block].
 * 2. The exception message contains [expectedSubstring].
 * 3. [BreadcrumbManager] recorded the corresponding observability trace.
 *
 * @param T The expected exception type.
 * @param expectedSubstring The expected message substring or constant.
 * @param expectedMarker The TDO marker (defaults to [TraceConstants.UNEXPECTED]).
 * @param expectedLogSubstring Optional log substring if different from exception message.
 * @param block The code block expected to throw.
 * @return The captured exception instance for further assertions if needed.
 */
@CoveredRequirement("6.1.1.9.10")
@CoveredRequirement("6.3.2.9.10")
inline fun <reified T : Throwable> assertExceptionEmitted(
    expectedSubstring: String,
    expectedMarker: String = TraceConstants.UNEXPECTED,
    expectedLogSubstring: String? = null,
    noinline block: () -> Unit
): T {
    // 1. Check Exception Type
    val exception: T = try {
        block()
        fail("Expected exception of type ${T::class.java.name} to be thrown, but no exception was thrown.")
        error("Unreachable")
    } catch (e: Throwable) {
        (e as? T) ?: throw AssertionError("Expected exception of type ${T::class.java.name}, but caught ${e::class.java.name}", e)
    }

    // 2. Check Exception Message (0 Magic String)
    val fullMessage = exception.message.orEmpty() + " " + exception.cause?.message.orEmpty()
    assertTrue(
        "Expected exception message containing '$expectedSubstring', but got '$fullMessage'",
        fullMessage.contains(expectedSubstring)
    )

    // 3. Check Observability Flight Log Trace
    val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
    val logSub = expectedLogSubstring ?: expectedSubstring
    val hasTrace = breadcrumbs.any {
        it.contains(expectedMarker) && it.contains(logSub)
    }
    assertTrue(
        "Expected Flight Log trace containing '$expectedMarker' and '$logSub', but recent logs were:\n${breadcrumbs.takeLast(10).joinToString("\n")}",
        hasTrace
    )

    return exception
}
