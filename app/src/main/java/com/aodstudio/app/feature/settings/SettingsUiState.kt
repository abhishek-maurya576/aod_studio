package com.aodstudio.app.feature.settings

/**
 * UI State for the Settings screen.
 */
data class SettingsUiState(
    val isAodEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isBatterySaverExempt: Boolean = false,
    val burnInShiftIntervalMinutes: Int = 5,
    val userMessage: String? = null
)
