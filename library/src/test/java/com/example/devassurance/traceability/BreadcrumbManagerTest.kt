package com.example.devassurance.traceability

import com.parabd.lib.traceability.CoveredRequirement
import com.parabd.lib.traceability.utils.BreadcrumbManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BreadcrumbManager] verifying storing, rotation, and clearing of breadcrumbs.
 */
class BreadcrumbManagerTest {

    /**
     * Resets the breadcrumb buffer before each test execution.
     */
    @Before
    fun setup() {
        BreadcrumbManager.clear()
    }

    /**
     * Verifies that adding a breadcrumb correctly formats and stores the message in the buffer.
     */
    @Test
    @CoveredRequirement("6.1.2.9.1")
    fun testAddBreadcrumb_StoresCorrectly() {
        BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action 1")
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertEquals(1, breadcrumbs.size)
        // Check that it contains the message and prefix (ignoring the timestamp at the beginning)
        assertTrue(breadcrumbs[0].contains("[EXPECTED] Action 1"))
    }

    /**
     * Verifies that the circular buffer respects [BreadcrumbManager]'s maximum size (20)
     * by evicting older breadcrumbs when new ones are added.
     */
    @Test
    @CoveredRequirement("6.1.2.9.1")
    fun testAddBreadcrumb_CircularBuffer_Rotates() {
        // MAX_BREADCRUMBS is 20
        for (i in 1..25) {
            BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action $i")
        }
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertEquals(20, breadcrumbs.size)
        // First 5 should be gone (1..5), so it should start with "Action 6"
        assertTrue(breadcrumbs[0].contains("[EXPECTED] Action 6"))
        assertTrue(breadcrumbs.last().contains("[EXPECTED] Action 25"))
    }

    /**
     * Verifies that clearing [BreadcrumbManager] removes all breadcrumbs from memory.
     */
    @Test
    @CoveredRequirement("6.1.2.9.1")
    fun testClear_RemovesAll() {
        BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action 1")
        BreadcrumbManager.clear()
        assertTrue(BreadcrumbManager.getBreadcrumbs().isEmpty())
    }
}
