package com.aodstudio.app.aod.lifecycle

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.aodstudio.app.aod.service.AODForegroundService
import com.aodstudio.app.config.AppConfig

/**
 * ServiceWatchdog — AlarmManager-based restart mechanism for AODForegroundService.
 *
 * OriginOS aggressively kills background processes. START_STICKY alone is insufficient
 * because OriginOS freezes the entire app process rather than just the service, preventing
 * Android's normal service-restart machinery from running. An AlarmManager alarm with
 * ALLOW_WHILE_IDLE survives OEM freezing because it is dispatched by the system's own
 * alarm subsystem, not the app process.
 *
 * Usage:
 *   - [schedule] is called from [AODForegroundService.onTaskRemoved] to arm the watchdog.
 *   - [cancel] is called from [AODForegroundService.onDestroy] for clean shutdown.
 *   - When the alarm fires, [onReceive] attempts to restart [AODForegroundService].
 */
class ServiceWatchdog : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        Log.i(TAG, "Watchdog alarm fired — restarting AODForegroundService")
        AODForegroundService.startService(context)
    }

    companion object {
        private const val TAG = "ServiceWatchdog"
        private const val REQUEST_CODE = 9001

        /**
         * Arms a one-shot AlarmManager alarm using [AlarmManager.setExactAndAllowWhileIdle].
         * The alarm fires [AppConfig.Service.WATCHDOG_ALARM_INTERVAL_MS] from now and
         * triggers [ServiceWatchdog.onReceive] to restart the service.
         *
         * Called from [AODForegroundService.onTaskRemoved].
         */
        fun schedule(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val pendingIntent = buildPendingIntent(context)
                val triggerAtMs = SystemClock.elapsedRealtime() + AppConfig.Service.WATCHDOG_ALARM_INTERVAL_MS

                val canUseExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

                if (canUseExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerAtMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerAtMs,
                        pendingIntent
                    )
                }
                Log.i(TAG, "Watchdog scheduled in ${AppConfig.Service.WATCHDOG_ALARM_INTERVAL_MS / 1000}s (exact=$canUseExact)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule watchdog alarm: ${e.message}")
            }
        }

        /**
         * Cancels any pending watchdog alarm. Call on clean service shutdown to prevent
         * spurious restarts after the user intentionally disables AOD.
         */
        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(buildPendingIntent(context))
            Log.i(TAG, "Watchdog alarm cancelled")
        }

        private fun buildPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ServiceWatchdog::class.java)
            // FLAG_IMMUTABLE required on API 31+; FLAG_UPDATE_CURRENT ensures we replace
            // any existing alarm rather than stacking duplicates.
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
        }
    }
}
