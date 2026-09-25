package com.parabd.lib.traceability.utils

import android.util.Log
import com.parabd.lib.traceability.SatisfiesRequirement

/**
 * Centralized logging and observability manager.
 *
 * Placed in [com.parabd.lib.traceability.utils] alongside [BreadcrumbManager] to serve as the unified
 * Dual-Channel Observability API (Logcat + Flight Recorder).
 *
 * 1. Breadcrumbs are ALWAYS emitted unconditionally for TDO Flight Log tracking and post-mortem diagnostics.
 * 2. Logcat output is safely filtered based on [Log.isLoggable] or debug mode to avoid noise in release builds.
 */
@SatisfiesRequirement("6.1.1.9")
@SatisfiesRequirement("6.3.2.9")
object LogManager {

    /**
     * Override flag allowing JVM unit tests to bypass native Android [Log.isLoggable] checks
     * or redirect outputs without requiring Robolectric.
     *
     * Set to `true` or `false` to override loggability, or `null` for normal behavior.
     */
    @Volatile
    var forceLoggable: Boolean? = null

    /**
     * Checks whether a log message at the given level and tag should be sent to Android Logcat.
     *
     * @param tag The tag identifying the log source.
     * @param level The Android log priority level (e.g., [Log.DEBUG], [Log.INFO]).
     * @return `true` if output to logcat is enabled or forced; `false` otherwise.
     */
    internal fun isLoggable(tag: String, level: Int): Boolean {
        val forced = forceLoggable
        return forced ?: try {
            Log.isLoggable(tag, level)
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * Emits a DEBUG level log.
     *
     * Unconditionally adds a breadcrumb to [BreadcrumbManager] and outputs to Logcat if loggable.
     *
     * @param tag The tag identifying the log source.
     * @param message The log message to record.
     * @param marker The TDO marker prefix (defaults to [TraceConstants.EXPECTED]).
     */
    fun d(tag: String, message: String, marker: String = TraceConstants.EXPECTED) {
        BreadcrumbManager.addBreadcrumb(marker, message)
        if (isLoggable(tag, Log.DEBUG)) {
            try {
                Log.d(tag, "$marker $message")
            } catch (_: Throwable) {}
        }
    }

    /**
     * Emits an INFO level log.
     *
     * Unconditionally adds a breadcrumb to [BreadcrumbManager] and outputs to Logcat if loggable.
     *
     * @param tag The tag identifying the log source.
     * @param message The log message to record.
     * @param marker The TDO marker prefix (defaults to [TraceConstants.EXPECTED]).
     */
    fun i(tag: String, message: String, marker: String = TraceConstants.EXPECTED) {
        BreadcrumbManager.addBreadcrumb(marker, message)
        if (isLoggable(tag, Log.INFO)) {
            try {
                Log.i(tag, "$marker $message")
            } catch (_: Throwable) {}
        }
    }

    /**
     * Emits a WARN level log.
     *
     * Unconditionally adds a breadcrumb to [BreadcrumbManager] and outputs to Logcat if loggable.
     *
     * @param tag The tag identifying the log source.
     * @param message The log message to record.
     * @param marker The TDO marker prefix (defaults to [TraceConstants.BRANCH_ALT]).
     */
    fun w(tag: String, message: String, marker: String = TraceConstants.BRANCH_ALT) {
        BreadcrumbManager.addBreadcrumb(marker, message)
        if (isLoggable(tag, Log.WARN)) {
            try {
                Log.w(tag, "$marker $message")
            } catch (_: Throwable) {}
        }
    }

    /**
     * Emits an ERROR level log with an optional exception.
     *
     * Unconditionally adds a breadcrumb to [BreadcrumbManager] and outputs to Logcat if loggable.
     *
     * @param tag The tag identifying the log source.
     * @param message The error message to record.
     * @param throwable Optional exception associated with the error.
     * @param marker The TDO marker prefix (defaults to [TraceConstants.RECOVERY]).
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        marker: String = TraceConstants.RECOVERY,
    ) {
        val fullMsg = if (throwable != null) "$message (${throwable.message})" else message
        BreadcrumbManager.addBreadcrumb(marker, fullMsg)

        if (isLoggable(tag, Log.ERROR)) {
            try {
                if (throwable != null) {
                    Log.e(tag, "$marker $message", throwable)
                } else {
                    Log.e(tag, "$marker $message")
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Emits an UNEXPECTED structural anomaly log.
     *
     * Unconditionally adds a breadcrumb with [TraceConstants.UNEXPECTED] to [BreadcrumbManager]
     * and outputs to Logcat if loggable.
     *
     * @param tag The tag identifying the log source.
     * @param message The log message describing the structural anomaly.
     */
    fun u(tag: String, message: String) {
        BreadcrumbManager.addBreadcrumb(TraceConstants.UNEXPECTED, message)
        if (isLoggable(tag, Log.ERROR)) {
            try {
                Log.e(tag, "${TraceConstants.UNEXPECTED} $message")
            } catch (_: Throwable) {}
        }
    }
}
