package com.aodstudio.app.aod.compatibility

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VivoAdapter — OriginOS / FuntouchOS OEM compatibility layer.
 *
 * Provides deep-link intents for Vivo-specific system settings screens that are required
 * for [AODForegroundService] survival on OriginOS 6. Each intent is wrapped in a try/catch
 * with a graceful fallback to the standard App Info settings page.
 *
 * ## UNVERIFIED INTENT STRINGS
 * Vivo changes activity class names across OriginOS versions (5 vs 6 vs FuntouchOS 13).
 * Every Vivo-specific component name below is marked // UNVERIFIED and must be tested
 * on the physical Vivo T4 Pro running OriginOS 6 before relying on it.
 *
 * Resolution strategy for each intent:
 *   1. Try OriginOS 6 primary component
 *   2. Try OriginOS 5 / FuntouchOS fallback component
 *   3. Try iQOO variant (different package, same screen)
 *   4. Fall back to Settings.ACTION_APPLICATION_DETAILS_SETTINGS (always works)
 */
@Singleton
class VivoAdapter @Inject constructor() : DeviceCompatibility {

    companion object {
        private const val TAG = "VivoAdapter"
    }

    override val isVivoDevice: Boolean
        get() = Build.MANUFACTURER.contains("vivo", ignoreCase = true) ||
                Build.BRAND.contains("vivo", ignoreCase = true) ||
                Build.BRAND.contains("iqoo", ignoreCase = true) ||
                Build.MANUFACTURER.contains("bbk", ignoreCase = true) // Parent company check

    // ──────────────────────────────────────────────────────────────────────────
    // Autostart / Background Startup Manager
    // iManager > Apps > Autostart
    // ──────────────────────────────────────────────────────────────────────────

    override fun getAutostartIntent(context: Context): Intent? {
        if (!isVivoDevice) return null

        val candidates = listOf(
            // UNVERIFIED — OriginOS 6 primary: iManager via com.iqoo.secure
            Intent().apply {
                component = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            },
            // UNVERIFIED — OriginOS 5 / FuntouchOS 13 path via vivo permission manager
            Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            },
            // UNVERIFIED — iQOO variant (different package, same UI pattern)
            Intent().apply {
                component = ComponentName(
                    "com.bbk.iqoo.secure",
                    "com.bbk.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            },
            // UNVERIFIED — Older FuntouchOS autostart path
            Intent().apply {
                component = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.safeguard.PurifyActivity"
                )
            }
        )

        return resolveFirstOrAppInfo(context, candidates, "getAutostartIntent")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Background Popup / Overlay Permission
    // Battery > Background Activity Management
    // ──────────────────────────────────────────────────────────────────────────

    override fun getBackgroundPopupIntent(context: Context): Intent? {
        if (!isVivoDevice) return null

        val candidates = listOf(
            // UNVERIFIED — OriginOS 6: Permission Manager soft permission detail screen
            Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
                )
                putExtra("packagename", context.packageName)
            },
            // UNVERIFIED — Alternative path seen on some OriginOS 5 builds
            Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.PurviewTabActivity"
                )
                putExtra("packagename", context.packageName)
            }
        )

        return resolveFirstOrAppInfo(context, candidates, "getBackgroundPopupIntent")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Battery > High Background Power Consumption Whitelist
    // This is SEPARATE from AOSP battery optimization. Vivo maintains its own list.
    // ──────────────────────────────────────────────────────────────────────────

    override fun getBatteryHighBackgroundIntent(context: Context): Intent? {
        if (!isVivoDevice) return null

        val candidates = listOf(
            // UNVERIFIED — com.vivo.abe is the Vivo Application Battery Engine package.
            // PermissionManagerActivity is the screen showing the "High Background Power" list.
            // This string is derived from decompiled OriginOS 6 system APKs — NOT confirmed
            // working on Vivo T4 Pro. Must be tested on device.
            Intent().apply {
                component = ComponentName(
                    "com.vivo.abe",
                    "com.vivo.abe.PermissionManagerActivity"
                )
                putExtra("packagename", context.packageName)
            },
            // UNVERIFIED — Alternative ABE entry point seen on OriginOS 5
            Intent().apply {
                component = ComponentName(
                    "com.vivo.abe",
                    "com.vivo.abe.ui.BatteryManageListActivity"
                )
            },
            // UNVERIFIED — Battery Manager via settings provider on some Vivo builds
            Intent().apply {
                action = "com.vivo.action.BATTERY_APP_MANAGER"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            // Fallback: standard AOSP battery optimization exemption dialog
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )

        return resolveFirstOrAppInfo(context, candidates, "getBatteryHighBackgroundIntent")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // OriginOS version detection
    // ──────────────────────────────────────────────────────────────────────────

    override fun getOriginOsVersion(): String? {
        if (!isVivoDevice) return null
        return try {
            // Vivo exposes OriginOS version as a system property.
            // UNVERIFIED — property name may differ across OriginOS 5 vs 6.
            val cls = Class.forName("android.os.SystemProperties")
            val getMethod = cls.getMethod("get", String::class.java, String::class.java)
            // Try multiple known property keys:
            val version = listOf(
                "ro.vivo.os.version",       // UNVERIFIED — OriginOS 6 candidate
                "ro.vivo.os.build.display", // UNVERIFIED — alternative key
                "ro.build.version.vivo"     // UNVERIFIED — older FuntouchOS key
            ).firstNotNullOfOrNull { key ->
                val value = getMethod.invoke(null, key, "") as String
                value.ifEmpty { null }
            }
            if (version != null) {
                Log.d(TAG, "Detected OriginOS version: $version")
            } else {
                Log.w(TAG, "Could not detect OriginOS version from system properties — VERIFY ON DEVICE")
            }
            version
        } catch (e: Exception) {
            Log.e(TAG, "Error reading OriginOS version via reflection: ${e.message}")
            null
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Tries each candidate intent in order, returns the first one that resolves to an Activity.
     * Falls back to [Settings.ACTION_APPLICATION_DETAILS_SETTINGS] if none resolve,
     * then null if even the fallback fails (should be impossible on any Android device).
     */
    private fun resolveFirstOrAppInfo(
        context: Context,
        candidates: List<Intent>,
        callerTag: String
    ): Intent? {
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (context.packageManager.resolveActivity(intent, 0) != null) {
                    Log.d(TAG, "$callerTag resolved: ${intent.component ?: intent.action}")
                    return intent
                }
            } catch (e: Exception) {
                Log.w(TAG, "$callerTag — candidate threw exception: ${e.message}")
            }
        }
        // Universal fallback: standard App Info page. Always available.
        Log.w(TAG, "$callerTag — no OEM deep-link resolved, falling back to App Info settings")
        return try {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            Log.e(TAG, "$callerTag — even App Info fallback failed: ${e.message}")
            null
        }
    }
}
