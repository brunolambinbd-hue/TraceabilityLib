package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.SatisfiesRequirement

/**
 * Test-Driven Observability (TDO) assertion helpers.
 *
 * Provides specialized assertion methods for verifying breadcrumb emission and sequence ordering
 * during unit or integration testing.
 */
@SatisfiesRequirement("6.1.1.9")
@SatisfiesRequirement("6.3.2.9")
object TdoAssertions {

    /**
     * Asserts that a breadcrumb containing the specified prefix tag and message substring has been emitted to [BreadcrumbManager].
     *
     * @param expectedTag The expected TDO marker tag (e.g., [TraceConstants.EXPECTED]).
     * @param substring The expected substring within the breadcrumb message.
     * @throws IllegalStateException If no matching breadcrumb is found in [BreadcrumbManager].
     */
    fun assertBreadcrumbEmitted(expectedTag: String, substring: String) {
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        val found = breadcrumbs.any { it.contains(expectedTag) && it.contains(substring) }
        check(found) {
            "Breadcrumb [$expectedTag] containing '$substring' MISSING in Flight Log.\n" +
            "Recent Breadcrumbs (${breadcrumbs.size}):\n${breadcrumbs.takeLast(10).joinToString("\n")}"
        }
    }

    /**
     * Asserts that a sequence of breadcrumb entries was emitted in the specified order.
     *
     * In non-strict mode (default), intermediate breadcrumbs between sequence items are allowed.
     * In strict mode, the total number of recorded breadcrumbs must match [expectedSequence] exactly.
     *
     * @param expectedSequence List of [Pair]s containing expected tag and message substring for each step in order.
     * @param strict If `true`, requires exact size matching with no extra intermediate breadcrumbs. Defaults to `false`.
     * @throws IllegalStateException If a step in the sequence is missing, out of order, or if strict size validation fails.
     */
    fun assertBreadcrumbSequence(
        expectedSequence: List<Pair<String, String>>,
        strict: Boolean = false,
    ) {
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        var lastFoundIndex = -1

        expectedSequence.forEachIndexed { step, (expectedTag, expectedSub) ->
            val foundIndex = breadcrumbs.indexOfFirst { item ->
                (breadcrumbs.indexOf(item) > lastFoundIndex) &&
                item.contains(expectedTag) &&
                item.contains(expectedSub)
            }

            check(foundIndex > lastFoundIndex) {
                "Execution Path Sequence Break at Step #$step: Expected [$expectedTag | '$expectedSub'] after index $lastFoundIndex.\n" +
                "Flight Log Breadcrumbs (${breadcrumbs.size} total):\n${breadcrumbs.joinToString("\n")}"
            }

            lastFoundIndex = foundIndex
        }

        if (strict) {
            check(breadcrumbs.size == expectedSequence.size) {
                "Strict Sequence Mismatch: Expected ${expectedSequence.size} total breadcrumbs, but found ${breadcrumbs.size}.\n" +
                "Flight Log:\n${breadcrumbs.joinToString("\n")}"
            }
        }
    }
}
