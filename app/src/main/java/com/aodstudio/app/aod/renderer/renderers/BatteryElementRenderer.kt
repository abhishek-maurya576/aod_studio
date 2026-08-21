package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * High-definition Canvas renderer for BATTERY type elements.
 * Supports 5 distinct battery presentation modes:
 *  1. PERCENTAGE: Clean typographical percentage text with vector charging indicator.
 *  2. ICON: Horizontal battery capsule with terminal nub, dynamic fill, and charging bolt.
 *  3. ICON_PERCENTAGE: Capsule icon paired side-by-side with percentage text.
 *  4. RING: Circular gauge with background track, round-cap progress sweep, and centered readout.
 *  5. BAR: Minimalist progress pill with background track and proportional fill.
 */
class BatteryElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val styleType = element.properties[AODElement.PROP_BATTERY_STYLE] ?: "PERCENTAGE"

        when (styleType.uppercase()) {
            "ICON" -> renderBatteryIconOnly(canvas, element, context)
            "ICON_PERCENTAGE", "ICON_AND_PERCENTAGE", "PERCENTAGE_ICON" -> renderBatteryIconWithText(canvas, element, context)
            "RING", "CIRCULAR", "GAUGE" -> renderBatteryRing(canvas, element, context)
            "BAR", "PROGRESS" -> renderBatteryBar(canvas, element, context)
            else -> renderBatteryPercentageText(canvas, element, context)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mode 1: PERCENTAGE (Text Only)
    // ──────────────────────────────────────────────────────────────────────────

    private fun renderBatteryPercentageText(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        val textPaint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        val baseAlpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        textPaint.alpha = baseAlpha

        val textContent = "${context.batteryPercentage}%"

        if (context.isCharging) {
            val textWidth = textPaint.measureText(textContent)
            val fontMetrics = textPaint.fontMetrics
            val textHeight = fontMetrics.descent - fontMetrics.ascent
            val boltWidth = textHeight * 0.42f
            val boltHeight = textHeight * 0.85f
            val gap = 6f * context.scaleFactor
            val totalWidth = boltWidth + gap + textWidth

            val startX = drawX - totalWidth / 2f
            val boltCenterX = startX + boltWidth / 2f
            val boltCenterY = drawY + (fontMetrics.ascent + fontMetrics.descent) / 2f

            val boltColor = RendererUtils.parseColor(element.style.accentColor, RendererUtils.parseColor(element.style.color, Color.WHITE))
            drawChargingBolt(canvas, boltCenterX, boltCenterY, boltWidth, boltHeight, boltColor, baseAlpha)

            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(textContent, startX + boltWidth + gap, drawY, textPaint)
        } else {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(textContent, drawX, drawY, textPaint)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mode 2: ICON (Capsule Only)
    // ──────────────────────────────────────────────────────────────────────────

    private fun renderBatteryIconOnly(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        val iconHeight = (element.style.fontSize * 1.05f + 12f) * context.scaleFactor
        val iconWidth = iconHeight * 2.1f

        drawBatteryCapsule(
            canvas = canvas,
            centerX = drawX,
            centerY = drawY,
            width = iconWidth,
            height = iconHeight,
            percentage = context.batteryPercentage,
            isCharging = context.isCharging,
            element = element,
            scaleFactor = context.scaleFactor
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mode 3: ICON_PERCENTAGE (Side-by-side Capsule + Text)
    // ──────────────────────────────────────────────────────────────────────────

    private fun renderBatteryIconWithText(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        val textPaint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        val baseAlpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        textPaint.alpha = baseAlpha
        textPaint.textAlign = Paint.Align.LEFT

        val textContent = "${context.batteryPercentage}%"
        val textWidth = textPaint.measureText(textContent)
        val fontMetrics = textPaint.fontMetrics

        val iconHeight = (element.style.fontSize * 0.95f + 8f) * context.scaleFactor
        val iconWidth = iconHeight * 2.1f
        val capWidth = iconHeight * 0.16f
        val strokeWidth = (if (element.style.strokeWidth > 0f) element.style.strokeWidth else 2.2f) * context.scaleFactor
        val totalIconWidth = iconWidth + capWidth + strokeWidth
        val gap = 10f * context.scaleFactor
        val totalCombinedWidth = totalIconWidth + gap + textWidth

        val startX = drawX - totalCombinedWidth / 2f
        val iconCenterX = startX + totalIconWidth / 2f
        val textCenterY = drawY + (fontMetrics.ascent + fontMetrics.descent) / 2f

        // Draw battery capsule on left
        drawBatteryCapsule(
            canvas = canvas,
            centerX = iconCenterX,
            centerY = textCenterY,
            width = iconWidth,
            height = iconHeight,
            percentage = context.batteryPercentage,
            isCharging = context.isCharging,
            element = element,
            scaleFactor = context.scaleFactor
        )

        // Draw percentage text on right
        val textStartX = startX + totalIconWidth + gap
        canvas.drawText(textContent, textStartX, drawY, textPaint)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mode 4: RING (Circular Gauge)
    // ──────────────────────────────────────────────────────────────────────────

    private fun renderBatteryRing(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        val baseColor = RendererUtils.parseColor(element.style.color, Color.WHITE)
        val accentColor = RendererUtils.parseColor(element.style.accentColor, baseColor)
        val baseAlpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        // Proportional stroke width and radius calculations
        val strokeWidth = (if (element.style.strokeWidth > 0f) element.style.strokeWidth else 4.5f) * context.scaleFactor
        val ringRadius = ((element.style.fontSize * 1.35f + 14f) * context.scaleFactor).coerceIn(16f * context.scaleFactor, 120f * context.scaleFactor)

        // 1. Full 360° Background Track Ring
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            alpha = (baseAlpha * 0.22f).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(drawX, drawY, ringRadius, trackPaint)

        // 2. Active Progress Sweep Arc
        val progressColor = if (context.batteryPercentage <= 15 && !context.isCharging) {
            Color.parseColor("#FF5252")
        } else {
            accentColor
        }

        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = progressColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
            alpha = baseAlpha
        }

        val oval = RectF(drawX - ringRadius, drawY - ringRadius, drawX + ringRadius, drawY + ringRadius)
        val sweepAngle = (context.batteryPercentage.coerceIn(0, 100) / 100f) * 360f

        if (sweepAngle > 0f) {
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
        }

        // 3. Center Content (Percentage number or charging bolt)
        if (context.isCharging) {
            val boltSize = ringRadius * 0.9f
            drawChargingBolt(canvas, drawX, drawY, boltSize * 0.52f, boltSize, progressColor, baseAlpha)
        } else {
            val textPaint = RendererUtils.createTextPaint(element.style, context.scaleFactor).apply {
                textSize = (ringRadius * 0.68f).coerceAtLeast(10f * context.scaleFactor)
                textAlign = Paint.Align.CENTER
                alpha = baseAlpha
            }
            val fontMetrics = textPaint.fontMetrics
            val baselineY = drawY - (fontMetrics.ascent + fontMetrics.descent) / 2f
            canvas.drawText("${context.batteryPercentage}", drawX, baselineY, textPaint)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mode 5: BAR (Progress Bar Pill)
    // ──────────────────────────────────────────────────────────────────────────

    private fun renderBatteryBar(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        val baseColor = RendererUtils.parseColor(element.style.color, Color.WHITE)
        val accentColor = RendererUtils.parseColor(element.style.accentColor, baseColor)
        val baseAlpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val barW = (element.width * 0.85f).coerceAtLeast(80f) * context.scaleFactor
        val barH = (if (element.style.strokeWidth > 0f) element.style.strokeWidth * 2.8f else element.style.fontSize * 0.65f).coerceIn(6f * context.scaleFactor, 28f * context.scaleFactor)
        val cornerRadius = barH / 2f

        // 1. Background Bar Track
        val trackRect = RectF(drawX - barW / 2f, drawY - barH / 2f, drawX + barW / 2f, drawY + barH / 2f)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            style = Paint.Style.FILL
            alpha = (baseAlpha * 0.22f).toInt().coerceIn(0, 255)
        }
        canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, trackPaint)

        // 2. Active Level Fill
        val fillPercentage = context.batteryPercentage.coerceIn(0, 100) / 100f
        val fillW = (barW * fillPercentage).coerceAtLeast(0f)

        if (fillW > 0f) {
            val fillRect = RectF(drawX - barW / 2f, drawY - barH / 2f, (drawX - barW / 2f) + fillW, drawY + barH / 2f)
            val fillColor = if (context.batteryPercentage <= 15 && !context.isCharging) {
                Color.parseColor("#FF5252")
            } else {
                accentColor
            }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = fillColor
                style = Paint.Style.FILL
                alpha = baseAlpha
            }
            canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint)
        }

        // 3. Optional charging indicator
        if (context.isCharging) {
            val boltSize = barH * 1.4f
            val boltX = drawX + barW / 2f + boltSize * 0.6f
            drawChargingBolt(canvas, boltX, drawY, boltSize * 0.5f, boltSize, accentColor, baseAlpha)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Shared Component: Precision Battery Capsule Drawer
    // ──────────────────────────────────────────────────────────────────────────

    private fun drawBatteryCapsule(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        percentage: Int,
        isCharging: Boolean,
        element: AODElement,
        scaleFactor: Float
    ) {
        val baseColor = RendererUtils.parseColor(element.style.color, Color.WHITE)
        val accentColor = RendererUtils.parseColor(element.style.accentColor, baseColor)
        val baseAlpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val strokeWidth = (if (element.style.strokeWidth > 0f) element.style.strokeWidth else 2.2f) * scaleFactor
        val capWidth = height * 0.16f
        val capHeight = height * 0.42f
        val totalSpan = width + capWidth + strokeWidth

        val left = centerX - totalSpan / 2f + strokeWidth / 2f
        val top = centerY - height / 2f
        val right = left + width
        val bottom = top + height
        val cornerRadius = height * 0.26f

        // 1. Outer Bezel / Shell
        val shellRect = RectF(left, top, right, bottom)
        val shellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            alpha = baseAlpha
        }
        canvas.drawRoundRect(shellRect, cornerRadius, cornerRadius, shellPaint)

        // 2. Positive Terminal Nub on Right
        val capLeft = right + strokeWidth / 2f
        val capRect = RectF(capLeft, centerY - capHeight / 2f, capLeft + capWidth, centerY + capHeight / 2f)
        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            style = Paint.Style.FILL
            alpha = (baseAlpha * 0.85f).toInt().coerceIn(0, 255)
        }
        canvas.drawRoundRect(capRect, capWidth * 0.45f, capWidth * 0.45f, capPaint)

        // 3. Inner Level Fill
        val pad = strokeWidth + 2.5f * scaleFactor
        val innerLeft = left + pad
        val innerTop = top + pad
        val innerRight = right - pad
        val innerBottom = bottom - pad
        val maxInnerW = (innerRight - innerLeft).coerceAtLeast(0f)
        val innerH = (innerBottom - innerTop).coerceAtLeast(0f)

        val fillFraction = percentage.coerceIn(0, 100) / 100f
        val fillW = maxInnerW * fillFraction

        if (fillW > 0f && innerH > 0f) {
            val fillRect = RectF(innerLeft, innerTop, innerLeft + fillW, innerBottom)
            val fillColor = if (percentage <= 15 && !isCharging) {
                Color.parseColor("#FF5252")
            } else {
                accentColor
            }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = fillColor
                style = Paint.Style.FILL
                alpha = baseAlpha
            }
            val fillCornerRadius = (cornerRadius - pad).coerceAtLeast(1.5f * scaleFactor)
            canvas.drawRoundRect(fillRect, fillCornerRadius, fillCornerRadius, fillPaint)
        }

        // 4. Centered Charging Bolt Overlay
        if (isCharging) {
            val boltH = height * 0.72f
            val boltW = boltH * 0.52f
            val capsuleCenterX = left + width / 2f
            val boltColor = if (fillFraction > 0.4f) Color.BLACK else accentColor
            drawChargingBolt(canvas, capsuleCenterX, centerY, boltW, boltH, boltColor, 255)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Shared Component: Geometric Vector Charging Bolt Path Drawer
    // ──────────────────────────────────────────────────────────────────────────

    private fun drawChargingBolt(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        width: Float,
        height: Float,
        color: Int,
        alpha: Int
    ) {
        val path = Path().apply {
            moveTo(cx + width * 0.12f, cy - height * 0.50f)
            lineTo(cx - width * 0.45f, cy + height * 0.04f)
            lineTo(cx - width * 0.04f, cy + height * 0.04f)
            lineTo(cx - width * 0.22f, cy + height * 0.50f)
            lineTo(cx + width * 0.45f, cy - height * 0.04f)
            lineTo(cx + width * 0.04f, cy - height * 0.04f)
            close()
        }

        val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.style = Paint.Style.FILL
            this.alpha = alpha.coerceIn(0, 255)
        }

        canvas.drawPath(path, boltPaint)
    }
}
