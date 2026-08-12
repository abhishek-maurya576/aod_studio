package com.aodstudio.app.aod.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aodstudio.app.aod.lifecycle.ServiceWatchdog
import com.aodstudio.app.aod.overlay.AODWindowOverlayManager
import com.aodstudio.app.aod.sensor.PocketSensorManager
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AODForegroundService — core engine of the "fake AOD" mechanism.
 *
 * Architecture note (why NOT JobIntentService / WorkManager):
 *   OriginOS 6 freezes WorkManager/JobScheduler workers within ~2 minutes of screen-off
 *   because it treats them as lower-priority background work. A started Service with
 *   startForeground() is classified by OriginOS as a user-visible process and is kept alive
 *   much longer (still requires OEM whitelist steps — see VivoOnboardingScreen).
 *
 * Screen-off lifecycle:
 *   ACTION_SCREEN_OFF  → acquire SCREEN_DIM_WAKE_LOCK + show TYPE_APPLICATION_OVERLAY window
 *   ACTION_SCREEN_ON   → cancel overlay immediately (before keyguard) + release wakelock
 *   ACTION_USER_PRESENT → same cleanup (double-guard against races)
 *
 * WakeLock note:
 *   SCREEN_DIM_WAKE_LOCK is deprecated since API 17 but still functional as a behavioral
 *   hint on most OEM skins including OriginOS. Combined with ACQUIRE_CAUSES_WAKEUP and
 *   ON_AFTER_RELEASE it requests the display stay at minimum brightness rather than STATE_OFF.
 *   VERIFY ON DEVICE: on some OriginOS builds this is silently demoted to a CPU-only wakelock.
 *   The log statement in acquireWakeLock() will help confirm which path is active.
 */
@AndroidEntryPoint
class AODForegroundService : Service() {

    @Inject
    lateinit var overlayManager: AODWindowOverlayManager

    @Inject
    lateinit var getThemesUseCase: GetThemesUseCase

    @Inject
    lateinit var pocketSensorManager: PocketSensorManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Held during the screen-off AOD window. Released on screen-on or user unlock.
    private var wakeLock: PowerManager.WakeLock? = null

    // Main-thread Handler used to sequence USER_PRESENT cleanup before keyguard dismissal.
    private val mainHandler = Handler(Looper.getMainLooper())

    // ──────────────────────────────────────────────────────────────────────────
    // BroadcastReceiver — screen state events
    // Note: ACTION_SCREEN_OFF / ACTION_SCREEN_ON cannot be declared in the manifest
    // since API 26. They MUST be registered dynamically from a running Service/Activity.
    // ──────────────────────────────────────────────────────────────────────────
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> onScreenOff()
                Intent.ACTION_SCREEN_ON  -> onScreenOn()
                // USER_PRESENT fires after the keyguard is fully dismissed.
                // We still handle it as a fallback in case SCREEN_ON was missed.
                Intent.ACTION_USER_PRESENT -> onUserPresent()
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()
        registerScreenReceiver()
        startPocketDetection()
        Log.i(TAG, "AODForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY alone is insufficient on OriginOS — the watchdog supplements it.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "AODForegroundService destroyed — performing clean shutdown")
        // Cancel the watchdog alarm on clean shutdown (user explicitly disabled AOD).
        // onTaskRemoved arms it; onDestroy cancels it, so the alarm only fires if the OS
        // killed us without calling onDestroy (which is what OriginOS does from recents).
        ServiceWatchdog.cancel(this)
        releaseWakeLock()
        pocketSensorManager.stopListening()
        try { unregisterReceiver(screenReceiver) } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Receiver already unregistered: ${e.message}")
        }
        overlayManager.hideOverlay()
        serviceScope.cancel()
    }

    /**
     * Called when the user swipes the app from recents — or when OriginOS kills the process
     * from the recents memory manager. START_STICKY will try to restart, but OriginOS can
     * suppress it. The AlarmManager watchdog fires after [AppConfig.Service.WATCHDOG_ALARM_INTERVAL_MS]
     * as a fallback restart path that bypasses OEM process freezing.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.w(TAG, "onTaskRemoved — arming AlarmManager watchdog as OriginOS fallback restart")
        ServiceWatchdog.schedule(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ──────────────────────────────────────────────────────────────────────────
    // Screen state handlers
    // ──────────────────────────────────────────────────────────────────────────

    private fun onScreenOff() {
        Log.d(TAG, "ACTION_SCREEN_OFF — acquiring WakeLock & launching AODActivity (showWhenLocked)")
        acquireWakeLock()

        // 1. Launch AODActivity: Has showWhenLocked=true which Android OS requires
        // to render above the locked Keyguard on Android 8+ / Android 16.
        try {
            val aodIntent = Intent(this, com.aodstudio.app.aod.ui.AODActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(aodIntent)
            Log.i(TAG, "AODActivity launched successfully on SCREEN_OFF")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch AODActivity: ${e.message}", e)
        }

        // 2. Also attach overlayManager as a secondary fallback
        serviceScope.launch {
            val activeThemeResult = getThemesUseCase.getActiveTheme()
            if (activeThemeResult is Result.Success) {
                overlayManager.showOverlay(activeThemeResult.data)
            } else {
                Log.e(TAG, "No active theme — cannot show AOD overlay")
            }
        }
    }

    private fun onScreenOn() {
        // DO NOT hide the overlay on SCREEN_ON.
        // On OriginOS 6, SCREEN_DIM_WAKE_LOCK is silently demoted (confirmed from logcat).
        // The display goes to STATE_OFF despite the lock, then when the user presses power
        // to wake it, SCREEN_ON fires. At this point the overlay SHOULD be visible (it is
        // our AOD clock face on the lock screen). We only dismiss it in onUserPresent(),
        // which fires after the user authenticates and the keyguard is fully dismissed.
        //
        // If we call hideOverlay() here, the overlay is destroyed as soon as the user
        // wakes the screen, making the AOD flash for a frame then disappear.
        Log.d(TAG, "ACTION_SCREEN_ON — display woke (overlay stays visible on lock screen)")
        // Re-arm the wakelock so the display stays on while showing the AOD lock screen.
        acquireWakeLock()
    }

    private fun onUserPresent() {
        Log.d(TAG, "ACTION_USER_PRESENT — user unlocked, removing AOD overlay")
        // USER_PRESENT fires after the keyguard is fully dismissed (biometric/PIN/pattern).
        // This is the correct point to remove the overlay — AFTER the user has authenticated,
        // not on SCREEN_ON which fires while the lock screen is still showing.
        // postAtFrontOfQueue beats any pending UI frames to avoid a black-flash artifact.
        mainHandler.postAtFrontOfQueue {
            overlayManager.hideOverlay()
            releaseWakeLock()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WakeLock management
    // ──────────────────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            // DIAGNOSIS from logcat (2026-08-12):
            //   SCREEN_DIM_WAKE_LOCK is confirmed a NO-OP on OriginOS 6 / Vivo T4 Pro.
            //   isHeld returns true but the display goes to STATE_OFF regardless.
            //   Switching to FULL_WAKE_LOCK which forces display to STATE_ON.
            //
            // FULL_WAKE_LOCK: keeps screen fully on (not just CPU). Deprecated since API 17
            // but still functional on most OEM builds including OriginOS 6 as of Android 16.
            // ACQUIRE_CAUSES_WAKEUP: wakes the screen if it's already gone to STATE_OFF
            //   during the race window between power-button press and broadcast delivery.
            // ON_AFTER_RELEASE: user activity hint so screen doesn't immediately re-dim
            //   after we release the lock during USER_PRESENT cleanup.
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                WAKELOCK_TAG
            )
        }
        if (wakeLock?.isHeld == false) {
            // 30-minute maximum timeout. releaseWakeLock() called on USER_PRESENT.
            wakeLock?.acquire(30 * 60 * 1000L)
            if (wakeLock?.isHeld == true) {
                Log.i(TAG, "FULL_WAKE_LOCK acquired — display forced to STATE_ON")
            } else {
                Log.e(TAG, "FULL_WAKE_LOCK acquire() returned but isHeld==false — " +
                        "PowerManager rejected this wakelock type. FLAG_KEEP_SCREEN_ON " +
                        "on the overlay window is the last fallback.")
            }
        }
    }

    private fun releaseWakeLock() {
        // Guard against double-release which throws IllegalStateException.
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d(TAG, "WakeLock released")
        }
        // Null out the reference so acquireWakeLock() will rebuild it fresh next time.
        wakeLock = null
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Pocket detection
    // ──────────────────────────────────────────────────────────────────────────

    private fun startPocketDetection() {
        pocketSensorManager.startListening { isInPocket ->
            if (isInPocket) {
                Log.d(TAG, "Device in pocket — suppressing AOD overlay via proximity sensor")
                overlayManager.hideOverlay()
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Notification channel & foreground notification
    // ──────────────────────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        // IMPORTANCE_MIN: notification is silent, collapsed by default, does NOT appear
        // in the status bar. This is the least intrusive level that still keeps the service
        // classified as foreground. The user cannot swipe away an ongoing notification,
        // preventing accidental service death.
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AOD Service",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Keeps AOD Studio active in the background"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun startForegroundServiceNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AOD Studio Active")
            .setContentText("Always-On Display is running")
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            // setOngoing(true) prevents the user from swiping the notification away,
            // which would kill the foreground service.
            .setOngoing(true)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        Log.d(TAG, "Foreground service notification started (IMPORTANCE_MIN, ongoing)")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Receiver registration
    // ──────────────────────────────────────────────────────────────────────────

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        // RECEIVER_NOT_EXPORTED: these are internal-only intents from the system.
        // Required on API 34+ to prevent other apps from spoofing screen-state broadcasts.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(screenReceiver, filter)
        }
        Log.d(TAG, "Screen state receiver registered dynamically (NOT in manifest — API 26+ requirement)")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Static helpers
    // ──────────────────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "AODForegroundService"
        const val CHANNEL_ID = "aod_service_channel"
        const val NOTIFICATION_ID = 1001
        // WakeLock tag must be "<package>:<tag>" format per PowerManager documentation.
        private const val WAKELOCK_TAG = "AODStudio:DimLock"

        fun startService(context: Context) {
            val intent = Intent(context, AODForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.i(TAG, "startService() called")
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AODForegroundService::class.java)
            context.stopService(intent)
            Log.i(TAG, "stopService() called")
        }
    }
}
