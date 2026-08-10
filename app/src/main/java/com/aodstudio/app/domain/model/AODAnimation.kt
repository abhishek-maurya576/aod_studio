package com.aodstudio.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Animation specification for AOD elements.
 * Supports fade, scale, pulse, glow, orbit, etc.
 * Must respect battery saver constraints.
 */
@Serializable
data class AODAnimation(
    val type: String = "NONE",          // NONE, FADE, SCALE, SLIDE, ROTATE, PULSE, GLOW, ORBIT
    val durationMs: Long = 1200L,
    val repeat: Boolean = true,
    val easing: String = "LINEAR"
) {
    companion object {
        const val TYPE_NONE = "NONE"
        const val TYPE_FADE = "FADE"
        const val TYPE_SCALE = "SCALE"
        const val TYPE_SLIDE = "SLIDE"
        const val TYPE_ROTATE = "ROTATE"
        const val TYPE_PULSE = "PULSE"
        const val TYPE_GLOW = "GLOW"
        const val TYPE_ORBIT = "ORBIT"
    }
}
