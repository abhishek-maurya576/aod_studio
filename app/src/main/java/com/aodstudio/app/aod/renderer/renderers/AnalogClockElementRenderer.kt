package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderer for Analog Clock elements using uniform scale and coordinate transformation.
 */
class AnalogClockElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val radius = (element.width / 2f) * context.scaleFactor

        val calendar = Calendar.getInstance().apply { time = context.date }
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        val showSeconds = element.properties[AODElement.PROP_SHOW_SECONDS] == "true"
        val showMarkers = element.properties["showMarkers"] != "false"
        val markerType = element.properties["markerType"] ?: "TICKS"

        // 1. Draw Markers / Ticks
        if (showMarkers) {
            drawClockMarkers(canvas, drawX, drawY, radius, markerType, element, context)
        }

        // 2. Draw Hour Hand
        val hourAngle = (hours + minutes / 60f) * 30f
        drawHand(canvas, drawX, drawY, radius * 0.5f, hourAngle, element.style.color, element.style.strokeWidth * 1.5f, context)

        // 3. Draw Minute Hand
        val minuteAngle = (minutes + seconds / 60f) * 6f
        drawHand(canvas, drawX, drawY, radius * 0.75f, minuteAngle, element.style.accentColor, element.style.strokeWidth, context)

        // 4. Draw Second Hand (if enabled)
        if (showSeconds) {
            val secondAngle = seconds * 6f
            drawHand(canvas, drawX, drawY, radius * 0.85f, secondAngle, "#E87C7C", 2f, context)
        }

        // 5. Draw Center Pin
        val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.accentColor)
            style = Paint.Style.FILL
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(drawX, drawY, 6f * context.scaleFactor, pinPaint)
    }

    private fun drawClockMarkers(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        type: String,
        element: AODElement,
        context: RenderContext
    ) {
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            style = Paint.Style.STROKE
            strokeWidth = element.style.strokeWidth * context.scaleFactor * 0.5f
            alpha = (element.opacity * 200).toInt().coerceIn(0, 255)
        }

        for (i in 0 until 12) {
            val angleRad = Math.toRadians((i * 30 - 90).toDouble()).toFloat()
            val startDist = radius * 0.85f
            val endDist = radius * 0.95f

            val startX = centerX + cos(angleRad) * startDist
            val startY = centerY + sin(angleRad) * startDist
            val endX = centerX + cos(angleRad) * endDist
            val endY = centerY + sin(angleRad) * endDist

            if (type.uppercase() == "DOTS") {
                markerPaint.style = Paint.Style.FILL
                canvas.drawCircle(endX, endY, 3f * context.scaleFactor, markerPaint)
            } else {
                canvas.drawLine(startX, startY, endX, endY, markerPaint)
            }
        }
    }

    private fun drawHand(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        length: Float,
        angleDeg: Float,
        colorHex: String,
        thickness: Float,
        context: RenderContext
    ) {
        val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(colorHex)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = thickness * context.scaleFactor
        }

        val angleRad = Math.toRadians((angleDeg - 90).toDouble()).toFloat()
        val endX = centerX + cos(angleRad) * length
        val endY = centerY + sin(angleRad) * length

        canvas.drawLine(centerX, centerY, endX, endY, handPaint)
    }
}
