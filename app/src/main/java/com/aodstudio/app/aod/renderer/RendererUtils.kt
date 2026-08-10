package com.aodstudio.app.aod.renderer

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.aodstudio.app.domain.model.AODStyle

/**
 * Utility functions for Canvas painting, color parsing, and typography.
 */
object RendererUtils {

    /**
     * Parses a hex color string (e.g., "#FFFFFF", "#99FFFFFF", "#000000") safely.
     * Returns fallback color on parse failure.
     */
    fun parseColor(colorString: String, fallback: Int = Color.WHITE): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Creates and configures an Android Paint instance based on AODStyle properties.
     */
    fun createTextPaint(style: AODStyle, scale: Float = 1f): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(style.color)
            textSize = style.fontSize * scale
            strokeWidth = style.strokeWidth * scale

            typeface = when (style.fontFamily.uppercase()) {
                "MONO" -> Typeface.MONOSPACE
                "DISPLAY" -> Typeface.create(Typeface.DEFAULT, parseFontWeight(style.fontWeight))
                else -> Typeface.create(Typeface.DEFAULT, parseFontWeight(style.fontWeight))
            }

            textAlign = when (style.alignment.uppercase()) {
                "LEFT" -> Paint.Align.LEFT
                "RIGHT" -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }

            if (!style.fill) {
                this.style = Paint.Style.STROKE
            }
        }
    }

    /**
     * Converts font weight string to Typeface style integer.
     */
    fun parseFontWeight(weightString: String): Int {
        return when (weightString.uppercase()) {
            "BOLD" -> Typeface.BOLD
            "ITALIC" -> Typeface.ITALIC
            "BOLD_ITALIC" -> Typeface.BOLD_ITALIC
            else -> Typeface.NORMAL
        }
    }
}
