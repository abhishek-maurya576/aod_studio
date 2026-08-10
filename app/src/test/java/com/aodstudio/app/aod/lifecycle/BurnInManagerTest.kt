package com.aodstudio.app.aod.lifecycle

import com.aodstudio.app.config.ThemeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BurnInManager pixel shift calculation, bounds constraints, and opacity caps.
 */
class BurnInManagerTest {

    private lateinit var burnInManager: BurnInManager

    @Before
    fun setup() {
        burnInManager = BurnInManager()
    }

    @Test
    fun `calculateOffset stays strictly within max offset bounds`() {
        val maxOffset = ThemeConfig.AOD.BURN_IN_MAX_OFFSET_PX.toFloat()

        // Test multiple time intervals
        for (i in 0..100) {
            val fakeTimeMs = i * ThemeConfig.AOD.BURN_IN_SHIFT_INTERVAL_MS
            val offset = burnInManager.calculateOffset(fakeTimeMs)

            assertTrue("OffsetX $offset should be >= -$maxOffset", offset.offsetX >= -maxOffset)
            assertTrue("OffsetX $offset should be <= $maxOffset", offset.offsetX <= maxOffset)
            assertTrue("OffsetY $offset should be >= -$maxOffset", offset.offsetY >= -maxOffset)
            assertTrue("OffsetY $offset should be <= $maxOffset", offset.offsetY <= maxOffset)
        }
    }

    @Test
    fun `capOpacityForBurnInProtection caps opacity at 85 percent`() {
        assertEquals(0.85f, burnInManager.capOpacityForBurnInProtection(1.0f), 0.001f)
        assertEquals(0.85f, burnInManager.capOpacityForBurnInProtection(0.90f), 0.001f)
        assertEquals(0.50f, burnInManager.capOpacityForBurnInProtection(0.50f), 0.001f)
    }

    @Test
    fun `offsets change across shift intervals`() {
        val t0 = 0L
        val t1 = ThemeConfig.AOD.BURN_IN_SHIFT_INTERVAL_MS * 1 // +5 mins
        val t2 = ThemeConfig.AOD.BURN_IN_SHIFT_INTERVAL_MS * 2 // +10 mins

        val offset0 = burnInManager.calculateOffset(t0)
        val offset1 = burnInManager.calculateOffset(t1)
        val offset2 = burnInManager.calculateOffset(t2)

        assertTrue("Offsets should shift between intervals", offset0 != offset1 || offset1 != offset2)
    }
}
