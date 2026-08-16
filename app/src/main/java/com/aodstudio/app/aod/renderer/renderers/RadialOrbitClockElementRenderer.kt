package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Native Canvas ElementRenderer for the Radial Orbit Chronograph AOD template.
 * Renders the hour, capsule with concentric rotating minute and second dials, and date/day stack.
 */
class RadialOrbitClockElementRenderer : ElementRenderer {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val scale = context.scaleFactor

        val calendar = Calendar.getInstance().apply { time = context.date }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millisecond = calendar.get(Calendar.MILLISECOND)

        val continuousSecond = second + (millisecond / 1000f)
        val continuousMinute = minute + (continuousSecond / 60f)

        val hourStr = String.format(Locale.getDefault(), "%02d", hour)
        val dateStr = dateFormat.format(context.date).uppercase(Locale.getDefault())
        val dayStr = dayFormat.format(context.date).uppercase(Locale.getDefault())

        // ─── Proportional Concentric Geometry ───────────────────────────
        val capsuleHeight = 92f * scale
        val capsuleCornerRadius = capsuleHeight / 2f
        val innerDialRadius = 230f * scale
        val outerDialRadius = 370f * scale

        val dialCenterX = drawX - (outerDialRadius * 0.45f)
        val dialCenterY = drawY

        val leftChamberCenterX = dialCenterX + innerDialRadius
        val rightChamberCenterX = dialCenterX + outerDialRadius

        val capsuleLeft = leftChamberCenterX - (capsuleHeight / 2f)
        val capsuleRight = rightChamberCenterX + (capsuleHeight / 2f)
        val capsuleTop = drawY - (capsuleHeight / 2f)
        val capsuleBottom = drawY + (capsuleHeight / 2f)

        val hourRightX = capsuleLeft - (32f * scale)
        val dateStartX = capsuleRight + (48f * scale)

        // ─── 1. Draw Rotating Outer Seconds Dial ────────────────────────
        val majorTickLength = 24f * scale
        val minorTickLength = 12f * scale
        val dialTextOffset = 42f * scale

        val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
            alpha = (element.opacity * 220).toInt().coerceIn(0, 255)
        }

        val minorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
            alpha = (element.opacity * 85).toInt().coerceIn(0, 255)
        }

        val dialTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            textSize = 22f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            alpha = (element.opacity * 200).toInt().coerceIn(0, 255)
        }

        val rotationOffsetSecDeg = continuousSecond * 6f

        for (i in 0 until 60) {
            val isMajor = (i % 5 == 0)
            val currentAngleDeg = -(i * 6f) + rotationOffsetSecDeg
            val angleRad = Math.toRadians(currentAngleDeg.toDouble())

            val cosVal = cos(angleRad).toFloat()
            val sinVal = sin(angleRad).toFloat()

            val tickOuterX = dialCenterX + (outerDialRadius * cosVal)
            val tickOuterY = dialCenterY + (outerDialRadius * sinVal)

            val tickLength = if (isMajor) majorTickLength else minorTickLength
            val tickInnerX = dialCenterX + ((outerDialRadius - tickLength) * cosVal)
            val tickInnerY = dialCenterY + ((outerDialRadius - tickLength) * sinVal)

            val tickPaint = if (isMajor) majorTickPaint else minorTickPaint
            canvas.drawLine(tickInnerX, tickInnerY, tickOuterX, tickOuterY, tickPaint)

            if (isMajor) {
                val textRadius = outerDialRadius - dialTextOffset
                val textX = dialCenterX + (textRadius * cosVal)
                val textY = dialCenterY + (textRadius * sinVal)

                val label = String.format(Locale.getDefault(), "%02d", i)
                val fontMetrics = dialTextPaint.fontMetrics
                val textBaselineY = textY - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.drawText(label, textX, textBaselineY, dialTextPaint)
            }
        }

        // ─── 2. Draw Rotating Inner Minutes Dial ────────────────────────
        val activeMinutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            textSize = 48f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val minuteDialTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            textSize = 24f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            alpha = (element.opacity * 170).toInt().coerceIn(0, 255)
        }

        val rotationOffsetMinDeg = continuousMinute * 6f

        for (m in 0 until 60) {
            val isMajor = (m % 5 == 0)
            val currentAngleDeg = -(m * 6f) + rotationOffsetMinDeg
            val angleRad = Math.toRadians(currentAngleDeg.toDouble())

            val cosVal = cos(angleRad).toFloat()
            val sinVal = sin(angleRad).toFloat()

            val tickOuterX = dialCenterX + (innerDialRadius * cosVal)
            val tickOuterY = dialCenterY + (innerDialRadius * sinVal)

            if (!isMajor) {
                val tickInnerX = dialCenterX + ((innerDialRadius - (10f * scale)) * cosVal)
                val tickInnerY = dialCenterY + ((innerDialRadius - (10f * scale)) * sinVal)
                canvas.drawLine(tickInnerX, tickInnerY, tickOuterX, tickOuterY, minorTickPaint)
            }

            if (isMajor) {
                val label = String.format(Locale.getDefault(), "%02d", m)
                val isFramedInCapsule = abs(currentAngleDeg % 360f) < 4f || abs(currentAngleDeg % 360f) > 356f

                val paintToUse = if (isFramedInCapsule) activeMinutePaint else minuteDialTextPaint
                val fontMetrics = paintToUse.fontMetrics
                val textBaselineY = tickOuterY - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.drawText(label, tickOuterX, textBaselineY, paintToUse)
            }
        }

        // Draw the active non-multiple minute directly framed in the capsule
        val nearestMinute = Math.round(continuousMinute) % 60
        if (nearestMinute % 5 != 0) {
            val angleToEast = (continuousMinute - nearestMinute) * 6f
            val angleRad = Math.toRadians(angleToEast.toDouble())
            val textX = dialCenterX + (innerDialRadius * cos(angleRad).toFloat())
            val textY = dialCenterY + (innerDialRadius * sin(angleRad).toFloat())

            val label = String.format(Locale.getDefault(), "%02d", nearestMinute)
            val fontMetrics = activeMinutePaint.fontMetrics
            val textBaselineY = textY - (fontMetrics.descent + fontMetrics.ascent) / 2f
            canvas.drawText(label, textX, textBaselineY, activeMinutePaint)
        }

        // ─── 3. Draw Hour Typography ────────────────────────────────────
        val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            textSize = 175f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }
        val hourMetrics = hourPaint.fontMetrics
        val hourBaselineY = drawY - (hourMetrics.descent + hourMetrics.ascent) / 2f
        canvas.drawText(hourStr, hourRightX, hourBaselineY, hourPaint)

        // ─── 4. Draw Highlight Capsule ──────────────────────────────────
        val capsulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.accentColor, 0xFFFFFFFF.toInt())
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
            alpha = (element.opacity * 240).toInt().coerceIn(0, 255)
        }
        val capsuleRect = RectF(capsuleLeft, capsuleTop, capsuleRight, capsuleBottom)
        canvas.drawRoundRect(capsuleRect, capsuleCornerRadius, capsuleCornerRadius, capsulePaint)

        // ─── 5. Draw Right Side Date & Day Stack ────────────────────────
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xDDFFFFFF.toInt())
            textSize = 26f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.15f
            alpha = (element.opacity * 220).toInt().coerceIn(0, 255)
        }

        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color, 0xFFFFFFFF.toInt())
            textSize = 28f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.18f
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val dateMetrics = datePaint.fontMetrics
        val dayMetrics = dayPaint.fontMetrics
        val lineGap = 12f * scale
        val totalStackHeight = (dateMetrics.descent - dateMetrics.ascent) +
                (dayMetrics.descent - dayMetrics.ascent) + lineGap

        val dateBaselineY = drawY - (totalStackHeight / 2f) - dateMetrics.ascent
        val dayBaselineY = dateBaselineY + dateMetrics.descent + lineGap - dayMetrics.ascent

        canvas.drawText(dateStr, dateStartX, dateBaselineY, datePaint)
        canvas.drawText(dayStr, dateStartX, dayBaselineY, dayPaint)
    }
}
