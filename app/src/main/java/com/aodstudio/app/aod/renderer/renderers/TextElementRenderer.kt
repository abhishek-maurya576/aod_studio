package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for TEXT type elements.
 * Draws static text defined in element properties or element name.
 */
class TextElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val textContent = element.properties[AODElement.PROP_TEXT]
            ?: element.name.ifBlank { "Text" }

        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactorX)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = element.x * context.scaleFactorX
        val drawY = element.y * context.scaleFactorY

        canvas.drawText(textContent, drawX, drawY, paint)
    }
}
