package com.aodstudio.app.aod

import android.content.Context
import com.aodstudio.app.aod.overlay.AODWindowOverlayManager
import com.aodstudio.app.domain.model.AODTheme
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for Phase 15 AOD Activation and Overlay Manager state lifecycle.
 */
class AODActivationTest {

    @Test
    fun `AODWindowOverlayManager initial showing state is false`() {
        val mockContext = mockk<Context>(relaxed = true)
        val manager = AODWindowOverlayManager(mockContext)
        assertFalse(manager.isShowing())
    }

    @Test
    fun `default theme passes integrity check for activation`() {
        val theme = AODTheme.createDefaultTheme("Activation Test")
        assertNotNull(theme)
        assertFalse(theme.elements.isEmpty())
    }
}
