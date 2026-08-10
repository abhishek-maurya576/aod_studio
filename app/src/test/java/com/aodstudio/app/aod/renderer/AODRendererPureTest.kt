package com.aodstudio.app.aod.renderer

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure unit tests for renderer data processing and context state handling.
 */
class AODRendererPureTest {

    @Test
    fun `RenderContext default values are valid`() {
        val context = RenderContext()
        assertEquals(100, context.batteryPercentage)
        assertEquals(false, context.isCharging)
        assertEquals(0, context.notificationCount)
        assertEquals(1080, context.viewWidth)
        assertEquals(2400, context.viewHeight)
        assertEquals(1f, context.scaleFactorX)
        assertEquals(1f, context.scaleFactorY)
    }

    @Test
    fun `RenderContext custom state retains passed properties`() {
        val context = RenderContext(
            batteryPercentage = 75,
            isCharging = true,
            notificationCount = 4,
            mediaTitle = "Cyberpunk Track",
            mediaArtist = "Synthesizer",
            mediaIsPlaying = true,
            burnInOffsetX = 3.5f,
            burnInOffsetY = -2.0f
        )

        assertEquals(75, context.batteryPercentage)
        assertTrue(context.isCharging)
        assertEquals(4, context.notificationCount)
        assertEquals("Cyberpunk Track", context.mediaTitle)
        assertEquals("Synthesizer", context.mediaArtist)
        assertTrue(context.mediaIsPlaying)
        assertEquals(3.5f, context.burnInOffsetX, 0.001f)
        assertEquals(-2.0f, context.burnInOffsetY, 0.001f)
    }

    @Test
    fun `elements sorting by zIndex is correct`() {
        val elem1 = AODElement(id = "e1", type = AODElementType.CLOCK, zIndex = 5)
        val elem2 = AODElement(id = "e2", type = AODElementType.DATE, zIndex = 1)
        val elem3 = AODElement(id = "e3", type = AODElementType.BATTERY, zIndex = 3)

        val theme = AODTheme(name = "Sorted Theme", elements = listOf(elem1, elem2, elem3))
        val sorted = theme.elements.sortedBy { it.zIndex }

        assertEquals("e2", sorted[0].id)
        assertEquals("e3", sorted[1].id)
        assertEquals("e1", sorted[2].id)
    }
}
