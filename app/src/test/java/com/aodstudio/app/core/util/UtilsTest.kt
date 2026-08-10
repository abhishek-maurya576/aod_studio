package com.aodstudio.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for core utility functions.
 * Verifies ID generation, date formatting, and clamping.
 */
class UtilsTest {

    @Test
    fun `generateId returns non-empty unique strings`() {
        val id1 = generateId()
        val id2 = generateId()

        assertTrue("ID should not be empty", id1.isNotEmpty())
        assertTrue("IDs should be unique", id1 != id2)
    }

    @Test
    fun `getDayOfWeekShort returns 3-letter uppercase day`() {
        val day = getDayOfWeekShort()
        assertEquals("Day abbreviation should be 3 characters", 3, day.length)
        assertEquals("Day should be uppercase", day, day.uppercase())
    }

    @Test
    fun `getMonthShort returns 3-letter uppercase month`() {
        val month = getMonthShort()
        assertEquals("Month abbreviation should be 3 characters", 3, month.length)
        assertEquals("Month should be uppercase", month, month.uppercase())
    }

    @Test
    fun `float clamp constrains value within range`() {
        assertEquals(5f, 5f.clamp(0f, 10f), 0.001f)
        assertEquals(0f, (-5f).clamp(0f, 10f), 0.001f)
        assertEquals(10f, 15f.clamp(0f, 10f), 0.001f)
    }

    @Test
    fun `int clamp constrains value within range`() {
        assertEquals(5, 5.clamp(0, 10))
        assertEquals(0, (-5).clamp(0, 10))
        assertEquals(10, 15.clamp(0, 10))
    }
}
