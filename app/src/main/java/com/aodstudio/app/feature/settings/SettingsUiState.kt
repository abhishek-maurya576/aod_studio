package com.aodstudio.app.feature.settings

/**
 * UI State for the Settings screen.
 * Tracks all permission states, service running status, and user feedback messages.
 */
data class SettingsUiState(
    val isAodEnabled: Boolean = false,
    val isServiceActuallyRunning: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isBatteryOptimizationExempt: Boolean = false,
    val burnInShiftIntervalMinutes: Int = 5,
    val userMessage: String? = null
)
