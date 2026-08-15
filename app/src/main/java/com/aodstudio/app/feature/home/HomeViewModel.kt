package com.aodstudio.app.feature.home

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aodstudio.app.aod.service.AODForegroundService
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import com.aodstudio.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for HomeScreen managing active theme preview and service status.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getThemesUseCase: GetThemesUseCase,
    private val settingsRepository: SettingsRepository,
    val batteryRepository: BatteryRepository,
    val notificationRepository: NotificationRepository,
    val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.isAodEnabled.collect { isEnabled ->
                _uiState.update { it.copy(isAodActive = isEnabled) }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val activeThemeResult = getThemesUseCase.getActiveTheme()
            val theme = if (activeThemeResult is Result.Success) {
                activeThemeResult.data
            } else {
                AODTheme.createDefaultTheme()
            }

            val isAodEnabled = settingsRepository.isAodEnabledSync()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeTheme = theme,
                    isAodActive = isAodEnabled
                )
            }
        }
    }
}
