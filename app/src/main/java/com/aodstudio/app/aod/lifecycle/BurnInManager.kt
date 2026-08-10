package com.aodstudio.app.aod.lifecycle

import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.core.util.clamp
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin

/**
 * AMOLED Burn-in protection manager.
 * Calculates 5-minute periodic pixel shift offsets bounded by max ±4px
 * to prevent static subpixel degradation on OLED/AMOLED displays (Vivo T4 Pro).
 */
@Singleton
class BurnInManager @Inject constructor() {

    private var currentStep = 0
    private var lastShiftTimestamp: Long = 0L

    /**
     * Calculates the current burn-in pixel shift offset based on current timestamp.
     * Shifts position periodically (default every 5 minutes / 300,000 ms).
     */
    fun calculateOffset(currentTimeMs: Long = System.currentTimeMillis()): BurnInOffset {
        val maxOffset = ThemeConfig.AOD.BURN_IN_MAX_OFFSET_PX.toFloat()
        val shiftInterval = ThemeConfig.AOD.BURN_IN_SHIFT_INTERVAL_MS

        val stepIndex = (currentTimeMs / shiftInterval).toInt()

        // Circular orbital trajectory for smooth, non-disruptive shifting
        val angleRad = Math.toRadians((stepIndex * 45 % 360).toDouble()).toFloat()
        val rawX = cos(angleRad) * maxOffset
        val rawY = sin(angleRad) * maxOffset

        val clampedX = rawX.clamp(-maxOffset, maxOffset)
        val clampedY = rawY.clamp(-maxOffset, maxOffset)

        return BurnInOffset(
            offsetX = clampedX,
            offsetY = clampedY
        )
    }

    /**
     * Caps element opacity to maximum 85% to protect AMOLED subpixels from wear.
     */
    fun capOpacityForBurnInProtection(requestedOpacity: Float): Float {
        val maxSafeOpacity = 0.85f
        return requestedOpacity.coerceAtMost(maxSafeOpacity)
    }
}
