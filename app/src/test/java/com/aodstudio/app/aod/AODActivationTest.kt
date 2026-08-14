package com.aodstudio.app.aod

import android.content.Context
import com.aodstudio.app.aod.overlay.AODWindowOverlayManager
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.SettingsRepository
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for AOD Activation and Overlay Manager state lifecycle.
 */
class AODActivationTest {

    @Test
    fun `AODWindowOverlayManager initial showing state is false`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockBatteryRepo = mockk<BatteryRepository>(relaxed = true)
        val mockNotificationRepo = mockk<NotificationRepository>(relaxed = true)
        val mockMediaRepo = mockk<MediaRepository>(relaxed = true)
        val mockSettingsRepo = mockk<SettingsRepository>(relaxed = true)

        val manager = AODWindowOverlayManager(
            context = mockContext,
            batteryRepository = mockBatteryRepo,
            notificationRepository = mockNotificationRepo,
            mediaRepository = mockMediaRepo,
            settingsRepository = mockSettingsRepo
        )
        assertFalse(manager.isShowing())
    }

    @Test
    fun `default theme passes integrity check for activation`() {
        val theme = AODTheme.createDefaultTheme("Activation Test")
        assertNotNull(theme)
        assertFalse(theme.elements.isEmpty())
    }
}
