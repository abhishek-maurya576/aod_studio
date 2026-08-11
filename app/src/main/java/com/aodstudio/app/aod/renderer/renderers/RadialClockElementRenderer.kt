package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.core.util.formatTime
import com.aodstudio.app.domain.model.AODElement
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderer for Radial and Orbit Clock styles using uniform scale and coordinate transformation.
 */
class RadialClockElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val radius = (element.width / 2f) * context.scaleFactor

        val calendar = Calendar.getInstance().apply { time = context.date }
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        // 1. Draw Outer Orbit Track
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            style = Paint.Style.STROKE
            strokeWidth = element.style.strokeWidth * context.scaleFactor * 0.5f
            alpha = (element.opacity * 100).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(drawX, drawY, radius, trackPaint)

        // 2. Draw Hour Orbit Dot
        val hourAngleRad = Math.toRadians(((hours + minutes / 60f) * 30f - 90).toDouble()).toFloat()
        val hourDotX = drawX + cos(hourAngleRad) * radius
        val hourDotY = drawY + sin(hourAngleRad) * radius

        val hourDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.accentColor)
            style = Paint.Style.FILL
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(hourDotX, hourDotY, 8f * context.scaleFactor, hourDotPaint)

        // 3. Draw Minute Orbit Arc
        val minuteSweep = (minutes / 60f) * 360f
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.accentColor)
            style = Paint.Style.STROKE
            strokeWidth = element.style.strokeWidth * context.scaleFactor
            strokeCap = Paint.Cap.ROUND
            alpha = (element.opacity * 220).toInt().coerceIn(0, 255)
        }
        val oval = RectF(drawX - radius * 0.8f, drawY - radius * 0.8f, drawX + radius * 0.8f, drawY + radius * 0.8f)
        canvas.drawArc(oval, -90f, minuteSweep, false, arcPaint)

        // 4. Center Digital Time Display
        val timeString = formatTime("HH:mm", context.date)
        val textPaint = RendererUtils.createTextPaint(element.style, context.scaleFactor).apply {
            textSize = element.style.fontSize * 0.5f * context.scaleFactor
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }
        canvas.drawText("○ $timeString", drawX, drawY + textPaint.textSize * 0.35f, textPaint)
    }
}
