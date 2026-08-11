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
import kotlin.math.sin

/**
 * Renderer for MUSIC type elements matching Android 16-17 Material You System UI design specifications.
 * Implements animated squiggly/wavy progress line, album thumbnail, track metadata, and media control buttons.
 */
class MusicElementRenderer : ElementRenderer {

    override fun render(canvas: Canvas, element: AODElement, context: RenderContext) {
        val title = context.mediaTitle ?: "Midnight City"
        val artist = context.mediaArtist ?: "M83 • Android 16 Wave"

        val musicStyle = element.properties["musicStyle"]?.uppercase() ?: "WAVY_PROGRESS"
        val waveIntensity = element.properties["waveIntensity"]?.uppercase() ?: "MEDIUM"
        val showAlbumArt = element.properties["showAlbumArt"]?.toBoolean() ?: true
        val showControls = element.properties["showControls"]?.toBoolean() ?: true

        val drawX = RendererUtils.getDrawX(element, context)
        val drawY = RendererUtils.getDrawY(element, context)
        val scale = context.scaleFactor

        val primaryColor = RendererUtils.parseColor(element.style.color)
        val alphaInt = (element.opacity * 255).toInt().coerceIn(0, 255)

        val textPaint = RendererUtils.createTextPaint(element.style, scale).apply {
            alpha = alphaInt
            textAlign = Paint.Align.CENTER
        }

        val subTextPaint = Paint(textPaint).apply {
            textSize = element.style.fontSize * 0.55f * scale
            color = Color.GRAY
            alpha = (alphaInt * 0.7f).toInt()
            textAlign = Paint.Align.CENTER
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

        when (musicStyle) {
            "MINIMAL" -> {
                canvas.drawText("🎵 $title", drawX, drawY, textPaint)
            }

            "COMPACT" -> {
                canvas.drawText("🎵 $title • $artist", drawX, drawY, textPaint)
            }

            else -> {
                // WAVY_PROGRESS, CLASSIC, or NEON_WAVE (Android 16 Squiggly Wave UI)

                // 1. Optional Album Art Thumbnail Circle
                if (showAlbumArt) {
                    val artSize = 32f * scale
                    val artRect = RectF(
                        drawX - 160f * scale,
                        drawY - 50f * scale,
                        drawX - 160f * scale + artSize,
                        drawY - 50f * scale + artSize
                    )
                    fillPaint.color = primaryColor
                    fillPaint.alpha = (alphaInt * 0.25f).toInt()
                    canvas.drawRoundRect(artRect, 8f * scale, 8f * scale, fillPaint)

                    val notePaint = Paint(textPaint).apply {
                        textSize = 16f * scale
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("🎵", artRect.centerX(), artRect.centerY() + 5f * scale, notePaint)
                }

                // 2. Track Title & Artist
                textPaint.textAlign = Paint.Align.LEFT
                subTextPaint.textAlign = Paint.Align.LEFT
                val textLeftX = if (showAlbumArt) drawX - 115f * scale else drawX - 160f * scale

                canvas.drawText(title, textLeftX, drawY - 32f * scale, textPaint)
                canvas.drawText(artist, textLeftX, drawY - 12f * scale, subTextPaint)

                // 3. Android 16 Squiggly / Wavy Animated Progress Bar Path
                val waveWidth = 320f * scale
                val waveStartX = drawX - 160f * scale
                val waveY = drawY + 12f * scale

                val amplitude = when (waveIntensity) {
                    "HIGH" -> 8f * scale
                    "LOW" -> 3f * scale
                    else -> 5f * scale
                }
                val frequency = 0.08f / scale
                val animPhase = (System.currentTimeMillis() / 40.0)

                val wavePath = Path()
                wavePath.moveTo(waveStartX, waveY)

                var x = 0f
                while (x <= waveWidth * 0.65f) {
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
                    moveTo(waveStartX + waveWidth * 0.65f, waveY)
                    lineTo(waveStartX + waveWidth, waveY)
                }
                strokePaint.color = Color.GRAY
                strokePaint.alpha = (alphaInt * 0.35f).toInt()
                canvas.drawPath(remainingPath, strokePaint)

                // Wave Thumb / Playhead Circle
                val thumbX = waveStartX + waveWidth * 0.65f
                val thumbY = waveY + sin(waveWidth * 0.65f * frequency + animPhase).toFloat() * amplitude
                fillPaint.color = primaryColor
                fillPaint.alpha = alphaInt
                canvas.drawCircle(thumbX, thumbY, 5f * scale, fillPaint)

                // 4. Media Control Buttons (⏮ ▶ ⏭)
                if (showControls) {
                    val btnY = drawY + 45f * scale
                    val ctrlPaint = Paint(textPaint).apply {
                        textSize = 20f * scale
                        textAlign = Paint.Align.CENTER
                    }

                    canvas.drawText("⏮", drawX - 60f * scale, btnY, ctrlPaint)
                    canvas.drawText(if (context.mediaIsPlaying) "⏸" else "▶", drawX, btnY, ctrlPaint)
                    canvas.drawText("⏭", drawX + 60f * scale, btnY, ctrlPaint)
                }
            }
        }
    }
}
