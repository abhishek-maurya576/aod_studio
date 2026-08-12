package com.aodstudio.app.aod.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.aodstudio.app.aod.lifecycle.BurnInManager
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import java.util.Date
import kotlin.math.min

/**
 * Custom View host for rendering AOD Canvas themes on screen.
 * Implements 1Hz frame-rate capping, AMOLED black background, and uniform aspect-ratio scaling.
 */
class AODRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val renderer = AODRenderer()
    private val burnInManager = BurnInManager()
    private var currentTheme: AODTheme? = null

    private var batteryRepo: BatteryRepository? = null
    private var notificationRepo: NotificationRepository? = null
    private var mediaRepo: MediaRepository? = null

    private val redrawIntervalMs: Long = 1000L
    private val redrawHandler = Handler(Looper.getMainLooper())
    private var isRendering = false

    private val redrawRunnable = object : Runnable {
        override fun run() {
            if (isRendering) {
                invalidate()
                redrawHandler.postDelayed(this, redrawIntervalMs)
            }
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        // GPU-backed layer: the compositor caches this View's content between 1Hz redraws.
        // Reduces CPU wake time between invalidate() calls — critical for battery efficiency
        // during screen-off AOD mode where we're already fighting OEM power management.
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setTheme(theme: AODTheme) {
        this.currentTheme = theme
        invalidate()
    }

    fun setBatteryRepository(repo: BatteryRepository) {
        this.batteryRepo = repo
    }

    fun setNotificationRepository(repo: NotificationRepository) {
        this.notificationRepo = repo
    }

    fun setMediaRepository(repo: MediaRepository) {
        this.mediaRepo = repo
    }

    fun stopRendering() {
        isRendering = false
        redrawHandler.removeCallbacks(redrawRunnable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRendering = true
        redrawHandler.postDelayed(redrawRunnable, redrawIntervalMs)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRendering()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        val theme = currentTheme ?: return
        val currentTimeMs = System.currentTimeMillis()
        val burnInOffset = burnInManager.calculateOffset(currentTimeMs)

        val battery = batteryRepo?.getCurrentBatteryState()
        val notifications = notificationRepo?.activeNotifications?.value
        val media = mediaRepo?.mediaState?.value

        // Calculate uniform scale and letterbox offsets to preserve exact theme proportions
        val canvasW = if (theme.canvas.width > 0) theme.canvas.width.toFloat() else 1080f
        val canvasH = if (theme.canvas.height > 0) theme.canvas.height.toFloat() else 2400f

        val scaleX = if (width > 0) width.toFloat() / canvasW else 1f
        val scaleY = if (height > 0) height.toFloat() / canvasH else 1f
        val uniformScale = min(scaleX, scaleY)

        val contentW = canvasW * uniformScale
        val contentH = canvasH * uniformScale
        val offsetX = (width.toFloat() - contentW) / 2f
        val offsetY = (height.toFloat() - contentH) / 2f

        val renderContext = RenderContext(
            viewWidth = width,
            viewHeight = height,
            scaleFactor = uniformScale,
            contentOffsetX = offsetX,
            contentOffsetY = offsetY,
            burnInOffsetX = burnInOffset.offsetX,
            burnInOffsetY = burnInOffset.offsetY,
            date = Date(currentTimeMs),
            batteryPercentage = battery?.percentage ?: 100,
            isCharging = battery?.isCharging ?: false,
            isBatteryFull = battery?.isFull ?: false,
            notificationCount = notifications?.size ?: 0,
            notificationPackages = notifications?.map { it.packageName } ?: emptyList(),
            mediaTitle = media?.title,
            mediaArtist = media?.artist,
            mediaIsPlaying = media?.isPlaying ?: false
        )

        renderer.renderTheme(canvas, theme, renderContext)
    }
}
