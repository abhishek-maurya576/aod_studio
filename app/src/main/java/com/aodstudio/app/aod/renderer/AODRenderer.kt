package com.aodstudio.app.aod.renderer

import android.graphics.Canvas
import android.graphics.Color
import com.aodstudio.app.aod.renderer.renderers.AnalogClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.BatteryElementRenderer
import com.aodstudio.app.aod.renderer.renderers.ClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.DateElementRenderer
import com.aodstudio.app.aod.renderer.renderers.GroupElementRenderer
import com.aodstudio.app.aod.renderer.renderers.ImageElementRenderer
import com.aodstudio.app.aod.renderer.renderers.MusicElementRenderer
import com.aodstudio.app.aod.renderer.renderers.NotificationElementRenderer
import com.aodstudio.app.aod.renderer.renderers.RadialClockElementRenderer
import com.aodstudio.app.aod.renderer.renderers.ShapeElementRenderer
import com.aodstudio.app.aod.renderer.renderers.TextElementRenderer
import com.aodstudio.app.aod.renderer.renderers.TypographyClockElementRenderer
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODTheme

/**
 * Core Canvas rendering engine pipeline.
 *
 * Takes an AODTheme configuration + live RenderContext, sorts elements by zIndex,
 * applies AMOLED black background, and delegates element drawing to sub-renderers.
 *
 * Key principle: ONE renderer engine handles ALL theme configurations.
 */
class AODRenderer {

    private val textRenderer = TextElementRenderer()
    private val clockRenderer = ClockElementRenderer()
    private val analogClockRenderer = AnalogClockElementRenderer()
    private val typographyClockRenderer = TypographyClockElementRenderer()
    private val radialClockRenderer = RadialClockElementRenderer()
    private val dateRenderer = DateElementRenderer()
    private val batteryRenderer = BatteryElementRenderer()
    private val shapeRenderer = ShapeElementRenderer()
    private val notificationRenderer = NotificationElementRenderer()
    private val musicRenderer = MusicElementRenderer()
    private val imageRenderer = ImageElementRenderer()
    private val groupRenderer = GroupElementRenderer()


    /**
     * Renders a complete AODTheme onto the provided Canvas.
     */
    fun renderTheme(canvas: Canvas, theme: AODTheme, context: RenderContext) {
        // 1. Clear background — AMOLED pure black (#000000)
        val bgColor = RendererUtils.parseColor(theme.canvas.background, Color.BLACK)
        canvas.drawColor(bgColor)

        // 2. Sort elements by zIndex ascending
        val sortedElements = theme.elements
            .filter { it.visibility }
            .sortedBy { it.zIndex }

        // 3. Render each element with burn-in offset and matrix transforms
        for (element in sortedElements) {
            canvas.save()

            // Apply burn-in shift offset
            val drawX = (element.x + context.burnInOffsetX) * context.scaleFactorX
            val drawY = (element.y + context.burnInOffsetY) * context.scaleFactorY

            // Apply element rotation around element center
            if (element.rotation != 0f) {
                canvas.rotate(element.rotation, drawX, drawY)
            }

            // Apply element scale
            if (element.scale != 1f) {
                canvas.scale(element.scale, element.scale, drawX, drawY)
            }

            // Delegate to sub-renderer
            val renderer = getRendererForType(element)
            renderer.render(canvas, element, context)

            canvas.restore()
        }
    }

    private fun getRendererForType(element: AODElement): ElementRenderer {
        return when (element.type) {
            AODElementType.CLOCK -> {
                when (element.properties["clockStyle"]?.uppercase()) {
                    "ANALOG" -> analogClockRenderer
                    "TYPOGRAPHY", "STACKED" -> typographyClockRenderer
                    "RADIAL", "ORBIT" -> radialClockRenderer
                    else -> clockRenderer
                }
            }
            AODElementType.DATE -> dateRenderer
            AODElementType.BATTERY -> batteryRenderer
            AODElementType.NOTIFICATION -> notificationRenderer
            AODElementType.MUSIC -> musicRenderer
            AODElementType.IMAGE -> imageRenderer
            AODElementType.GROUP -> groupRenderer
            AODElementType.SHAPE, AODElementType.LINE, AODElementType.RING, AODElementType.PROGRESS -> shapeRenderer
            else -> textRenderer
        }
    }
}
