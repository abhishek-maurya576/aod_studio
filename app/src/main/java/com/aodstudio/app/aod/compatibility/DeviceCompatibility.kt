package com.aodstudio.app.aod.compatibility

import android.content.Context
import android.content.Intent

/**
 * Interface defining OEM-specific adaptation rules and helper intents.
 * Each method returns null if the device is not the target OEM or if the
 * specific deep-link does not resolve on this OEM firmware version.
 * Callers must always handle the null case with a graceful fallback.
 */
interface DeviceCompatibility {

    /** True if the device is a Vivo or iQOO device (OriginOS / FuntouchOS). */
    val isVivoDevice: Boolean

    /**
     * Returns an Intent that deep-links to the OEM autostart / background startup manager.
     * On Vivo this is typically iManager > Autostart Manager.
     * Returns null if not a Vivo device or if no matching activity is installed.
     */
    fun getAutostartIntent(context: Context): Intent?

    /**
     * Returns an Intent that deep-links to the OEM background popup / background
     * activity permission screen (needed for overlay windows from background).
     * Returns null if not a Vivo device or if the deep-link does not resolve.
     */
    fun getBackgroundPopupIntent(context: Context): Intent?

    /**
     * Returns an Intent that deep-links to the OEM "High Background Power Consumption"
     * whitelist screen (Vivo-specific battery management setting).
     * This is DISTINCT from the standard AOSP REQUEST_IGNORE_BATTERY_OPTIMIZATIONS flow.
     * Returns null if not a Vivo device or if the intent does not resolve.
     */
    fun getBatteryHighBackgroundIntent(context: Context): Intent?

    /**
     * Returns the OriginOS / FuntouchOS version string if detectable, null otherwise.
     * Useful for branching intent strategies between OriginOS 5 and OriginOS 6.
     */
    fun getOriginOsVersion(): String?
}
