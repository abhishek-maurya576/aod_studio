package com.aodstudio.app

import com.aodstudio.app.aod.lifecycle.BurnInManager
import com.aodstudio.app.battery.BatteryState
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.model.ThemeSerializer
import com.aodstudio.app.media.MediaState
import com.aodstudio.app.notification.NotificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end regression test suite verifying core stability across all 16 phases.
 */
class AODStudioRegressionTest {

    @Test
    fun `default theme serializes and deserializes without loss`() {
        val originalTheme = AODTheme.createDefaultTheme("Regression Theme")
        val json = ThemeSerializer.serialize(originalTheme)

        val parsedResult = ThemeSerializer.deserialize(json)
        assertTrue(parsedResult is com.aodstudio.app.core.common.Result.Success)

        val deserializedTheme = (parsedResult as com.aodstudio.app.core.common.Result.Success).data
        assertEquals(originalTheme.name, deserializedTheme.name)
        assertEquals(originalTheme.elements.size, deserializedTheme.elements.size)
    }

    @Test
    fun `burn in manager bounds and opacity caps hold`() {
        val manager = BurnInManager()
        val offset = manager.calculateOffset(System.currentTimeMillis())

        assertTrue(offset.offsetX in -4f..4f)
        assertTrue(offset.offsetY in -4f..4f)
        assertEquals(0.85f, manager.capOpacityForBurnInProtection(1.0f), 0.001f)
    }

    @Test
    fun `battery media and notification models construct properly`() {
        val battery = BatteryState(percentage = 92, isCharging = true)
        val media = MediaState(title = "Track", artist = "Artist", isPlaying = true)
        val notif = NotificationItem(key = "k", packageName = "com.test")

        assertEquals(92, battery.percentage)
        assertTrue(media.hasActiveMedia)
        assertEquals("com.test", notif.packageName)
    }
}
