package com.aodstudio.app.feature.library

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aodstudio.app.aod.service.AODForegroundService
import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.template.TemplateRegistry
import com.aodstudio.app.domain.usecase.DeleteThemeUseCase
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.domain.usecase.ImportExportThemeUseCase
import com.aodstudio.app.domain.usecase.ResetThemeUseCase
import com.aodstudio.app.domain.usecase.SaveThemeUseCase
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.notification.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Theme Library screen.
 * Manages loading, dynamic category filtering, active selection, applying to AOD service, reset-to-default, deletion, and JSON import/export.
 */
@HiltViewModel
class ThemeLibraryViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getThemesUseCase: GetThemesUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val deleteThemeUseCase: DeleteThemeUseCase,
    private val resetThemeUseCase: ResetThemeUseCase,
    private val importExportThemeUseCase: ImportExportThemeUseCase,
    private val settingsRepository: com.aodstudio.app.domain.repository.SettingsRepository,
    val templateRegistry: TemplateRegistry,
    val batteryRepository: BatteryRepository,
    val notificationRepository: NotificationRepository,
    val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeLibraryUiState())
    val uiState: StateFlow<ThemeLibraryUiState> = _uiState.asStateFlow()

    init {
        loadThemes()
    }

    fun loadThemes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val themesResult = getThemesUseCase.execute()) {
                is Result.Success -> {
                    val activeIdResult = getThemesUseCase.getActiveTheme()
                    val activeId = (activeIdResult as? Result.Success)?.data?.id
                    val loadedThemes = themesResult.data

                    // Dynamically discover all unique categories
                    val registryCategories = templateRegistry.getCategories()
                    val themeCategories = loadedThemes
                        .mapNotNull { it.metadata[AODTheme.META_CATEGORY] }
                        .filter { it.isNotBlank() }

                    val allCategories = (registryCategories + themeCategories)
                        .distinct()
                        .sortedWith(Comparator { a, b ->
                            if (a.equals("All", ignoreCase = true)) -1
                            else if (b.equals("All", ignoreCase = true)) 1
                            else a.compareTo(b, ignoreCase = true)
                        })

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            themes = loadedThemes,
                            categories = allCategories,
                            activeThemeId = activeId
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = themesResult.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun activateTheme(themeId: String) {
        viewModelScope.launch {
            when (val result = saveThemeUseCase.setActive(themeId)) {
                is Result.Success -> {
                    try {
                        if (Settings.canDrawOverlays(context)) {
                            AODForegroundService.startService(context)
                            settingsRepository.setAodEnabled(true)
                            _uiState.update {
                                it.copy(
                                    activeThemeId = themeId,
                                    userMessage = "Theme applied & active on AOD!"
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    activeThemeId = themeId,
                                    userMessage = "Theme set as active! Grant System Overlay in Settings to start AOD."
                                )
                            }
                        }
                    } catch (e: Throwable) {
                        _uiState.update {
                            it.copy(
                                activeThemeId = themeId,
                                errorMessage = "Activated theme, but could not start AOD service: ${e.message}"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun resetThemeToDefault(themeId: String) {
        viewModelScope.launch {
            when (val result = resetThemeUseCase.execute(themeId)) {
                is Result.Success -> {
                    loadThemes()
                    _uiState.update {
                        it.copy(userMessage = "Reset '${result.data.name}' to default configuration")
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun isBuiltInTemplate(themeId: String): Boolean {
        return templateRegistry.isBuiltInTemplate(themeId)
    }

    fun isThemeCustomized(theme: AODTheme): Boolean {
        return templateRegistry.isThemeCustomized(theme)
    }

    fun deleteTheme(themeId: String) {
        viewModelScope.launch {
            when (val result = deleteThemeUseCase.execute(themeId)) {
                is Result.Success -> {
                    loadThemes()
                    _uiState.update { it.copy(userMessage = "Theme deleted") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun importTheme(jsonString: String) {
        viewModelScope.launch {
            when (val result = importExportThemeUseCase.importFromJson(jsonString)) {
                is Result.Success -> {
                    loadThemes()
                    _uiState.update { it.copy(userMessage = "Imported '${result.data.name}'") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
