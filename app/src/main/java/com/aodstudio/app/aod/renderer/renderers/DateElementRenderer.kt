package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.core.util.formatDate
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for DATE type elements.
 * Supports flexible date formatting (e.g. MON • AUG 10, 10/08/2026, MONDAY).
 */
class DateElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val format = element.properties[AODElement.PROP_FORMAT] ?: "EEE • MMM dd"
        val dateString = formatDate(format, context.date)

        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        canvas.drawText(dateString, drawX, drawY, paint)
    }
}
