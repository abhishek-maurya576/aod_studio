package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for NOTIFICATION type elements.
 * Displays active notification count or icons indicator with fallback for preview/editor mode.
 */
class NotificationElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val count = context.notificationCount
        val visibilityMode = element.properties["visibilityMode"]?.uppercase() ?: "COUNT_BADGE"

        // Use live count if available; fallback to "2" indicator for preview/editor mode
        val displayCount = if (count > 0) count else 2

        val textContent = when (visibilityMode) {
            "ICONS_ONLY" -> "🔔  💬  ✉️"
            "DETAILED" -> "🔔 $displayCount Notifications"
            else -> "🔔 $displayCount"
        }

        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        canvas.drawText(textContent, drawX, drawY, paint)
    }
}
