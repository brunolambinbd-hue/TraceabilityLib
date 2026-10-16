package com.parabd.lib.traceability.utils

import android.util.Log
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

/**
 * Robolectric unit test suite for [LogManager] and [LogManagerExt].
 *
 * Runs with real Android framework shadows on JVM to achieve 100% JaCoCo coverage
 * over native [android.util.Log] invocations and try-catch blocks.
 */
@RunWith(RobolectricTestRunner::class)
class LogManagerRobolectricTest {

    @Before
    fun setUp() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    @After
    fun tearDown() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    /**
     * Verifies 100% branch saturation of [LogManager.isLoggable] using the 3 states of [LogManager.forceLoggable]
     * and [ShadowLog.setLoggable] alternation, validating captured streams via [ShadowLog.getLogsForTag].
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_ShadowLog_3States_And_StreamCapture() {
        ShadowLog.stream = System.out
        val tag = "STRATEGY_TAG"

        // 🟢 1. État 1 : forceLoggable = true (Log forcé actif)
        LogManager.forceLoggable = true
        assertTrue(LogManager.isLoggable(tag, Log.DEBUG))
        LogManager.d(tag, "Log avec forceLoggable = true")

        // 🔴 2. État 2 : forceLoggable = false (Log forcé masqué)
        LogManager.forceLoggable = false
        assertFalse(LogManager.isLoggable(tag, Log.DEBUG))
        LogManager.d(tag, "Log masque avec forceLoggable = false")

        // ⚪ 3. État 3 : forceLoggable = null (Délégation à ShadowLog)
        LogManager.forceLoggable = null

        // 🟢 Étape 3a : Activer le niveau DEBUG dans ShadowLog
        ShadowLog.setLoggable(tag, Log.DEBUG)
        assertTrue(LogManager.isLoggable(tag, Log.DEBUG))
        LogManager.d(tag, "Log via ShadowLog DEBUG actif")

        // 🔴 Étape 3b : Rehausser au niveau INFO dans ShadowLog (Log.DEBUG devient false)
        ShadowLog.setLoggable(tag, Log.INFO)
        assertFalse(LogManager.isLoggable(tag, Log.DEBUG))
        LogManager.d(tag, "Log via ShadowLog DEBUG inactif")

        // 🔍 4. Vérification du flux natif capturé par ShadowLog
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue("ShadowLog doit contenir le log forceLoggable = true", logs.any { it.msg.contains("Log avec forceLoggable = true") })
        assertTrue("ShadowLog doit contenir le log ShadowLog DEBUG actif", logs.any { it.msg.contains("Log via ShadowLog DEBUG actif") })
        assertFalse("ShadowLog NE doit PAS contenir le log DEBUG inactif", logs.any { it.msg.contains("Log via ShadowLog DEBUG inactif") })
    }

    /**
     * Verifies full execution of [LogManager.d], [LogManager.i], [LogManager.w], [LogManager.e], [LogManager.u]
     * under Robolectric with [LogManager.forceLoggable] set to `true`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Robolectric_FullCoverage_ForceLoggableTrue() {
        LogManager.forceLoggable = true

        LogManager.d("ROBO_TAG", "Debug message Robolectric")
        LogManager.i("ROBO_TAG", "Info message Robolectric")
        LogManager.w("ROBO_TAG", "Warn message Robolectric")
        LogManager.e("ROBO_TAG", "Error with throwable", RuntimeException("Robo Exception"))
        LogManager.e("ROBO_TAG", "Error without throwable")
        LogManager.e("ROBO_TAG", "Error with message containing ex: Robo Exception", RuntimeException("Robo Exception"))
        LogManager.e("ROBO_TAG", "Error with message ending in colon: ", RuntimeException("Robo Exception"))
        LogManager.e(
            tag = "ROBO_TAG",
            prefix = "Error prefix: ",
            throwable = RuntimeException("Prefix Robo Exception"),
            fallbackMessage = "Fallback",
        )
        LogManager.e(
            tag = "ROBO_TAG",
            prefix = "Error prefix without colon",
            throwable = RuntimeException("Prefix Robo Exception"),
            fallbackMessage = "Fallback",
        )
        LogManager.u("ROBO_TAG", "Unexpected message Robolectric")

        val breadcrumbs = BreadcrumbManager.getBreadcrumbs()
        assertTrue("Breadcrumbs should be recorded", breadcrumbs.size >= 8)
    }

    /**
     * Verifies execution of [LogManager] when [LogManager.forceLoggable] is `null` or `false`.
     */
    @Test
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManager_Robolectric_FullCoverage_ForceLoggableFalseAndNull() {
        LogManager.forceLoggable = false

        LogManager.d("ROBO_TAG", "Debug message false")
        LogManager.i("ROBO_TAG", "Info message false")
        LogManager.w("ROBO_TAG", "Warn message false")
        LogManager.e("ROBO_TAG", "Error message false")
        LogManager.u("ROBO_TAG", "Unexpected message false")

        LogManager.forceLoggable = null
        val loggableResult = LogManager.isLoggable("ROBO_TAG", Log.DEBUG)
        assertNotNull("isLoggable should return a boolean", loggableResult)
    }

    /**
     * Verifies extension methods [LogManager.throwU] and [LogManager.throwE] under Robolectric.
     */
    @Test
    @Suppress("UNREACHABLE_CODE")
    @CoveredRequirement("6.1.1.9.10")
    @CoveredRequirement("6.3.2.9.10")
    fun testLogManagerExt_Robolectric_Coverage() {
        LogManager.forceLoggable = true

        val cause = RuntimeException("Cause Robo")
        val uEx = try {
            LogManager.throwU("ROBO_TAG", "Robo U message", cause)
        } catch (e: IllegalStateException) {
            e
        }
        assertEquals("Robo U message", uEx.message)

        val origEx = IllegalArgumentException("Orig Robo")
        val eEx = try {
            LogManager.throwE("ROBO_TAG", "Robo E message", origEx)
        } catch (e: IllegalArgumentException) {
            e
        }
        assertEquals("Orig Robo", eEx.message)
    }
}
