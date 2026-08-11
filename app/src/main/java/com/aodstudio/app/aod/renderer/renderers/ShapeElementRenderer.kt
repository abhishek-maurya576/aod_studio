package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for SHAPE, LINE, RING, and PROGRESS elements using uniform scale and coordinate transformation.
 */
class ShapeElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val shapeType = element.properties[AODElement.PROP_SHAPE_TYPE] ?: "CIRCLE"

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            strokeWidth = element.style.strokeWidth * context.scaleFactor
            style = if (element.style.fill) Paint.Style.FILL else Paint.Style.STROKE
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val w = element.width * context.scaleFactor
        val h = element.height * context.scaleFactor

        when (shapeType.uppercase()) {
            "CIRCLE", "RING" -> {
                val radius = w / 2f
                if (shapeType.equals("RING", ignoreCase = true)) paint.style = Paint.Style.STROKE
                canvas.drawCircle(drawX, drawY, radius, paint)
            }
            "LINE" -> {
                canvas.drawLine(drawX - w / 2f, drawY, drawX + w / 2f, drawY, paint)
            }
            "RECTANGLE" -> {
                val rect = RectF(drawX - w / 2f, drawY - h / 2f, drawX + w / 2f, drawY + h / 2f)
                canvas.drawRoundRect(rect, element.style.cornerRadius * context.scaleFactor, element.style.cornerRadius * context.scaleFactor, paint)
            }
            else -> {
                val rect = RectF(drawX - w / 2f, drawY - h / 2f, drawX + w / 2f, drawY + h / 2f)
                canvas.drawRect(rect, paint)
            }
        }
    }
}
