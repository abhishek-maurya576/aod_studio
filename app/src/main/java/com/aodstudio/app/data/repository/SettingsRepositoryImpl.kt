package com.aodstudio.app.data.repository

import android.content.Context
import com.aodstudio.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences implementation of [SettingsRepository].
 * Manages user behavior preferences with `doubleTapToExit` defaulting to true.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isDoubleTapToExitEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_DOUBLE_TAP_TO_EXIT, true)
    )
    override val isDoubleTapToExitEnabled: StateFlow<Boolean> = _isDoubleTapToExitEnabled.asStateFlow()

    private val _isAodEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_AOD_ENABLED, false)
    )
    override val isAodEnabled: StateFlow<Boolean> = _isAodEnabled.asStateFlow()

    override fun setDoubleTapToExit(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DOUBLE_TAP_TO_EXIT, enabled).apply()
        _isDoubleTapToExitEnabled.value = enabled
    }

    override fun getDoubleTapToExitSync(): Boolean {
        return prefs.getBoolean(KEY_DOUBLE_TAP_TO_EXIT, true)
    }

    override fun setAodEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AOD_ENABLED, enabled).apply()
        _isAodEnabled.value = enabled
    }

    override fun isAodEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_AOD_ENABLED, false)
    }

    companion object {
        private const val PREFS_NAME = "aod_settings_prefs"
        private const val KEY_DOUBLE_TAP_TO_EXIT = "double_tap_to_exit"
        private const val KEY_AOD_ENABLED = "aod_master_enabled"
    }
}
