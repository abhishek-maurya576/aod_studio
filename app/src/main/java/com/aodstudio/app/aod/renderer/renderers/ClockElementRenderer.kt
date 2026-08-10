package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.core.util.formatTime
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for CLOCK type elements.
 * Supports digital time formatting: HH:mm, hh:mm a, HH:mm:ss, etc.
 */
class ClockElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val format = element.properties[AODElement.PROP_FORMAT] ?: "HH:mm"
        val timeString = formatTime(format, context.date)

        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactorX)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = element.x * context.scaleFactorX
        val drawY = element.y * context.scaleFactorY

        canvas.drawText(timeString, drawX, drawY, paint)
    }
}
