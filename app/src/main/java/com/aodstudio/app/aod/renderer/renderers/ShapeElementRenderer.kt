package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for SHAPE, LINE, RING, and PROGRESS elements.
 * Supports CIRCLE, RECTANGLE, LINE, RING, and ARC geometries.
 */
class ShapeElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val shapeType = element.properties[AODElement.PROP_SHAPE_TYPE] ?: "CIRCLE"

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            strokeWidth = element.style.strokeWidth * context.scaleFactorX
            style = if (element.style.fill) Paint.Style.FILL else Paint.Style.STROKE
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val drawX = element.x * context.scaleFactorX
        val drawY = element.y * context.scaleFactorY
        val w = element.width * context.scaleFactorX
        val h = element.height * context.scaleFactorY

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
                canvas.drawRoundRect(rect, element.style.cornerRadius, element.style.cornerRadius, paint)
            }
            else -> {
                val rect = RectF(drawX - w / 2f, drawY - h / 2f, drawX + w / 2f, drawY + h / 2f)
                canvas.drawRect(rect, paint)
            }
        }
    }
}
