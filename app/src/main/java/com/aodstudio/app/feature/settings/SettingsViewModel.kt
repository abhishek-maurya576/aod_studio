package com.aodstudio.app.feature.settings

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.aodstudio.app.aod.service.AODForegroundService
import com.aodstudio.app.notification.service.AODNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 * Manages permission checks, AOD service toggle, battery optimization, and notification listener.
 * Checks REAL service running state (not just a toggle boolean).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
    }

    /**
     * Check all permission states and actual service running status.
     * Called on init and when returning from system settings screens.
     */
    fun checkPermissions() {
        val hasOverlay = Settings.canDrawOverlays(context)

        val hasNotificationListener = isNotificationListenerEnabled()

        val isBatteryExempt = isBatteryOptimizationExempt()

        val isServiceRunning = isServiceRunning(AODForegroundService::class.java)

        _uiState.update {
            it.copy(
                hasOverlayPermission = hasOverlay,
                hasNotificationPermission = hasNotificationListener,
                isBatteryOptimizationExempt = isBatteryExempt,
                isAodEnabled = isServiceRunning,
                isServiceActuallyRunning = isServiceRunning
            )
        }
    }

    fun toggleAodService(enable: Boolean) {
        if (enable) {
            if (!_uiState.value.hasOverlayPermission) {
                _uiState.update { it.copy(userMessage = "System Overlay permission required. Grant it first.") }
                return
            }
            AODForegroundService.startService(context)
            _uiState.update {
                it.copy(
                    isAodEnabled = true,
                    isServiceActuallyRunning = true,
                    userMessage = "AOD Service Started"
                )
            }
        } else {
            AODForegroundService.stopService(context)
            _uiState.update {
                it.copy(
                    isAodEnabled = false,
                    isServiceActuallyRunning = false,
                    userMessage = "AOD Service Stopped"
                )
            }
        }
    }

    fun openOverlaySettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openNotificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openBatteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /**
     * Check if our NotificationListenerService is enabled in system settings.
     */
    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val componentName = ComponentName(context, AODNotificationListenerService::class.java)
        return enabledListeners.contains(componentName.flattenToString())
    }

    /**
     * Check if our app is exempt from battery optimization (unrestricted background).
     */
    private fun isBatteryOptimizationExempt(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    /**
     * Check if a service is actually running — not just a saved toggle state.
     */
    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}
