package com.aodstudio.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.usecase.DeleteThemeUseCase
import com.aodstudio.app.domain.usecase.DuplicateThemeUseCase
import com.aodstudio.app.domain.usecase.GetThemesUseCase
import com.aodstudio.app.domain.usecase.ImportExportThemeUseCase
import com.aodstudio.app.domain.usecase.SaveThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Theme Library screen.
 * Manages loading, filtering, active selection, duplication, deletion, and JSON import/export.
 */
@HiltViewModel
class ThemeLibraryViewModel @Inject constructor(
    private val getThemesUseCase: GetThemesUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val deleteThemeUseCase: DeleteThemeUseCase,
    private val duplicateThemeUseCase: DuplicateThemeUseCase,
    private val importExportThemeUseCase: ImportExportThemeUseCase
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            themes = themesResult.data,
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
                    _uiState.update {
                        it.copy(
                            activeThemeId = themeId,
                            userMessage = "Theme activated as active AOD"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun duplicateTheme(themeId: String) {
        viewModelScope.launch {
            when (val result = duplicateThemeUseCase.execute(themeId)) {
                is Result.Success -> {
                    loadThemes()
                    _uiState.update { it.copy(userMessage = "Theme duplicated successfully") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
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
