package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for NOTIFICATION type elements.
 * Renders real app icon Bitmaps, live notification count badges, or detailed text summary.
 * Supports dynamic icon sizing (via element.style.fontSize and element.scale) and smart text truncation.
 */
class NotificationElementRenderer : ElementRenderer {

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#33FFFFFF")
    }

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val notifications = context.notificationsList
        val count = context.notificationCount
        val visibilityMode = element.properties["visibilityMode"]?.uppercase() ?: "ICONS_ONLY"
        val isPreview = element.properties["isPreview"]?.toBoolean() ?: false

        // Hide element on live AOD screen if zero notifications active
        if (count == 0 && !isPreview && notifications.isEmpty()) {
            return
        }

        val scale = context.scaleFactor
        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        val alphaInt = (element.opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.alpha = alphaInt
        badgeBgPaint.alpha = (element.opacity * 0.2f * 255).toInt().coerceIn(0, 255)

        when (visibilityMode) {
            "ICONS_ONLY", "ICONS" -> {
                renderAppIcons(canvas, element, context, notifications, drawX, drawY, scale, alphaInt)
            }
            "DETAILED" -> {
                val latest = notifications.firstOrNull()
                val rawTextContent = if (latest != null && latest.appName.isNotBlank()) {
                    "🔔 ${latest.appName}${if (latest.title.isNotBlank()) ": ${latest.title}" else ""}"
                } else {
                    "🔔 $count Notifications"
                }

                val paint = RendererUtils.createTextPaint(element.style, scale)
                paint.alpha = alphaInt

                // Constrain text to container width with smart truncation ("...")
                val canvasW = if (context.viewWidth > 0) context.viewWidth.toFloat() else 1080f
                val maxTextWidth = (canvasW * 0.85f * context.scaleFactor).coerceAtLeast(300f * scale)
                val truncatedText = truncateText(rawTextContent, paint, maxTextWidth)

                canvas.drawText(truncatedText, drawX, drawY, paint)
            }
            else -> {
                // COUNT_BADGE
                val displayCount = if (count > 0) count else 1
                val textContent = "🔔 $displayCount"
                val paint = RendererUtils.createTextPaint(element.style, scale)
                paint.alpha = alphaInt
                canvas.drawText(textContent, drawX, drawY, paint)
            }
        }
    }

    private fun renderAppIcons(
        canvas: Canvas,
        element: AODElement,
        context: RenderContext,
        notifications: List<com.aodstudio.app.notification.NotificationItem>,
        centerX: Float,
        centerY: Float,
        scale: Float,
        alphaInt: Int
    ) {
        // Calculate dynamic icon size from element.style.fontSize & element.scale
        val baseFontSize = if (element.style.fontSize > 0f) element.style.fontSize else 24f
        val iconSize = (baseFontSize * 1.8f * scale).coerceIn(24f * scale, 120f * scale)
        val spacing = iconSize * 0.35f

        // Take up to 5 distinct app icons
        val itemsToDraw = if (notifications.isNotEmpty()) {
            notifications.distinctBy { it.packageName }.take(5)
        } else {
            emptyList()
        }

        if (itemsToDraw.isEmpty()) {
            // Render clean bell icon indicator for editor preview mode
            val textPaint = RendererUtils.createTextPaint(element.style, scale)
            textPaint.alpha = alphaInt
            canvas.drawText("🔔 2 Notifications", centerX, centerY, textPaint)
            return
        }

        val totalWidth = itemsToDraw.size * iconSize + (itemsToDraw.size - 1) * spacing
        var currentX = centerX - (totalWidth / 2f)

        val srcRect = Rect()
        val dstRect = RectF()

        for (item in itemsToDraw) {
            val bitmap = item.iconBitmap
            val left = currentX
            val top = centerY - (iconSize / 2f)
            val right = currentX + iconSize
            val bottom = centerY + (iconSize / 2f)

            dstRect.set(left, top, right, bottom)

            if (bitmap != null && !bitmap.isRecycled) {
                srcRect.set(0, 0, bitmap.width, bitmap.height)
                canvas.drawBitmap(bitmap, srcRect, dstRect, iconPaint)
            } else {
                // Draw clean circular fallback badge with bell path
                val radius = iconSize / 2f
                canvas.drawCircle(left + radius, top + radius, radius, badgeBgPaint)
                drawFallbackBellIcon(canvas, left + radius, top + radius, radius * 0.6f, alphaInt)
            }

            currentX += iconSize + spacing
        }
    }

    private fun drawFallbackBellIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, alphaInt: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.WHITE
            alpha = alphaInt
        }
        val path = Path().apply {
            moveTo(cx - size * 0.5f, cy + size * 0.3f)
            lineTo(cx + size * 0.5f, cy + size * 0.3f)
            quadTo(cx + size * 0.4f, cy - size * 0.3f, cx, cy - size * 0.5f)
            quadTo(cx - size * 0.4f, cy - size * 0.3f, cx - size * 0.5f, cy + size * 0.3f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text

        var low = 0
        var high = text.length
        var best = text

        while (low <= high) {
            val mid = (low + high) / 2
            val candidate = text.substring(0, mid) + "..."
            if (paint.measureText(candidate) <= maxWidth) {
                best = candidate
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best
    }
}
