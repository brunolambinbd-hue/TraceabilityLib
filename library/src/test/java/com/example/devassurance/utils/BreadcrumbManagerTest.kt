package com.parabd.lib.traceability.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class BreadcrumbManagerTest {

    @Before
    fun setup() {
        BreadcrumbManager.clear()
    }

    @Test
    fun testAddBreadcrumb_StoresCorrectly() {
        BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action 1")
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertThat(breadcrumbs).hasSize(1)
        // Check that it contains the message and prefix (ignoring the timestamp at the beginning)
        assertThat(breadcrumbs[0]).contains("[EXPECTED] Action 1")
    }

    @Test
    fun testAddBreadcrumb_CircularBuffer_Rotates() {
        // MAX_BREADCRUMBS is 20
        for (i in 1..25) {
            BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action $i")
        }
        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertThat(breadcrumbs).hasSize(20)
        // First 5 should be gone (1..5), so it should start with "Action 6"
        assertThat(breadcrumbs[0]).contains("[EXPECTED] Action 6")
        assertThat(breadcrumbs.last()).contains("[EXPECTED] Action 25")
    }

    @Test
    fun testClear_RemovesAll() {
        BreadcrumbManager.addBreadcrumb("[EXPECTED]", "Action 1")
        BreadcrumbManager.clear()
        assertThat(BreadcrumbManager.getBreadcrumbs()).isEmpty()
    }
}

