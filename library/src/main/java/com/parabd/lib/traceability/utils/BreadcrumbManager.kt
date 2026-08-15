package com.parabd.lib.traceability.utils

import java.util.Collections
import java.util.LinkedList

/**
 * Singleton responsible for storing the last N breadcrumbs (qualified logs) in memory.
 * These logs are flushed to the crash log file when an uncaught exception occurs.
 */
object BreadcrumbManager {
    private const val MAX_BREADCRUMBS = 20
    private val buffer = Collections.synchronizedList(LinkedList<String>())

    /**
     * Adds a qualified log to the circular buffer.
     * @param prefix The nomenclature prefix (e.g., [EXPECTED]).
     * @param message The log message.
     */
    fun addBreadcrumb(prefix: String, message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val entry = "$time $prefix $message"
        synchronized(buffer) {
            if (buffer.size >= MAX_BREADCRUMBS) {
                buffer.removeAt(0)
            }
            buffer.add(entry)
        }
    }

    /**
     * Retrieves all current breadcrumbs.
     * @return A list of formatted log entries.
     */
    fun getBreadcrumbs(): List<String> {
        return synchronized(buffer) {
            buffer.toList()
        }
    }

    /**
     * Clears all breadcrumbs. Useful for testing.
     */
    fun clear() {
        buffer.clear()
    }
}

