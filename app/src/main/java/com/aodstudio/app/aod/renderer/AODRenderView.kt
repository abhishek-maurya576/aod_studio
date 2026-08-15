package com.aodstudio.app.aod.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.aodstudio.app.aod.lifecycle.BurnInManager
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs
import kotlin.math.min

/**
 * Custom View host for rendering AOD Canvas themes on screen.
 * Implements 1Hz frame-rate capping, AMOLED black background, uniform aspect-ratio scaling,
 * and direct touch interaction for system media controls.
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
        // GPU-backed layer for hardware composition
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setTheme(theme: AODTheme) {
        this.currentTheme = theme
        invalidate()
    }

    fun setBatteryRepository(repo: BatteryRepository) {
        this.batteryRepo = repo
    }

    private var notificationJob: kotlinx.coroutines.Job? = null
    private val viewScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    fun setNotificationRepository(repo: NotificationRepository) {
        this.notificationRepo = repo
        observeNotificationFlow()
    }

    private fun observeNotificationFlow() {
        notificationJob?.cancel()
        val repo = notificationRepo ?: return
        notificationJob = viewScope.launch {
            repo.activeNotifications.collect {
                postInvalidate()
            }
        }
    }

    fun setMediaRepository(repo: MediaRepository) {
        this.mediaRepo = repo
        repo.initSessionListener()
    }

    fun stopRendering() {
        isRendering = false
        redrawHandler.removeCallbacksAndMessages(null)
        notificationJob?.cancel()
        notificationJob = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRendering = true
        redrawHandler.removeCallbacksAndMessages(null)
        redrawHandler.postDelayed(redrawRunnable, redrawIntervalMs)
        observeNotificationFlow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRendering()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val theme = currentTheme ?: return false
        val musicElement = theme.elements.firstOrNull { it.type == AODElementType.MUSIC && it.visibility }
            ?: return false

        val mediaState = mediaRepo?.mediaState?.value
        if (mediaState == null || !mediaState.hasActiveMedia) {
            return false
        }

        val canvasW = if (theme.canvas.width > 0) theme.canvas.width.toFloat() else 1080f
        val canvasH = if (theme.canvas.height > 0) theme.canvas.height.toFloat() else 2400f

        val scaleX = if (width > 0) width.toFloat() / canvasW else 1f
        val scaleY = if (height > 0) height.toFloat() / canvasH else 1f
        val uniformScale = min(scaleX, scaleY)
        val effectiveScale = uniformScale * musicElement.scale

        val offsetX = (width.toFloat() - (canvasW * uniformScale)) / 2f
        val offsetY = (height.toFloat() - (canvasH * uniformScale)) / 2f

        val currentTimeMs = System.currentTimeMillis()
        val burnInOffset = burnInManager.calculateOffset(currentTimeMs)

        val drawX = offsetX + (musicElement.x + burnInOffset.offsetX) * uniformScale
        val drawY = offsetY + (musicElement.y + burnInOffset.offsetY) * uniformScale

        val touchX = event.x
        val touchY = event.y

        // 1. Timeline / Seekbar Wave Touch & Scrubbing
        val waveStartX = drawX - 160f * effectiveScale
        val waveWidth = 320f * effectiveScale
        val waveY = drawY + 14f * effectiveScale

        val isTouchNearWave = abs(touchY - waveY) <= 40f * effectiveScale &&
                touchX >= waveStartX - 30f * effectiveScale &&
                touchX <= waveStartX + waveWidth + 30f * effectiveScale

        if (isTouchNearWave) {
            val touchFraction = ((touchX - waveStartX) / waveWidth).coerceIn(0f, 1f)
            val durationMs = mediaState.durationMs
            if (durationMs > 0) {
                val targetMs = (touchFraction * durationMs).toLong()
                mediaRepo?.seekTo(targetMs)
                invalidate()
            }
            return true
        }

        // 2. Transport Control Buttons (48dp Minimum Touch Hit Box)
        val btnCenterY = drawY + 48f * effectiveScale
        val touchRadius = 50f * effectiveScale

        if (abs(touchY - btnCenterY) <= touchRadius) {
            val prevBtnX = drawX - 70f * effectiveScale
            val playBtnX = drawX
            val nextBtnX = drawX + 70f * effectiveScale

            when {
                abs(touchX - prevBtnX) <= touchRadius -> {
                    if (event.action == MotionEvent.ACTION_UP) {
                        mediaRepo?.skipToPrevious()
                        invalidate()
                    }
                    return true
                }
                abs(touchX - playBtnX) <= touchRadius -> {
                    if (event.action == MotionEvent.ACTION_UP) {
                        mediaRepo?.playPause()
                        invalidate()
                    }
                    return true
                }
                abs(touchX - nextBtnX) <= touchRadius -> {
                    if (event.action == MotionEvent.ACTION_UP) {
                        mediaRepo?.skipToNext()
                        invalidate()
                    }
                    return true
                }
            }
        }

        return false
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
            notificationsList = notifications ?: emptyList(),
            mediaTitle = media?.title,
            mediaArtist = media?.artist,
            mediaAlbum = media?.album,
            mediaAlbumArt = media?.albumArtBitmap,
            mediaIsPlaying = media?.isPlaying ?: false,
            mediaProgressMs = media?.progressMs ?: 0L,
            mediaDurationMs = media?.durationMs ?: 0L,
            hasActiveMedia = media?.hasActiveMedia ?: false
        )

        renderer.renderTheme(canvas, theme, renderContext)
    }
}

