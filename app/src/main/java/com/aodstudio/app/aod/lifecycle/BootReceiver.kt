package com.aodstudio.app.aod.lifecycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aodstudio.app.aod.service.AODForegroundService

/**
 * BroadcastReceiver listening for ACTION_BOOT_COMPLETED to auto-launch AOD service on boot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let { AODForegroundService.startService(it) }
        }
    }
}
