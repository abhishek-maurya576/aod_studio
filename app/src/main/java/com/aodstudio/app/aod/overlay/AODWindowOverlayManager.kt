package com.aodstudio.app.aod.overlay

import android.content.Context
import android.graphics.Color
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
 * AODWindowOverlayManager — attaches a full-screen TYPE_APPLICATION_OVERLAY window
 * that renders the AOD canvas while the device is in the "dim-wakelock" state.
 *
 * ## Why PixelFormat.OPAQUE + screenBrightness = 0.0f?
 *   - OPAQUE tells the compositor to composite this layer as opaque, so the system can
 *     skip blending with layers below — slightly lower GPU overhead.
 *   - Background is pure #000000 (AMOLED true-black). On AMOLED panels, black pixels are
 *     physically turned off at the subpixel level, which minimizes power draw for the
 *     portions of the display that show no content.
 *   - screenBrightness = 0.0f is a per-window override (distinct from system brightness).
 *     It requests the display backlight at minimum level while this window is in front.
 *     NOTE: this is NOT true AMOLED doze — the entire panel is still powered. AMOLED black
 *     only saves power per-pixel, not at the panel driver level.
 *
 * ## FLAG_DISMISS_KEYGUARD deprecation (API 26+):
 *   The correct API 27+ replacement is KeyguardManager.requestDismissKeyguard(Activity, cb).
 *   However, that requires an Activity reference, which a Service-context window manager
 *   does not have. We therefore set the flag on LayoutParams as a best-effort signal and
 *   accept that on some API 27+ builds it may be silently ignored.
 *   VERIFY ON DEVICE: whether the keyguard auto-dismisses on Vivo T4 Pro (OriginOS 6).
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

    @Volatile
    private var isOverlayShowing = false

    // Keep a reference to the current LayoutParams so we can update screenBrightness
    // without removing/re-adding the view (which causes a visible flicker).
    private var overlayParams: WindowManager.LayoutParams? = null

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Attaches the AOD overlay window. If already showing, updates the theme without
     * removing and re-adding the view (avoids a visible flicker).
     */
    @Suppress("DEPRECATION")
    fun showOverlay(theme: AODTheme) {
        if (isOverlayShowing) {
            overlayView?.setTheme(theme)
            Log.d(TAG, "Overlay already showing — theme updated to: ${theme.name}")
            return
        }

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            // Double-tap gesture detector: lets the user dismiss the AOD overlay without
            // pressing the power button, which would stop our FULL_WAKE_LOCK.
            val gestureDetector = GestureDetector(
                context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        Log.d(TAG, "Double-tap detected — dismissing AOD overlay")
                        hideOverlay()
                        return true
                    }
                }
            )

            val renderView = AODRenderView(context).apply {
                setBatteryRepository(batteryRepository)
                setNotificationRepository(notificationRepository)
                setMediaRepository(mediaRepository)
                setTheme(theme)
                // True-black background: on AMOLED, these pixels draw zero current.
                setBackgroundColor(Color.BLACK)

                setOnTouchListener { view, event ->
                    val handledByRenderView = onTouchEvent(event)
                    if (handledByRenderView) {
                        true
                    } else {
                        gestureDetector.onTouchEvent(event)
                    }
                }
            }

            val params = buildLayoutParams()
            windowManager?.addView(renderView, params)
            overlayView = renderView
            overlayParams = params
            isOverlayShowing = true
            Log.i(TAG, "AOD overlay attached — theme: ${theme.name}, " +
                    "screenBrightness=${params.screenBrightness}, format=OPAQUE")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach overlay window", e)
            overlayView = null
            overlayParams = null
            isOverlayShowing = false
        }
    }

    /**
     * Removes the AOD overlay window. Idempotent — safe to call multiple times.
     * Stops the 1Hz render loop before removing the view to prevent Handler leaks.
     */
    fun hideOverlay() {
        if (!isOverlayShowing) return
        try {
            overlayView?.stopRendering()
            overlayView?.let { windowManager?.removeView(it) }
            Log.d(TAG, "AOD overlay removed")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay window (may already be detached): ${e.message}")
        } finally {
            overlayView = null
            overlayParams = null
            isOverlayShowing = false
        }
    }

    fun isShowing(): Boolean = isOverlayShowing

    // ──────────────────────────────────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun buildLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // FLAG_KEEP_SCREEN_ON: window-level enforcement — belt-and-suspenders with FULL_WAKE_LOCK.
            // FLAG_SHOW_WHEN_LOCKED: show over lock screen (required for AOD on lock screen).
            // FLAG_TURN_SCREEN_ON: wake display if it went to STATE_OFF during the race window.
            // FLAG_DISMISS_KEYGUARD: deprecated API 26+ best-effort signal.
            // FLAG_LAYOUT_IN_SCREEN + FLAG_FULLSCREEN: true edge-to-edge rendering.
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
            // OPAQUE: skip compositor blending with layers below.
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // screenBrightness: per-window override. 0.01f = minimum visible level.
            // NOTE: 0.0f maps to "use system default" on some OriginOS builds —
            // use 0.01f which is the true minimum override value (virtually invisible
            // backlight but not the ambiguous zero sentinel).
            screenBrightness = 0.01f

            // Cover display cutout (notch) for Vivo T4 Pro.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }
}
