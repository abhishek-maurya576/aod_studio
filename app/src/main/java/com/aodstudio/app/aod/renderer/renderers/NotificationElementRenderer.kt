package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for NOTIFICATION type elements.
 * Displays active notification count or notification icons indicator.
 */
class NotificationElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        if (context.notificationCount <= 0) return

        val textContent = "🔔 ${context.notificationCount}"
        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        canvas.drawText(textContent, drawX, drawY, paint)
    }
}
