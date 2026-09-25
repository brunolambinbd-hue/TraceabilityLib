package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.SatisfiesRequirement
import java.util.Collections
import java.util.LinkedList

/**
 * Singleton responsible for storing the last N breadcrumbs (qualified logs) in memory.
 *
 * Serves as an in-memory circular buffer (flight recorder) that retains recent operational
 * events with timestamp prefixes. These breadcrumbs can be flushed or attached to diagnostic
 * crash logs when an uncaught exception or structural anomaly occurs.
 */
@SatisfiesRequirement("6.1.2.9")
object BreadcrumbManager {

    /**
     * Maximum number of breadcrumbs stored in memory before older entries are evicted.
     */
    @SatisfiesRequirement("6.1.2.9")
    private const val MAX_BREADCRUMBS = 20

    /**
     * Thread-safe circular buffer holding formatted log entries.
     */
    @SatisfiesRequirement("6.1.2.9")
    private val buffer = Collections.synchronizedList(LinkedList<String>())

    /**
     * Adds a qualified log entry to the circular buffer.
     *
     * Automatically prepends a timestamp formatted as "HH:mm:ss.SSS" to the entry.
     * If the buffer exceeds [MAX_BREADCRUMBS], the oldest entry is removed.
     *
     * @param prefix The nomenclature prefix tag.
     * @param message The log message describing the event.
     */
    @SatisfiesRequirement("6.1.2.9")
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
     * Retrieves a thread-safe snapshot copy of all current breadcrumbs.
     *
     * @return A list of formatted log entries currently stored in the buffer.
     */
    @SatisfiesRequirement("6.1.2.9")
    fun getBreadcrumbs(): List<String> {
        return synchronized(buffer) {
            buffer.toList()
        }
    }

    /**
     * Clears all breadcrumbs from memory. Primary usage is for resetting state between tests.
     */
    @SatisfiesRequirement("6.1.2.9")
    fun clear() {
        buffer.clear()
    }
}
