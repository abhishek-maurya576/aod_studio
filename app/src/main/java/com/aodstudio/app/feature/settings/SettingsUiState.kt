package com.aodstudio.app.feature.settings

/**
 * UI State for the Settings screen.
 * Tracks permission states, service status, hardware toggles, and user preferences.
 */
data class SettingsUiState(
    val isAodEnabled: Boolean = false,
    val isServiceActuallyRunning: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isBatteryOptimizationExempt: Boolean = false,
    val doubleTapToExit: Boolean = true,
    val powerSavingMode: Boolean = true,
    val pocketDetectionEnabled: Boolean = true,
    val burnInProtectionEnabled: Boolean = true,
    val aodTimeoutMinutes: Int = 0,
    val userMessage: String? = null
)
