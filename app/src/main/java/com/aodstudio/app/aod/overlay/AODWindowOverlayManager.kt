package com.aodstudio.app.aod.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.domain.model.AODTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WindowManager overlay controller for Vivo OriginOS 6 / Android 16.
 * Attaches custom AODRenderView to system WindowManager using TYPE_APPLICATION_OVERLAY
 * with AMOLED black canvas and lockscreen flags.
 */
@Singleton
class AODWindowOverlayManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private var windowManager: WindowManager? = null
    private var overlayView: AODRenderView? = null
    private var isOverlayShowing = false

    fun showOverlay(theme: AODTheme) {
        if (isOverlayShowing) {
            overlayView?.setTheme(theme)
            return
        }

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val renderView = AODRenderView(context).apply {
                setTheme(theme)
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            windowManager?.addView(renderView, params)
            overlayView = renderView
            isOverlayShowing = true
        } catch (e: Exception) {
            isOverlayShowing = false
        }
    }

    fun hideOverlay() {
        if (!isOverlayShowing) return
        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
            isOverlayShowing = false
        } catch (e: Exception) {
            isOverlayShowing = false
        }
    }

    fun isShowing(): Boolean = isOverlayShowing
}
