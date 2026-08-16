package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for FINGERPRINT biometric indicator elements.
 * Draws a high-definition vector fingerprint glyph with concentric biometric ridges
 * and an optional outer alignment ring.
 */
class FingerprintElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val color = RendererUtils.parseColor(element.style.color)
        val alphaInt = (element.opacity * 255).toInt().coerceIn(0, 255)
        val scale = context.scaleFactor

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        // Base stroke thickness and radius
        val strokeW = (element.style.strokeWidth.takeIf { it > 0 } ?: 2.5f) * scale
        val radius = (element.width / 2f).coerceAtLeast(36f) * scale

        val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.alpha = alphaInt
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            this.strokeCap = Paint.Cap.ROUND
            this.strokeJoin = Paint.Join.ROUND
        }

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.alpha = (alphaInt * 0.35f).toInt().coerceIn(0, 255)
            this.style = Paint.Style.STROKE
            this.strokeWidth = 1.5f * scale
        }

        // 1. Outer Target Guide Ring
        val showGuideRing = element.properties["showGuideRing"]?.toBoolean() ?: true
        if (showGuideRing) {
            canvas.drawCircle(drawX, drawY, radius, ringPaint)
        }

        // 2. Biometric Fingerprint Ridges (Concentric Arcs & Paths)
        val r1 = radius * 0.28f
        val r2 = radius * 0.48f
        val r3 = radius * 0.68f

        // Center Loop Ridge
        val oval1 = RectF(drawX - r1, drawY - r1 * 1.3f, drawX + r1, drawY + r1 * 0.9f)
        val path1 = Path().apply {
            arcTo(oval1, 180f, 180f)
            lineTo(drawX + r1, drawY + r1 * 0.4f)
        }
        canvas.drawPath(path1, ridgePaint)

        // Mid Ridge Loop
        val oval2 = RectF(drawX - r2, drawY - r2 * 1.25f, drawX + r2, drawY + r2 * 0.95f)
        val path2 = Path().apply {
            arcTo(oval2, 190f, 160f)
            lineTo(drawX + r2 * 0.9f, drawY + r2 * 0.5f)
        }
        canvas.drawPath(path2, ridgePaint)

        // Left Accent Ridge
        val pathLeft = Path().apply {
            val ovalLeft = RectF(drawX - r2 * 0.9f, drawY - r2 * 0.6f, drawX - r1 * 0.3f, drawY + r2 * 0.7f)
            arcTo(ovalLeft, 90f, 120f)
        }
        canvas.drawPath(pathLeft, ridgePaint)

        // Outer Upper Ridge
        val oval3 = RectF(drawX - r3, drawY - r3 * 1.15f, drawX + r3, drawY + r3 * 0.85f)
        val path3 = Path().apply {
            arcTo(oval3, 205f, 130f)
        }
        canvas.drawPath(path3, ridgePaint)

        // Lower Right Tail Ridge
        val pathRight = Path().apply {
            moveTo(drawX + r3 * 0.75f, drawY - r3 * 0.1f)
            quadTo(drawX + r3 * 0.8f, drawY + r3 * 0.4f, drawX + r3 * 0.4f, drawY + r3 * 0.75f)
        }
        canvas.drawPath(pathRight, ridgePaint)
    }
}
