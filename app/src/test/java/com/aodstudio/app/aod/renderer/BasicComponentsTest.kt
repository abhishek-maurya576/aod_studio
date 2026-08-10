package com.aodstudio.app.aod.renderer

import com.aodstudio.app.aod.renderer.renderers.AnalogClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.BatteryElementRenderer
import com.aodstudio.app.aod.renderer.renderers.ClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.DateElementRenderer
import com.aodstudio.app.aod.renderer.renderers.ShapeElementRenderer
import com.aodstudio.app.aod.renderer.renderers.TextElementRenderer
import com.aodstudio.app.core.util.formatDate
import com.aodstudio.app.core.util.formatTime
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Date

/**
 * Unit tests for Phase 5 basic components (Digital Clock formats, Date formats, Analog Clock renderer, shapes).
 */
class BasicComponentsTest {

    @Test
    fun `formatTime formats HH mm correctly`() {
        val date = Date()
        val time24 = formatTime("HH:mm", date)
        assertNotNull(time24)
        assertEquals(5, time24.length) // e.g. "21:45"
    }

    @Test
    fun `formatTime formats 12h time with AM PM correctly`() {
        val date = Date()
        val time12 = formatTime("hh:mm a", date).uppercase()
        assertNotNull(time12)
        assertTrue(time12.contains("AM") || time12.contains("PM"))
    }

    @Test
    fun `formatDate supports multiple date patterns`() {
        val date = Date()
        val pattern1 = formatDate("EEE • MMM dd", date)
        val pattern2 = formatDate("yyyy-MM-dd", date)
        val pattern3 = formatDate("EEEE", date)

        assertNotNull(pattern1)
        assertNotNull(pattern2)
        assertNotNull(pattern3)
        assertEquals(10, pattern2.length) // e.g. "2026-08-10"
    }

    @Test
    fun `AnalogClockElementRenderer instantiates cleanly`() {
        val renderer = AnalogClockElementRenderer()
        assertNotNull(renderer)
    }

    @Test
    fun `all basic element renderers instantiate cleanly`() {
        val clockRenderer = ClockElementRenderer()
        val dateRenderer = DateElementRenderer()
        val textRenderer = TextElementRenderer()
        val batteryRenderer = BatteryElementRenderer()
        val shapeRenderer = ShapeElementRenderer()

        assertNotNull(clockRenderer)
        assertNotNull(dateRenderer)
        assertNotNull(textRenderer)
        assertNotNull(batteryRenderer)
        assertNotNull(shapeRenderer)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
