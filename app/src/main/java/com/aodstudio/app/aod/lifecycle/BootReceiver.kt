package com.aodstudio.app.aod.lifecycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aodstudio.app.aod.service.AODForegroundService

/**
 * BootReceiver — starts [AODForegroundService] automatically after device reboot.
 *
 * Requires RECEIVE_BOOT_COMPLETED permission in AndroidManifest.xml.
 *
 * goAsync() pattern: [BroadcastReceiver.onReceive] has a 10-second wall-clock deadline
 * on newer Android versions (and especially strict OEM variants like OriginOS). Using
 * [goAsync] extends the window and ensures startForegroundService() can complete
 * without triggering an ANR on slower boot sequences.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context ?: return

        // goAsync() prevents Android from recycling this BroadcastReceiver object
        // before startForegroundService() has been dispatched to the system.
        val pendingResult = goAsync()
        try {
            val prefs = context.getSharedPreferences("aod_settings_prefs", Context.MODE_PRIVATE)
            val isAodEnabled = prefs.getBoolean("aod_master_enabled", false)

            if (isAodEnabled) {
                Log.i(TAG, "ACTION_BOOT_COMPLETED received & AOD enabled — starting AODForegroundService")
                AODForegroundService.startService(context)
            } else {
                Log.i(TAG, "ACTION_BOOT_COMPLETED received but AOD is disabled by user — skipping service start")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to evaluate or start AODForegroundService on boot: ${e.message}")
        } finally {
            // Must call finish() to signal the system that async processing is done.
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
