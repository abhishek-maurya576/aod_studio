package com.aodstudio.app.aod.compatibility

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vivo / OriginOS 6 compatibility adapter providing explicit intents for
 * Vivo-specific autostart management and background popup permissions.
 */
@Singleton
class VivoAdapter @Inject constructor() : DeviceCompatibility {

    override val isVivoDevice: Boolean
        get() = Build.MANUFACTURER.contains("vivo", ignoreCase = true) ||
                Build.BRAND.contains("vivo", ignoreCase = true) ||
                Build.BRAND.contains("iqoo", ignoreCase = true)

    override fun getAutostartIntent(context: Context): Intent? {
        if (!isVivoDevice) return null

        val intents = listOf(
            Intent().apply {
                component = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            },
            Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            },
            Intent().apply {
                component = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.safeguard.PurifyActivity"
                )
            }
        )

        for (intent in intents) {
            if (context.packageManager.resolveActivity(intent, 0) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return intent
            }
        }
        return null
    }

    override fun getBackgroundPopupIntent(context: Context): Intent? {
        if (!isVivoDevice) return null

        val intent = Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
            )
            putExtra("packagename", context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return if (context.packageManager.resolveActivity(intent, 0) != null) {
            intent
        } else null
    }
}
