package com.aodstudio.app.aod.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WindowManager overlay controller for AOD rendering.
 * Attaches AODRenderView to system window using TYPE_APPLICATION_OVERLAY
 * with FLAG_SHOW_WHEN_LOCKED, FLAG_TURN_SCREEN_ON, and FLAG_KEEP_SCREEN_ON at 0.01f brightness
 * for screen-off AMOLED rendering.
 */
@Singleton
class AODWindowOverlayManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val batteryRepository: BatteryRepository,
    private val notificationRepository: NotificationRepository,
    private val mediaRepository: MediaRepository
) {
    companion object {
        private const val TAG = "AODOverlayManager"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: AODRenderView? = null
    private var isOverlayShowing = false

    @Suppress("DEPRECATION")
    fun showOverlay(theme: AODTheme) {
        if (isOverlayShowing) {
            overlayView?.setTheme(theme)
            return
        }

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    Log.d(TAG, "Double tap detected on AOD overlay — exiting overlay")
                    hideOverlay()
                    return true
                }
            })

            val renderView = AODRenderView(context).apply {
                setBatteryRepository(batteryRepository)
                setNotificationRepository(notificationRepository)
                setMediaRepository(mediaRepository)
                setTheme(theme)

                setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    true
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                screenBrightness = 0.01f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            windowManager?.addView(renderView, params)
            overlayView = renderView
            isOverlayShowing = true
            Log.d(TAG, "AOD overlay shown successfully with theme: ${theme.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay", e)
            isOverlayShowing = false
        }
    }

    fun hideOverlay() {
        if (!isOverlayShowing) return
        try {
            overlayView?.stopRendering()
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
            isOverlayShowing = false
            Log.d(TAG, "AOD overlay hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide overlay", e)
            overlayView = null
            isOverlayShowing = false
        }
    }

    fun isShowing(): Boolean = isOverlayShowing
}
