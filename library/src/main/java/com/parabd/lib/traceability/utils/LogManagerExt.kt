package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.SatisfiesRequirement

/**
 * Extension functions for [LogManager] providing helper utilities for exception logging,
 * automatic exception rethrowing, and formatted error messages with fallbacks.
 */

/**
 * Formats a combined error message from an operational prefix and throwable detail with fallback support.
 *
 * @param prefix The log prefix describing the operation (e.g. "Audit failed: ").
 * @param throwable The exception instance.
 * @param fallbackMessage Fallback message if [throwable]'s message is null or blank.
 * @return The formatted error string with prefix and detail.
 */
internal fun formatErrorMessage(
    prefix: String,
    throwable: Throwable,
    fallbackMessage: String,
): String {
    val messageText = throwable.message
    val detail = if (messageText.isNullOrBlank()) {
        fallbackMessage
    } else {
        messageText
    }

    val cleanPrefix = prefix.trimEnd()
    return if (cleanPrefix.endsWith(":")) {
        "$cleanPrefix $detail"
    } else {
        "$cleanPrefix: $detail"
    }
}

/**
 * Emits an UNEXPECTED structural anomaly log via [LogManager.u] and immediately throws an [IllegalStateException].
 *
 * Useful for defensive programming where a structural anomaly must halt execution.
 *
 * @param tag The tag identifying the log source.
 * @param message The error message describing the structural anomaly.
 * @param cause Optional underlying cause of the exception.
 * @return Nothing, as this function always throws an exception.
 * @throws IllegalStateException Always thrown with [message] and [cause].
 */
@SatisfiesRequirement("6.1.1.9")
@SatisfiesRequirement("6.3.2.9")
fun LogManager.throwU(tag: String, message: String, cause: Throwable? = null): Nothing {
    u(tag, message)
    throw IllegalStateException(message, cause)
}

/**
 * Emits an ERROR level log via [LogManager.e] and rethrows the provided exception.
 *
 * Useful for logging an error right before propagating the exception up the call stack.
 *
 * @param T The type of throwable to throw.
 * @param tag The tag identifying the log source.
 * @param message The error message to record.
 * @param exception The exception instance to log and throw.
 * @return Nothing, as this function always rethrows [exception].
 * @throws Throwable Always rethrows [exception].
 */
@SatisfiesRequirement("6.1.1.9")
@SatisfiesRequirement("6.3.2.9")
fun <T : Throwable> LogManager.throwE(tag: String, message: String, exception: T): Nothing {
    e(tag, message, exception)
    throw exception
}

/**
 * Emits an ERROR level log for an exception with an explicit prefix, automatically extracting
 * and formatting the throwable's message with [fallbackMessage] if null or blank.
 *
 * Delegates to [LogManager.e] to ensure unified dual-channel logging (Flight Recorder + Logcat).
 *
 * @param tag The tag identifying the log source.
 * @param prefix The log prefix describing the operation (e.g., "Audit failed: ").
 * @param throwable The exception instance.
 * @param fallbackMessage Fallback message if [throwable]'s message is null or blank. Defaults to "Operation failed".
 */
@SatisfiesRequirement("6.1.1.9")
@SatisfiesRequirement("6.3.2.9")
fun LogManager.e(
    tag: String,
    prefix: String,
    throwable: Throwable,
    fallbackMessage: String = "Operation failed",
) {
    val fullMsg = formatErrorMessage(prefix, throwable, fallbackMessage)
    e(tag, fullMsg, throwable)
}
