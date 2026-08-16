package com.aodstudio.app.aod.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AODActivity — Fullscreen Lockscreen Activity with showWhenLocked=true.
 *
 * ## WHY AN ACTIVITY INSTEAD OF SERVICE OVERLAY FOR LOCK SCREEN?
 * Android security rules (API 26+ through Android 16) explicitly prevent
 * [WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY] windows created from a Service
 * from displaying on top of the system Keyguard (Lock Screen).
 *
 * To render on top of the locked screen, Android REQUIRES an Activity with
 * `setShowWhenLocked(true)` and `setTurnScreenOn(true)`.
 *
 * When launched on screen-off, this Activity covers the screen with pure black (#000000),
 * requests minimum brightness (0.01f), and draws the AOD clock. Double-tapping or
 * unlocking authenticates the user and finishes this Activity.
 */
@AndroidEntryPoint
class AODActivity : ComponentActivity() {

    @Inject
    lateinit var getThemesUseCase: GetThemesUseCase

    @Inject
    lateinit var batteryRepository: BatteryRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var mediaRepository: MediaRepository

    @Inject
    lateinit var settingsRepository: com.aodstudio.app.domain.repository.SettingsRepository

    private var renderView: AODRenderView? = null

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                Log.d(TAG, "ACTION_USER_PRESENT received — finishing AODActivity")
                dismissAod()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureWindow()

        val gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    return if (settingsRepository.getDoubleTapToExitSync()) {
                        Log.d(TAG, "Double-tap detected on AODActivity — dismissing (setting enabled)")
                        dismissAod()
                        true
                    } else {
                        Log.d(TAG, "Double-tap detected on AODActivity — ignored (setting disabled)")
                        false
                    }
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    Log.d(TAG, "Swipe/fling detected on AODActivity — dismissing to reveal keyguard")
                    dismissAod()
                    return true
                }
            }
        )

        renderView = AODRenderView(this).apply {
            setBatteryRepository(batteryRepository)
            setNotificationRepository(notificationRepository)
            setMediaRepository(mediaRepository)
            setBackgroundColor(Color.BLACK)
            onFingerprintTouch = {
                Log.d(TAG, "Fingerprint zone touched on AODActivity — dismissing instantly to Keyguard")
                dismissAod()
            }
            setOnTouchListener { view, event ->
                val handledByRenderView = onTouchEvent(event)
                if (handledByRenderView) {
                    true
                } else {
                    val isFodZone = event.x in (view.width * 0.35f)..(view.width * 0.65f) &&
                            event.y in (view.height * 0.70f)..(view.height * 0.90f)
                    if (isFodZone && event.action == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "Hardware FOD zone touched — dismissing AOD instantly for native optical scan")
                        dismissAod()
                        true
                    } else {
                        gestureDetector.onTouchEvent(event)
                        true
                    }
                }
            }
        }

        setContentView(renderView)
        loadActiveTheme()

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(userPresentReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(userPresentReceiver, filter)
        }

        Log.i(TAG, "AODActivity created with showWhenLocked=true")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureWindow()
        loadActiveTheme()
    }

    override fun onResume() {
        super.onResume()
        configureWindow()
        loadActiveTheme()
    }

    private fun configureWindow() {
        // Enable showWhenLocked and turnScreenOn natively
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Keep screen on and set dim brightness
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val params = window.attributes
        params.screenBrightness = 0.01f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = params

        // Edge-to-edge layout, hide status & navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun loadActiveTheme() {
        lifecycleScope.launch {
            when (val result = getThemesUseCase.getActiveTheme()) {
                is Result.Success -> {
                    renderView?.setTheme(result.data)
                }
                else -> Log.e(TAG, "Failed to load active theme for AODActivity")
            }
        }
    }

    private fun dismissAod() {
        finishAndRemoveTask()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        renderView?.stopRendering()
        try {
            unregisterReceiver(userPresentReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver unregister error: ${e.message}")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent accidental back button dismissal unless double tapped or swiped
        dismissAod()
    }

    companion object {
        private const val TAG = "AODActivity"
    }
}
