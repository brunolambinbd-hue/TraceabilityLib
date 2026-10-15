package com.parabd.lib.traceability.utils

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite for [BreadcrumbManager].
 *
 * Verifies the in-memory flight recorder circular buffer functionality,
 * including storage formatting, capacity rotation (max 20 entries), and state clearing.
 */
class BreadcrumbManagerTest {

    /**
     * Resets the in-memory breadcrumb buffer before each test method execution.
     */
    @Before
    fun setUp() {
        BreadcrumbManager.clear()
    }

    /**
     * Cleans up the in-memory breadcrumb buffer after each test method execution.
     */
    @After
    fun tearDown() {
        BreadcrumbManager.clear()
    }

    /**
     * Verifies that [BreadcrumbManager.addBreadcrumb] formats log entries with timestamp
     * and correctly stores them in the in-memory buffer.
     */
    @Test
    @CoveredRequirement("6.1.2.9.1")
    fun testAddBreadcrumb_StoresCorrectly() {
        BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action 1")
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()

        assertEquals(1, breadcrumbs.size)
        assertTrue("Breadcrumb should contain prefix and message", breadcrumbs[0].contains("[EXPECTED] Action 1"))
    }

    /**
     * Verifies that [BreadcrumbManager] operates as a circular buffer capped at 20 entries,
     * evicting the oldest entries when capacity is exceeded.
     */
    @Test
    @CoveredRequirement("6.1.2.9.1")
    fun testAddBreadcrumb_CircularBuffer_Rotates() {
        for (i in 1..25) {
            BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action $i")
        }
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()

        assertEquals(20, breadcrumbs.size)
        assertTrue("First entry should be rotated to Action 6", breadcrumbs[0].contains("[EXPECTED] Action 6"))
        assertTrue("Last entry should be Action 25", breadcrumbs.last().contains("[EXPECTED] Action 25"))
    }

    /**
     * Verifies that [BreadcrumbManager.clear] removes all entries from the buffer.
     */
    @Test
    @CoveredRequirement("6.1.2.9.1")
    fun testClear_RemovesAll() {
        BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action 1")
        BreadcrumbManager.clear()

        assertTrue("Breadcrumb buffer should be empty after clear()", BreadcrumbManager.getBreadcrumbs().isEmpty())
    }
}
