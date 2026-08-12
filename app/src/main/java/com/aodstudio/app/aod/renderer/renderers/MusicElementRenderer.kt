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
import kotlin.math.sin

/**
 * Renderer for MUSIC type elements matching Android 16-17 Material You System UI design specifications.
 * Supports Small Mode (compact track info + play/pause) and Large Mode (prominent album art, metadata, squiggly wave, full controls).
 * Uses native vector path rendering for media control icons without unicode emojis.
 */
class MusicElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val title = context.mediaTitle ?: "Midnight City"
        val artist = context.mediaArtist ?: "M83 • Android 16 Wave"
        val isPlaying = context.mediaIsPlaying
        val albumArtBitmap = context.mediaAlbumArt

        val playerSize = element.properties[AODElement.PROP_PLAYER_SIZE]?.uppercase() ?: "LARGE"
        val musicStyle = element.properties["musicStyle"]?.uppercase() ?: "WAVY_PROGRESS"
        val waveIntensity = element.properties["waveIntensity"]?.uppercase() ?: "MEDIUM"
        val showAlbumArt = element.properties["showAlbumArt"]?.toBoolean() ?: (playerSize == "LARGE")
        val showControls = element.properties["showControls"]?.toBoolean() ?: true

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val scale = context.scaleFactor * element.scale

        val primaryColor = RendererUtils.parseColor(element.style.color)
        val alphaInt = (element.opacity * 255).toInt().coerceIn(0, 255)

        val textPaint = RendererUtils.createTextPaint(element.style, scale).apply {
            alpha = alphaInt
            textAlign = Paint.Align.LEFT
        }

        val subTextPaint = Paint(textPaint).apply {
            textSize = element.style.fontSize * 0.55f * scale
            color = Color.GRAY
            alpha = (alphaInt * 0.7f).toInt()
            textAlign = Paint.Align.LEFT
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
            alpha = alphaInt
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            style = Paint.Style.FILL
            alpha = alphaInt
        }

        if (playerSize == "SMALL" || musicStyle == "MINIMAL" || musicStyle == "COMPACT") {
            // ─── SMALL COMPACT MODE ──────────────────────────────────────────
            val startX = drawX - 120f * scale
            val maxTextWidth = 180f * scale
            
            // Draw vector music note badge
            drawMusicNoteVector(canvas, startX + 10f * scale, drawY - 12f * scale, 14f * scale, fillPaint)

            // Draw compact track title and artist with boundary truncation
            val rawTrackText = if (musicStyle == "MINIMAL") title else "$title • $artist"
            val displayTrackText = truncateText(rawTrackText, textPaint, maxTextWidth)
            canvas.drawText(displayTrackText, startX + 32f * scale, drawY - 6f * scale, textPaint)

            // Draw compact Play/Pause button
            val playBtnX = startX + 220f * scale
            val playBtnY = drawY - 12f * scale
            if (isPlaying) {
                drawPauseIconVector(canvas, playBtnX, playBtnY, 14f * scale, fillPaint)
            } else {
                drawPlayIconVector(canvas, playBtnX, playBtnY, 14f * scale, fillPaint)
            }
        } else {
            // ─── LARGE EXPANDED MODE (Android 16 Squiggly Wave UI) ────────────

            // 1. Prominent Album Art Thumbnail
            val artSize = 44f * scale
            val artStartX = drawX - 160f * scale
            val artStartY = drawY - 55f * scale
            val artRect = RectF(artStartX, artStartY, artStartX + artSize, artStartY + artSize)

            if (showAlbumArt) {
                if (albumArtBitmap != null && !albumArtBitmap.isRecycled) {
                    val srcRect = Rect(0, 0, albumArtBitmap.width, albumArtBitmap.height)
                    canvas.drawBitmap(albumArtBitmap, srcRect, artRect, fillPaint)
                } else {
                    // Fallback artwork container with music note vector
                    fillPaint.color = primaryColor
                    fillPaint.alpha = (alphaInt * 0.2f).toInt()
                    canvas.drawRoundRect(artRect, 10f * scale, 10f * scale, fillPaint)

                    fillPaint.alpha = alphaInt
                    drawMusicNoteVector(canvas, artRect.centerX(), artRect.centerY(), 18f * scale, fillPaint)
                }
            }

            // 2. Track Title & Artist Metadata (with strict width truncation)
            textPaint.textAlign = Paint.Align.LEFT
            subTextPaint.textAlign = Paint.Align.LEFT
            val textLeftX = if (showAlbumArt) drawX - 105f * scale else drawX - 160f * scale
            val maxTextWidth = (drawX + 155f * scale - textLeftX).coerceAtLeast(120f * scale)

            val displayTitle = truncateText(title, textPaint, maxTextWidth)
            val displayArtist = truncateText(artist, subTextPaint, maxTextWidth)

            canvas.drawText(displayTitle, textLeftX, drawY - 32f * scale, textPaint)
            canvas.drawText(displayArtist, textLeftX, drawY - 12f * scale, subTextPaint)

            // 3. Android 16 Animated Squiggly/Wavy Progress Line
            val waveWidth = 320f * scale
            val waveStartX = drawX - 160f * scale
            val waveY = drawY + 14f * scale

            val amplitude = when (waveIntensity) {
                "HIGH" -> 8f * scale
                "LOW" -> 3f * scale
                else -> 5f * scale
            }
            val frequency = 0.08f / scale
            val animPhase = if (isPlaying) (System.currentTimeMillis() / 40.0) else 0.0

            val wavePath = Path()
            wavePath.moveTo(waveStartX, waveY)

            var x = 0f
            val playedFraction = if (context.mediaDurationMs > 0) {
                (context.mediaProgressMs.toFloat() / context.mediaDurationMs.toFloat()).coerceIn(0.15f, 0.85f)
            } else 0.65f

            val playedWidth = waveWidth * playedFraction

            while (x <= playedWidth) {
                val yOffset = sin(x * frequency + animPhase).toFloat() * amplitude
                wavePath.lineTo(waveStartX + x, waveY + yOffset)
                x += 4f * scale
            }

            // Draw Squiggly Wave Path for Played Portion
            strokePaint.color = primaryColor
            strokePaint.alpha = alphaInt
            canvas.drawPath(wavePath, strokePaint)

            // Draw Straight Line for Remaining Portion
            val remainingPath = Path().apply {
                moveTo(waveStartX + playedWidth, waveY)
                lineTo(waveStartX + waveWidth, waveY)
            }
            strokePaint.color = Color.GRAY
            strokePaint.alpha = (alphaInt * 0.35f).toInt()
            canvas.drawPath(remainingPath, strokePaint)

            // Wave Thumb / Playhead Circle
            val thumbX = waveStartX + playedWidth
            val thumbY = waveY + sin(playedWidth * frequency + animPhase).toFloat() * amplitude
            fillPaint.color = primaryColor
            fillPaint.alpha = alphaInt
            canvas.drawCircle(thumbX, thumbY, 5f * scale, fillPaint)

            // 4. Transport Control Buttons (Previous, Play/Pause, Next)
            if (showControls) {
                val btnY = drawY + 48f * scale

                // Previous Button Vector
                drawSkipPreviousVector(canvas, drawX - 70f * scale, btnY, 18f * scale, fillPaint)

                // Play / Pause Button Vector (toggles based on live playback state)
                if (isPlaying) {
                    drawPauseIconVector(canvas, drawX, btnY, 20f * scale, fillPaint)
                } else {
                    drawPlayIconVector(canvas, drawX, btnY, 20f * scale, fillPaint)
                }

                // Next Button Vector
                drawSkipNextVector(canvas, drawX + 70f * scale, btnY, 18f * scale, fillPaint)
            }
        }
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "..."
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return if (end > 0) text.substring(0, end) + ellipsis else ellipsis
    }

    // ─── Native Vector Icon Drawing Helpers (No Emojis) ──────────────────────────

    private fun drawMusicNoteVector(canvas: Canvas, centerX: Float, centerY: Float, size: Float, paint: Paint) {
        val half = size / 2f
        // Note head
        canvas.drawCircle(centerX - half * 0.3f, centerY + half * 0.4f, half * 0.35f, paint)
        // Stem
        canvas.drawRect(
            centerX + half * 0.05f,
            centerY - half * 0.6f,
            centerX + half * 0.25f,
            centerY + half * 0.4f,
            paint
        )
        // Beam/Flag
        val flagPath = Path().apply {
            moveTo(centerX + half * 0.05f, centerY - half * 0.6f)
            lineTo(centerX + half * 0.6f, centerY - half * 0.4f)
            lineTo(centerX + half * 0.6f, centerY - half * 0.1f)
            lineTo(centerX + half * 0.05f, centerY - half * 0.3f)
            close()
        }
        canvas.drawPath(flagPath, paint)
    }

    private fun drawPlayIconVector(canvas: Canvas, centerX: Float, centerY: Float, size: Float, paint: Paint) {
        val half = size / 2f
        val path = Path().apply {
            moveTo(centerX - half * 0.6f, centerY - half * 0.8f)
            lineTo(centerX + half * 0.8f, centerY)
            lineTo(centerX - half * 0.6f, centerY + half * 0.8f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawPauseIconVector(canvas: Canvas, centerX: Float, centerY: Float, size: Float, paint: Paint) {
        val barWidth = size * 0.25f
        val barHeight = size * 0.8f
        val gap = size * 0.2f

        val leftRect = RectF(
            centerX - gap / 2f - barWidth,
            centerY - barHeight / 2f,
            centerX - gap / 2f,
            centerY + barHeight / 2f
        )
        val rightRect = RectF(
            centerX + gap / 2f,
            centerY - barHeight / 2f,
            centerX + gap / 2f + barWidth,
            centerY + barHeight / 2f
        )

        canvas.drawRoundRect(leftRect, 3f, 3f, paint)
        canvas.drawRoundRect(rightRect, 3f, 3f, paint)
    }

    private fun drawSkipPreviousVector(canvas: Canvas, centerX: Float, centerY: Float, size: Float, paint: Paint) {
        val half = size / 2f
        // Vertical left bar
        canvas.drawRect(centerX - half * 0.8f, centerY - half * 0.7f, centerX - half * 0.5f, centerY + half * 0.7f, paint)
        // Left triangle
        val path = Path().apply {
            moveTo(centerX + half * 0.6f, centerY - half * 0.7f)
            lineTo(centerX - half * 0.4f, centerY)
            lineTo(centerX + half * 0.6f, centerY + half * 0.7f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawSkipNextVector(canvas: Canvas, centerX: Float, centerY: Float, size: Float, paint: Paint) {
        val half = size / 2f
        // Right triangle
        val path = Path().apply {
            moveTo(centerX - half * 0.6f, centerY - half * 0.7f)
            lineTo(centerX + half * 0.4f, centerY)
            lineTo(centerX - half * 0.6f, centerY + half * 0.7f)
            close()
        }
        canvas.drawPath(path, paint)
        // Vertical right bar
        canvas.drawRect(centerX + half * 0.5f, centerY - half * 0.7f, centerX + half * 0.8f, centerY + half * 0.7f, paint)
    }
}

