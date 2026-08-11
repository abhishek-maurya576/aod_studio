package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for IMAGE elements using uniform scale and coordinate transformation.
 */
class ImageElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val w = element.width * context.scaleFactor
        val h = element.height * context.scaleFactor

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            style = Paint.Style.STROKE
            strokeWidth = element.style.strokeWidth * context.scaleFactor
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        // Draw decorative vector frame / placeholder graphic
        val rect = RectF(drawX - w / 2f, drawY - h / 2f, drawX + w / 2f, drawY + h / 2f)
        canvas.drawRoundRect(rect, element.style.cornerRadius * context.scaleFactor, element.style.cornerRadius * context.scaleFactor, paint)
    }
}
