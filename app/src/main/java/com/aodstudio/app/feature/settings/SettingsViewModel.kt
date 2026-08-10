package com.aodstudio.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.aodstudio.app.aod.service.AODForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for Settings screen handling permission checks, AOD service toggles, and Vivo device preferences.
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

    fun checkPermissions() {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        _uiState.update {
            it.copy(
                hasOverlayPermission = hasOverlay
            )
        }
    }

    fun toggleAodService(enable: Boolean) {
        if (enable) {
            if (!_uiState.value.hasOverlayPermission) {
                _uiState.update { it.copy(userMessage = "System Overlay permission required") }
                return
            }
            AODForegroundService.startService(context)
            _uiState.update { it.copy(isAodEnabled = true, userMessage = "AOD Service Enabled") }
        } else {
            AODForegroundService.stopService(context)
            _uiState.update { it.copy(isAodEnabled = false, userMessage = "AOD Service Disabled") }
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

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
