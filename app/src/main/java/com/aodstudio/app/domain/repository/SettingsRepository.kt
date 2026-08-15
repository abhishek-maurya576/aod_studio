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

    /**
     * Flow observing whether AOD service is persistently enabled by user.
     */
    val isAodEnabled: StateFlow<Boolean>

    /**
     * Updates AOD master enabled preference.
     */
    fun setAodEnabled(enabled: Boolean)

    /**
     * Synchronously checks if AOD master enabled preference is true.
     */
    fun isAodEnabledSync(): Boolean
}
