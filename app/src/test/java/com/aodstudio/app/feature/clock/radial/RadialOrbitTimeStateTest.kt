package com.aodstudio.app.feature.clock.radial

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for RadialOrbitTimeState calculation and formatting.
 */
class RadialOrbitTimeStateTest {

    @Test
    fun `time state correctly formats hour, minute, and continuous seconds and minutes`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 23)
            set(Calendar.SECOND, 30)
            set(Calendar.MILLISECOND, 0)
        }

        val state = RadialOrbitTimeState.fromCalendar(calendar, is24Hour = true)

        assertEquals(19, state.hour)
        assertEquals(23, state.minute)
        assertEquals(30, state.second)
        assertEquals(0, state.millisecond)
        assertEquals("19", state.formattedHour)
        assertEquals("23", state.formattedMinute)
        assertEquals(30.0f, state.continuousSecond, 0.001f)
        assertEquals(23.5f, state.continuousMinute, 0.001f)
    }

    @Test
    fun `continuous second and minute calculate continuous fractional progress`() {
        val state = RadialOrbitTimeState(
            hour = 8,
            minute = 15,
            second = 30,
            millisecond = 750,
            dateFormatted = "15 AUG 2026",
            dayOfWeek = "SATURDAY"
        )

        assertEquals(30.75f, state.continuousSecond, 0.001f)
        assertEquals(15f + (30.75f / 60f), state.continuousMinute, 0.001f)
        assertEquals("08", state.formattedHour)
        assertEquals("15", state.formattedMinute)
    }
}
