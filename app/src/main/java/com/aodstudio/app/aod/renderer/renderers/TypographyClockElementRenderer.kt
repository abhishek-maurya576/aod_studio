package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.core.util.formatTime
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for Typography and Stacked Clock styles.
 * Renders large stacked digits (Hours above Minutes, e.g. "03" over "45")
 * or custom typographic time layouts.
 */
class TypographyClockElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val hours = formatTime("HH", context.date)
        val minutes = formatTime("mm", context.date)

        val paintHours = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        paintHours.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val paintMinutes = RendererUtils.createTextPaint(element.style, context.scaleFactor).apply {
            color = RendererUtils.parseColor(element.style.accentColor)
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val fontSpacing = (element.style.fontSize * 0.9f) * context.scaleFactor

        // Top line: Hours
        canvas.drawText(hours, drawX, drawY - fontSpacing * 0.4f, paintHours)

        // Bottom line: Minutes
        canvas.drawText(minutes, drawX, drawY + fontSpacing * 0.6f, paintMinutes)
    }
}
