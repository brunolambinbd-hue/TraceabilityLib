package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.SatisfiesRequirement

/**
 * Standard technical markers used for traceability, breadcrumb logging, and flight recording.
 */
@SatisfiesRequirement("6.3.2.2")
object TraceConstants {
    /** Marker for nominal flow events and expected state transitions. */
    const val EXPECTED: String = "[EXPECTED]"

    /** Marker for alternative execution branches, secondary logic paths, or fallback mechanisms. */
    const val BRANCH_ALT: String = "[BRANCH_ALT]"

    /** Marker for unexpected states, structural anomalies, or logical errors detected before a crash. */
    const val UNEXPECTED: String = "[UNEXPECTED]"

    /** Marker for error recovery processes, catch blocks, and exception handling events. */
    const val RECOVERY: String = "[RECOVERY]"
}
