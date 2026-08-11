package com.aodstudio.app.aod.renderer.renderers

import android.graphics.Canvas
import com.aodstudio.app.aod.renderer.ElementRenderer
import com.aodstudio.app.aod.renderer.RenderContext
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.domain.model.AODElement

/**
 * Renderer for MUSIC type elements using uniform scale and coordinate transformation.
 */
class MusicElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val title = context.mediaTitle ?: return
        val artist = context.mediaArtist ?: ""

        val playSymbol = if (context.mediaIsPlaying) "🎵 " else "⏸ "
        val displayString = if (artist.isBlank()) "$playSymbol$title" else "$playSymbol$title - $artist"

        val paint = RendererUtils.createTextPaint(element.style, context.scaleFactor)
        paint.alpha = (element.opacity * 255).toInt().coerceIn(0, 255)

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)

        canvas.drawText(displayString, drawX, drawY, paint)
    }
}
