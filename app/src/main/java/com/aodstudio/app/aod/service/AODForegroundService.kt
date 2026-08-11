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
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
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
 * Foreground Service for persistent Always-On Display activation.
 * Manages screen off/on state lifecycle:
 *   - Screen OFF (ACTION_SCREEN_OFF): Acquires WakeLock & shows custom AOD overlay.
 *   - Screen ON / Unlocked (ACTION_SCREEN_ON / ACTION_USER_PRESENT): Releases WakeLock & hides overlay instantly.
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
    private var wakeLock: PowerManager.WakeLock? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "ACTION_SCREEN_OFF — acquiring WakeLock & showing AOD overlay")
                    acquireWakeLock()
                    serviceScope.launch {
                        val activeThemeResult = getThemesUseCase.getActiveTheme()
                        if (activeThemeResult is Result.Success) {
                            overlayManager.showOverlay(activeThemeResult.data)
                        }
                    }
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    Log.d(TAG, "ACTION_SCREEN_ON / USER_PRESENT — releasing WakeLock & hiding AOD overlay")
                    releaseWakeLock()
                    overlayManager.hideOverlay()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()
        registerScreenReceiver()
        startPocketDetection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        pocketSensorManager.stopListening()
        unregisterReceiver(screenReceiver)
        overlayManager.hideOverlay()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "AODStudio:ScreenOffAODWakeLock"
            )
        }
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(30 * 60 * 1000L)
            Log.d(TAG, "WakeLock acquired")
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d(TAG, "WakeLock released")
        }
    }

    private fun startPocketDetection() {
        pocketSensorManager.startListening { isInPocket ->
            if (isInPocket) {
                Log.d(TAG, "Device in pocket — hiding AOD overlay")
                overlayManager.hideOverlay()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AOD Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps AOD Studio active"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AOD Studio Active")
            .setContentText("Always-On Display service is running")
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
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
        Log.d(TAG, "Foreground service notification started")
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
        Log.d(TAG, "Screen state receiver registered")
    }

    companion object {
        private const val TAG = "AODForegroundService"
        const val CHANNEL_ID = "aod_service_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, AODForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AODForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
