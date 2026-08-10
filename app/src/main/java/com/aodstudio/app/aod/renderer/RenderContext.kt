package com.aodstudio.app.aod.renderer

import java.util.Date

/**
 * Live runtime state passed to element renderers during Canvas draw cycles.
 * Encapsulates system state (time, battery, notifications, media) and display state (burn-in offsets, scale).
 */
data class RenderContext(
    val date: Date = Date(),
    val batteryPercentage: Int = 100,
    val isCharging: Boolean = false,
    val isBatteryFull: Boolean = false,
    val notificationCount: Int = 0,
    val notificationPackages: List<String> = emptyList(),
    val mediaTitle: String? = null,
    val mediaArtist: String? = null,
    val mediaIsPlaying: Boolean = false,
    val burnInOffsetX: Float = 0f,
    val burnInOffsetY: Float = 0f,
    val viewWidth: Int = 1080,
    val viewHeight: Int = 2400,
    val scaleFactorX: Float = 1f,
    val scaleFactorY: Float = 1f
)
