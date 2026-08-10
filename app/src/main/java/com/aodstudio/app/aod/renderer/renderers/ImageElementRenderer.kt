package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for IMAGE elements.
 * Renders decorative vector icons, custom graphics, and image placeholders on the AOD canvas.
 */
class ImageElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = element.x * context.scaleFactorX
        val drawY = element.y * context.scaleFactorY
        val w = element.width * context.scaleFactorX
        val h = element.height * context.scaleFactorY

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            style = Paint.Style.STROKE
            strokeWidth = element.style.strokeWidth * context.scaleFactorX
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        // Draw decorative vector frame / placeholder graphic
        val rect = RectF(drawX - w / 2f, drawY - h / 2f, drawX + w / 2f, drawY + h / 2f)
        canvas.drawRoundRect(rect, element.style.cornerRadius * context.scaleFactorX, element.style.cornerRadius * context.scaleFactorX, paint)
    }
}
