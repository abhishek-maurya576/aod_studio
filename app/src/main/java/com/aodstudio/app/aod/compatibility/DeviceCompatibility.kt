package com.aodstudio.app.aod.compatibility

import android.content.Context
import android.content.Intent

/**
 * Interface defining OEM-specific adaptation rules and helper intents.
 */
interface DeviceCompatibility {
    val isVivoDevice: Boolean
    fun getAutostartIntent(context: Context): Intent?
    fun getBackgroundPopupIntent(context: Context): Intent?
}
