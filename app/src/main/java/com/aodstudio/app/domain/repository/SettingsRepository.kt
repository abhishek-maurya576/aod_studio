package com.aodstudio.app.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Domain repository interface for user preferences and behavior settings.
 */
interface SettingsRepository {

    /**
     * Flow observing whether double tap gesture dismisses AOD display.
     */
    val isDoubleTapToExitEnabled: StateFlow<Boolean>

    /**
     * Updates double tap to exit preference setting.
     */
    fun setDoubleTapToExit(enabled: Boolean)

    /**
     * Synchronously checks if double tap to exit preference is enabled.
     */
    fun getDoubleTapToExitSync(): Boolean
}
