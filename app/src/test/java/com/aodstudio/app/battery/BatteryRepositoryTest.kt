package com.aodstudio.app.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for BatteryState data model and PluggedType values.
 */
class BatteryRepositoryTest {

    @Test
    fun `BatteryState default values are valid`() {
        val state = BatteryState()
        assertEquals(100, state.percentage)
        assertFalse(state.isCharging)
        assertFalse(state.isFull)
        assertEquals(BatteryState.PluggedType.NONE, state.pluggedType)
        assertEquals("GOOD", state.health)
    }

    @Test
    fun `BatteryState custom charging parameters hold values`() {
        val state = BatteryState(
            percentage = 45,
            isCharging = true,
            isFull = false,
            pluggedType = BatteryState.PluggedType.AC
        )

        assertEquals(45, state.percentage)
        assertEquals(true, state.isCharging)
        assertEquals(BatteryState.PluggedType.AC, state.pluggedType)
    }
}
