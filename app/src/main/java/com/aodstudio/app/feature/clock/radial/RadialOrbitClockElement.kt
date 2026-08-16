package com.aodstudio.app.feature.clock.radial

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Produces real-time state for RadialOrbitClockElement updating every display frame (60fps/120fps)
 * for fluid continuous rotation of both minute and second dials.
 */
@Composable
fun rememberRadialOrbitTimeState(is24Hour: Boolean = true): RadialOrbitTimeState {
    var timeState by remember {
        mutableStateOf(RadialOrbitTimeState.fromCalendar(Calendar.getInstance(), is24Hour))
    }

    LaunchedEffect(is24Hour) {
        val calendar = Calendar.getInstance()
        while (true) {
            withFrameMillis { frameTimeMillis ->
                calendar.timeInMillis = System.currentTimeMillis()
                timeState = RadialOrbitTimeState.fromCalendar(calendar, is24Hour)
            }
        }
    }

    return timeState
}

/**
 * RadialOrbitClockElement — Premium Concentric Orbit Chronograph Clock Composable.
 *
 * Meticulously replicates the Dual-Orbit Chronograph AOD layout:
 * 1. Left: Large white sans-serif Hour display.
 * 2. Center: Dual-Chamber Highlight Capsule (stadium pill shape with thin white stroke).
 * 3. Inner Orbit Dial: Rotating Minutes Dial that continuously rotates based on continuous minute progress,
 *    framing the active minute in the left chamber of the capsule.
 * 4. Outer Orbit Dial: Rotating Seconds Dial with 60 precision tick marks and numbers (00, 05, 10... 55)
 *    continuously rotating with milliseconds, framing the active second in the right chamber.
 * 5. Right: Vertical uppercase Date ("16 AUG 2026") and Day ("SUNDAY") stack.
 *
 * Background is strictly pure AMOLED black (#000000).
 */
@Composable
fun RadialOrbitClockElement(
    timeState: RadialOrbitTimeState = rememberRadialOrbitTimeState(),
    tokens: RadialOrbitTokens = RadialOrbitTokens.Default,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Convert tokens to pixel metrics
    val hourFontSizePx = with(density) { tokens.hourFontSize.toPx() }
    val minuteFontSizePx = with(density) { tokens.minuteFontSize.toPx() }
    val dialFontSizePx = with(density) { tokens.dialFontSize.toPx() }
    val dateFontSizePx = with(density) { tokens.dateFontSize.toPx() }
    val dayFontSizePx = with(density) { tokens.dayFontSize.toPx() }
    val dateLetterSpacingPx = with(density) { tokens.dateLetterSpacing.toPx() }
    val dayLetterSpacingPx = with(density) { tokens.dayLetterSpacing.toPx() }

    val capsuleHeightPx = with(density) { tokens.capsuleHeight.toPx() }
    val capsuleStrokeWidthPx = with(density) { tokens.capsuleStrokeWidth.toPx() }
    val capsuleCornerRadiusPx = capsuleHeightPx / 2f

    val outerDialRadiusPx = with(density) { tokens.outerDialRadius.toPx() }
    val innerDialRadiusPx = with(density) { tokens.innerDialRadius.toPx() }
    val majorTickLengthPx = with(density) { tokens.majorTickLength.toPx() }
    val minorTickLengthPx = with(density) { tokens.minorTickLength.toPx() }
    val majorTickStrokeWidthPx = with(density) { tokens.majorTickStrokeWidth.toPx() }
    val minorTickStrokeWidthPx = with(density) { tokens.minorTickStrokeWidth.toPx() }
    val dialTextRadiusOffsetPx = with(density) { tokens.dialTextRadiusOffset.toPx() }

    val hourToCapsuleGapPx = with(density) { tokens.hourToCapsuleGap.toPx() }
    val capsuleToDateGapPx = with(density) { tokens.capsuleToDateGap.toPx() }

    // Pre-create native Paint objects for optimal drawing performance
    val hourPaint = remember(tokens.hourColor, hourFontSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.hourColor.toArgb()
            textSize = hourFontSizePx
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
    }

    val activeMinutePaint = remember(tokens.minuteColor, minuteFontSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.minuteColor.toArgb()
            textSize = minuteFontSizePx
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    }

    val minuteDialTextPaint = remember(tokens.dialTextColor, dialFontSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.dialTextColor.toArgb()
            textSize = dialFontSizePx * 1.15f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
    }

    val dialTextPaint = remember(tokens.dialTextColor, dialFontSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.dialTextColor.toArgb()
            textSize = dialFontSizePx
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
    }

    val datePaint = remember(tokens.dateTextColor, dateFontSizePx, dateLetterSpacingPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.dateTextColor.toArgb()
            textSize = dateFontSizePx
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            letterSpacing = dateLetterSpacingPx / dateFontSizePx
        }
    }

    val dayPaint = remember(tokens.dayTextColor, dayFontSizePx, dayLetterSpacingPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.dayTextColor.toArgb()
            textSize = dayFontSizePx
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            letterSpacing = dayLetterSpacingPx / dayFontSizePx
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.backgroundColor)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f

            // ─── Geometric Anchors for Concentric Dials & Capsule ────────
            // Center of both concentric dials
            val dialCenterX = canvasWidth * 0.28f
            val dialCenterY = centerY

            // Chamber center X coordinates (at angle = 0 / 3 o'clock)
            val leftChamberCenterX = dialCenterX + innerDialRadiusPx
            val rightChamberCenterX = dialCenterX + outerDialRadiusPx

            // Capsule dimensions precisely enclosing both chambers
            val capsuleLeft = leftChamberCenterX - (capsuleHeightPx / 2f)
            val capsuleRight = rightChamberCenterX + (capsuleHeightPx / 2f)
            val capsuleTop = centerY - (capsuleHeightPx / 2f)
            val capsuleWidth = capsuleRight - capsuleLeft

            // Hour text anchor (sits to the left of the capsule)
            val hourRightX = capsuleLeft - hourToCapsuleGapPx

            // Date & Day stack anchor (starts to the right of the capsule)
            val dateStartX = capsuleRight + capsuleToDateGapPx

            // ─── 1. Draw Rotating Outer Seconds Dial ──────────────────────
            drawRotatingSecondsDial(
                dialCenterX = dialCenterX,
                dialCenterY = dialCenterY,
                dialRadius = outerDialRadiusPx,
                continuousSecond = timeState.continuousSecond,
                majorTickLength = majorTickLengthPx,
                minorTickLength = minorTickLengthPx,
                majorTickStrokeWidth = majorTickStrokeWidthPx,
                minorTickStrokeWidth = minorTickStrokeWidthPx,
                dialTextRadiusOffset = dialTextRadiusOffsetPx,
                majorTickColor = tokens.majorTickColor,
                minorTickColor = tokens.minorTickColor,
                dialTextPaint = dialTextPaint
            )

            // ─── 2. Draw Rotating Inner Minutes Dial ──────────────────────
            drawRotatingMinutesDial(
                dialCenterX = dialCenterX,
                dialCenterY = dialCenterY,
                innerDialRadius = innerDialRadiusPx,
                continuousMinute = timeState.continuousMinute,
                minorTickLength = minorTickLengthPx,
                minorTickStrokeWidth = minorTickStrokeWidthPx,
                minorTickColor = tokens.minorTickColor,
                minuteDialTextPaint = minuteDialTextPaint,
                activeMinutePaint = activeMinutePaint
            )

            // ─── 3. Draw Hour Typography ─────────────────────────────────
            drawIntoCanvas { canvas ->
                val hourStr = timeState.formattedHour
                val fontMetrics = hourPaint.fontMetrics
                val textBaselineY = centerY - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.nativeCanvas.drawText(hourStr, hourRightX, textBaselineY, hourPaint)
            }

            // ─── 4. Draw Highlight Capsule ───────────────────────────────
            drawRoundRect(
                color = tokens.capsuleStrokeColor,
                topLeft = Offset(capsuleLeft, capsuleTop),
                size = Size(capsuleWidth, capsuleHeightPx),
                cornerRadius = CornerRadius(capsuleCornerRadiusPx, capsuleCornerRadiusPx),
                style = Stroke(width = capsuleStrokeWidthPx)
            )

            // ─── 5. Draw Right Side Date & Day Stack ─────────────────────
            drawIntoCanvas { canvas ->
                val dateStr = timeState.dateFormatted
                val dayStr = timeState.dayOfWeek

                val dateMetrics = datePaint.fontMetrics
                val dayMetrics = dayPaint.fontMetrics

                val lineSpacing = 6.dp.toPx()
                val totalTextHeight = (dateMetrics.descent - dateMetrics.ascent) +
                        (dayMetrics.descent - dayMetrics.ascent) + lineSpacing

                val dateBaselineY = centerY - (totalTextHeight / 2f) - dateMetrics.ascent
                val dayBaselineY = dateBaselineY + dateMetrics.descent + lineSpacing - dayMetrics.ascent

                canvas.nativeCanvas.drawText(dateStr, dateStartX, dateBaselineY, datePaint)
                canvas.nativeCanvas.drawText(dayStr, dateStartX, dayBaselineY, dayPaint)
            }
        }
    }
}

/**
 * Draws the inner rotating minutes chronograph dial with continuous rotation.
 */
private fun DrawScope.drawRotatingMinutesDial(
    dialCenterX: Float,
    dialCenterY: Float,
    innerDialRadius: Float,
    continuousMinute: Float,
    minorTickLength: Float,
    minorTickStrokeWidth: Float,
    minorTickColor: Color,
    minuteDialTextPaint: Paint,
    activeMinutePaint: Paint
) {
    // Rotation mapping: When continuousMinute = m, minute m is at angle 0° (inside the left chamber of capsule)
    val rotationOffsetDeg = continuousMinute * 6f

    // Draw minute ticks and 5-minute numeric labels around the inner orbit
    for (m in 0 until 60) {
        val isMajor = (m % 5 == 0)
        val baseAngleDeg = -(m * 6f)
        val currentAngleDeg = baseAngleDeg + rotationOffsetDeg
        val angleRad = Math.toRadians(currentAngleDeg.toDouble())

        val cosVal = cos(angleRad).toFloat()
        val sinVal = sin(angleRad).toFloat()

        val tickOuterX = dialCenterX + (innerDialRadius * cosVal)
        val tickOuterY = dialCenterY + (innerDialRadius * sinVal)

        // Draw minor tick lines between numbers
        if (!isMajor) {
            val tickInnerX = dialCenterX + ((innerDialRadius - minorTickLength) * cosVal)
            val tickInnerY = dialCenterY + ((innerDialRadius - minorTickLength) * sinVal)
            drawLine(
                color = minorTickColor,
                start = Offset(tickInnerX, tickInnerY),
                end = Offset(tickOuterX, tickOuterY),
                strokeWidth = minorTickStrokeWidth
            )
        }

        // Draw 5-minute interval labels (00, 05, 10, 15... 55)
        if (isMajor) {
            val label = String.format(Locale.getDefault(), "%02d", m)
            val isFramedInCapsule = abs(currentAngleDeg % 360f) < 4f || abs(currentAngleDeg % 360f) > 356f

            drawIntoCanvas { canvas ->
                val paintToUse = if (isFramedInCapsule) activeMinutePaint else minuteDialTextPaint
                val fontMetrics = paintToUse.fontMetrics
                val textBaselineY = tickOuterY - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.nativeCanvas.drawText(label, tickOuterX, textBaselineY, paintToUse)
            }
        }
    }

    // Draw the active continuous minute number directly inside the capsule left chamber if not already drawn by a major mark
    val nearestMinute = Math.round(continuousMinute) % 60
    if (nearestMinute % 5 != 0) {
        val angleToEast = (continuousMinute - nearestMinute) * 6f
        val angleRad = Math.toRadians(angleToEast.toDouble())
        val textX = dialCenterX + (innerDialRadius * cos(angleRad).toFloat())
        val textY = dialCenterY + (innerDialRadius * sin(angleRad).toFloat())

        val label = String.format(Locale.getDefault(), "%02d", nearestMinute)
        drawIntoCanvas { canvas ->
            val fontMetrics = activeMinutePaint.fontMetrics
            val textBaselineY = textY - (fontMetrics.descent + fontMetrics.ascent) / 2f
            canvas.nativeCanvas.drawText(label, textX, textBaselineY, activeMinutePaint)
        }
    }
}

/**
 * Draws the outer 60-tick seconds chronograph dial and numeric labels rotating with high-frequency time state.
 */
private fun DrawScope.drawRotatingSecondsDial(
    dialCenterX: Float,
    dialCenterY: Float,
    dialRadius: Float,
    continuousSecond: Float,
    majorTickLength: Float,
    minorTickLength: Float,
    majorTickStrokeWidth: Float,
    minorTickStrokeWidth: Float,
    dialTextRadiusOffset: Float,
    majorTickColor: Color,
    minorTickColor: Color,
    dialTextPaint: Paint
) {
    val rotationOffsetDeg = continuousSecond * 6f

    for (i in 0 until 60) {
        val isMajor = (i % 5 == 0)
        val baseAngleDeg = -(i * 6f)
        val currentAngleDeg = baseAngleDeg + rotationOffsetDeg
        val angleRad = Math.toRadians(currentAngleDeg.toDouble())

        val cosVal = cos(angleRad).toFloat()
        val sinVal = sin(angleRad).toFloat()

        val tickOuterX = dialCenterX + (dialRadius * cosVal)
        val tickOuterY = dialCenterY + (dialRadius * sinVal)

        val tickLength = if (isMajor) majorTickLength else minorTickLength
        val tickInnerX = dialCenterX + ((dialRadius - tickLength) * cosVal)
        val tickInnerY = dialCenterY + ((dialRadius - tickLength) * sinVal)

        val strokeWidth = if (isMajor) majorTickStrokeWidth else minorTickStrokeWidth
        val tickColor = if (isMajor) majorTickColor else minorTickColor

        // Draw radial tick line
        drawLine(
            color = tickColor,
            start = Offset(tickInnerX, tickInnerY),
            end = Offset(tickOuterX, tickOuterY),
            strokeWidth = strokeWidth
        )

        // Draw 5-second interval numeric labels (00, 05, 10... 55)
        if (isMajor) {
            val textRadius = dialRadius - dialTextRadiusOffset
            val textX = dialCenterX + (textRadius * cosVal)
            val textY = dialCenterY + (textRadius * sinVal)

            val label = String.format(Locale.getDefault(), "%02d", i)

            drawIntoCanvas { canvas ->
                val fontMetrics = dialTextPaint.fontMetrics
                val textBaselineY = textY - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.nativeCanvas.drawText(label, textX, textBaselineY, dialTextPaint)
            }
        }
    }
}
