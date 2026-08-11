package com.aodstudio.app.aod.renderer

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODStyle

/**
 * Utility functions for Canvas painting, color parsing, typography, and uniform coordinate transformation.
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
     * Calculates final transformed screen X coordinate applying uniform scale factor,
     * content offset, and burn-in offset.
     */
    fun getDrawX(element: AODElement, context: RenderContext): Float {
        return context.contentOffsetX + (element.x + context.burnInOffsetX) * context.scaleFactor
    }

    /**
     * Calculates final transformed screen Y coordinate applying uniform scale factor,
     * content offset, and burn-in offset.
     */
    fun getDrawY(element: AODElement, context: RenderContext): Float {
        return context.contentOffsetY + (element.y + context.burnInOffsetY) * context.scaleFactor
    }

    /**
     * Creates and configures an Android Paint instance based on AODStyle properties using uniform scaleFactor.
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
