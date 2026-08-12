package com.aodstudio.app.aod.renderer

import android.graphics.Bitmap
import java.util.Date

/**
 * Live runtime state passed to element renderers during Canvas draw cycles.
 * Uses uniform scaling (scaleFactor) and content offsets (contentOffsetX, contentOffsetY)
 * to ensure themes maintain 100% exact visual proportions across all container aspect ratios.
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
    val mediaAlbum: String? = null,
    val mediaAlbumArt: Bitmap? = null,
    val mediaIsPlaying: Boolean = false,
    val mediaProgressMs: Long = 0L,
    val mediaDurationMs: Long = 0L,
    val hasActiveMedia: Boolean = false,
    val burnInOffsetX: Float = 0f,
    val burnInOffsetY: Float = 0f,
    val viewWidth: Int = 1080,
    val viewHeight: Int = 2400,
    val scaleFactor: Float = 1f,
    val contentOffsetX: Float = 0f,
    val contentOffsetY: Float = 0f
) {
    // Backwards compatibility properties for sub-renderers
    val scaleFactorX: Float get() = scaleFactor
    val scaleFactorY: Float get() = scaleFactor
}

