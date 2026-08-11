package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for BATTERY type elements.
 * Renders percentage text, battery icon, or battery bar ring using uniform scale and positioning.
 */
class BatteryElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val styleType = element.properties[AODElement.PROP_BATTERY_STYLE] ?: "PERCENTAGE"

        when (styleType.uppercase()) {
            "RING" -> renderBatteryRing(canvas, element, context)
            "BAR" -> renderBatteryBar(canvas, element, context)
            else -> renderBatteryText(canvas, element, context)
        }
    }

    private fun renderBatteryText(canvas: Canvas, element: AODElement, context: RenderContext) {
        val chargingSymbol = if (context.isCharging) "⚡ " else ""
        val textContent = "$chargingSymbol${context.batteryPercentage}%"

        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        canvas.drawText(textContent, drawX, drawY, paint)
    }

    private fun renderBatteryRing(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val radius = (element.width / 2f) * context.scaleFactor

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            style = Paint.Style.STROKE
            strokeWidth = element.style.strokeWidth * context.scaleFactor
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val oval = RectF(drawX - radius, drawY - radius, drawX + radius, drawY + radius)
        val sweepAngle = (context.batteryPercentage / 100f) * 360f

        canvas.drawArc(oval, -90f, sweepAngle, false, strokePaint)
    }

    private fun renderBatteryBar(canvas: Canvas, element: AODElement, context: RenderContext) {
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val w = element.width * context.scaleFactor
        val h = element.height * context.scaleFactor

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RendererUtils.parseColor(element.style.color)
            style = Paint.Style.FILL
            alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
        }

        val fillWidth = (context.batteryPercentage / 100f) * w
        val rect = RectF(drawX - w / 2f, drawY - h / 2f, (drawX - w / 2f) + fillWidth, drawY + h / 2f)
        canvas.drawRoundRect(rect, element.style.cornerRadius * context.scaleFactor, element.style.cornerRadius * context.scaleFactor, barPaint)
    }
}
