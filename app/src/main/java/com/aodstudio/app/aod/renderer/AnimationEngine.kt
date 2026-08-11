package com.aodstudio.app.aod.renderer

import com.aodstudio.app.domain.model.AODAnimation
import kotlin.math.sin

/**
 * Battery-friendly micro-animation engine for AOD elements.
 * Calculates dynamic property offsets (alpha, scale, rotation, glow) based on time.
 */
class AnimationEngine {

    /**
     * Animated state evaluation output.
     */
    data class AnimatedProperties(
        val opacityMultiplier: Float = 1.0f,
        val scaleMultiplier: Float = 1.0f,
        val rotationOffsetDeg: Float = 0.0f
    )

    /**
     * Evaluates animation transformation for an element given current time.
     */
    fun evaluate(animation: AODAnimation?, currentTimeMs: Long): AnimatedProperties {
        if (animation == null || animation.type.equals("NONE", ignoreCase = true) || animation.type.isBlank()) {
            return AnimatedProperties()
        }

        val duration = animation.durationMs.coerceAtLeast(500)
        val progress = ((currentTimeMs % duration).toFloat() / duration) // 0.0f to 1.0f

        return when (animation.type.uppercase()) {
            "PULSE" -> {
                // Smooth sine wave scale between 0.95 and 1.05
                val factor = 1.0f + 0.05f * sin(progress * 2 * Math.PI).toFloat()
                AnimatedProperties(scaleMultiplier = factor)
            }
            "FADE" -> {
                // Smooth sine opacity modulation between 0.4 and 1.0
                val alpha = 0.7f + 0.3f * sin(progress * 2 * Math.PI).toFloat()
                AnimatedProperties(opacityMultiplier = alpha)
            }
            "ROTATE", "ORBIT" -> {
                // Smooth continuous 360 degree rotation
                val angle = progress * 360f
                AnimatedProperties(rotationOffsetDeg = angle)
            }
            "GLOW" -> {
                val alpha = 0.8f + 0.2f * sin(progress * 2 * Math.PI).toFloat()
                AnimatedProperties(opacityMultiplier = alpha)
            }
            else -> AnimatedProperties()
        }
    }
}
