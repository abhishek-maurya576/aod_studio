package com.aodstudio.app.aod.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.aodstudio.app.aod.lifecycle.BurnInManager
import com.aodstudio.app.domain.model.AODTheme
import java.util.Date

/**
 * Custom View host for rendering AOD Canvas themes on screen.
 * Implements 1Hz frame-rate capping and AMOLED black background optimization for battery saving.
 */
class AODRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val renderer = AODRenderer()
    private val burnInManager = BurnInManager()
    private var currentTheme: AODTheme? = null
    private var lastRedrawTimeMs: Long = 0L

    // 1 Hz refresh rate limit (1000ms) for battery saving on AMOLED display
    private val minRedrawIntervalMs: Long = 1000L

    init {
        setBackgroundColor(Color.BLACK)
    }

    fun setTheme(theme: AODTheme) {
        this.currentTheme = theme
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentTimeMs = System.currentTimeMillis()

        // Battery drain guard: throttle redraws if called faster than minRedrawIntervalMs
        if (currentTimeMs - lastRedrawTimeMs < minRedrawIntervalMs && lastRedrawTimeMs != 0L) {
            postInvalidateDelayed(minRedrawIntervalMs - (currentTimeMs - lastRedrawTimeMs))
            return
        }

        lastRedrawTimeMs = currentTimeMs

        canvas.drawColor(Color.BLACK)

        val theme = currentTheme ?: return
        val burnInOffset = burnInManager.calculateOffset(currentTimeMs)

        val renderContext = RenderContext(
            viewWidth = width,
            viewHeight = height,
            scaleFactorX = if (theme.canvas.width > 0) width.toFloat() / theme.canvas.width else 1.0f,
            scaleFactorY = if (theme.canvas.height > 0) height.toFloat() / theme.canvas.height else 1.0f,
            burnInOffsetX = burnInOffset.offsetX,
            burnInOffsetY = burnInOffset.offsetY,
            date = Date(currentTimeMs),
            batteryPercentage = 100,
            isCharging = false
        )

        renderer.renderTheme(canvas, theme, renderContext)

        // Schedule next battery-friendly redraw at 1Hz interval
        postInvalidateDelayed(minRedrawIntervalMs)
    }
}
