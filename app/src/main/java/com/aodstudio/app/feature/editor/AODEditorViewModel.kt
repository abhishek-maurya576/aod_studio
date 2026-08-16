package com.aodstudio.app.feature.editor

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aodstudio.app.aod.service.AODForegroundService
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.usecase.GetThemesUseCase
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
 * ViewModel for the AOD Editor screen.
 * Manages loading theme, selecting elements, updating positions/styles, adding/deleting elements, and saving & applying changes.
 */
@HiltViewModel
class AODEditorViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getThemesUseCase: GetThemesUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val settingsRepository: com.aodstudio.app.domain.repository.SettingsRepository,
    val mediaRepository: MediaRepository,
    val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AODEditorUiState())
    val uiState: StateFlow<AODEditorUiState> = _uiState.asStateFlow()

    fun loadTheme(themeId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (themeId == null) {
                val newTheme = AODTheme.createDefaultTheme("Custom Theme")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        theme = newTheme,
                        selectedElementId = newTheme.elements.firstOrNull()?.id,
                        isDirty = true
                    )
                }
            } else {
                when (val result = getThemesUseCase.getById(themeId)) {
                    is Result.Success -> {
                        val loadedTheme = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                theme = loadedTheme,
                                selectedElementId = loadedTheme.elements.firstOrNull()?.id,
                                isDirty = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun selectElement(elementId: String?) {
        _uiState.update { it.copy(selectedElementId = elementId) }
    }

    private val undoStack = mutableListOf<AODTheme>()
    private val redoStack = mutableListOf<AODTheme>()

    private fun pushHistoryState() {
        _uiState.value.theme?.let { current ->
            undoStack.add(current)
            redoStack.clear()
            _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = false) }
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.theme ?: return
        redoStack.add(current)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        _uiState.update {
            it.copy(
                theme = previous,
                isDirty = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.theme ?: return
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.lastIndex)
        _uiState.update {
            it.copy(
                theme = next,
                isDirty = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun updateElementPosition(elementId: String, rawX: Float, rawY: Float, snap: Boolean = true) {
        val currentTheme = _uiState.value.theme ?: return
        pushHistoryState()

        val finalX = if (snap && kotlin.math.abs(rawX - 540f) < 20f) 540f else rawX
        val finalY = if (snap && kotlin.math.abs(rawY - 1200f) < 20f) 1200f else rawY

        val updatedElements = currentTheme.elements.map { elem ->
            if (elem.id == elementId) elem.copy(x = finalX, y = finalY) else elem
        }
        _uiState.update {
            it.copy(
                theme = currentTheme.copy(elements = updatedElements),
                isDirty = true
            )
        }
    }

    fun moveLayerUp(elementId: String) {
        val currentTheme = _uiState.value.theme ?: return
        pushHistoryState()

        val updatedElements = currentTheme.elements.map { elem ->
            if (elem.id == elementId) elem.copy(zIndex = elem.zIndex + 1) else elem
        }
        _uiState.update {
            it.copy(
                theme = currentTheme.copy(elements = updatedElements),
                isDirty = true
            )
        }
    }

    fun moveLayerDown(elementId: String) {
        val currentTheme = _uiState.value.theme ?: return
        pushHistoryState()

        val updatedElements = currentTheme.elements.map { elem ->
            if (elem.id == elementId) elem.copy(zIndex = (elem.zIndex - 1).coerceAtLeast(0)) else elem
        }
        _uiState.update {
            it.copy(
                theme = currentTheme.copy(elements = updatedElements),
                isDirty = true
            )
        }
    }

    fun updateElementStyle(elementId: String, updatedElement: AODElement) {
        val currentTheme = _uiState.value.theme ?: return
        val updatedElements = currentTheme.elements.map { elem ->
            if (elem.id == elementId) updatedElement else elem
        }
        _uiState.update {
            it.copy(
                theme = currentTheme.copy(elements = updatedElements),
                isDirty = true
            )
        }
    }

    fun addElement(type: AODElementType) {
        val currentTheme = _uiState.value.theme ?: return
        val defaultY = when (type) {
            AODElementType.CLOCK -> 850f
            AODElementType.DATE -> 1000f
            AODElementType.BATTERY -> 1100f
            AODElementType.NOTIFICATION -> 1180f
            AODElementType.MUSIC -> 1260f
            AODElementType.FINGERPRINT -> 1900f
            else -> 1340f
        }
        val defaultFontSize = if (type == AODElementType.NOTIFICATION) 16f else 24f
        val defaultColor = if (type == AODElementType.FINGERPRINT) "#5EC98A" else "#FFFFFF"
        val defaultWidth = if (type == AODElementType.FINGERPRINT) 120f else 200f
        val defaultHeight = if (type == AODElementType.FINGERPRINT) 120f else 100f
        val existingCount = currentTheme.elements.count { it.type == type }
        val baseName = type.name.lowercase().replaceFirstChar { it.uppercase() }
        val newName = if (existingCount > 0) "$baseName ${existingCount + 1}" else baseName

        val newElem = AODElement(
            type = type,
            name = newName,
            x = 540f,
            y = defaultY,
            width = defaultWidth,
            height = defaultHeight,
            style = com.aodstudio.app.domain.model.AODStyle(
                fontSize = defaultFontSize,
                color = defaultColor,
                strokeWidth = 2.5f
            )
        )
        val updatedElements = currentTheme.elements + newElem
        _uiState.update {
            it.copy(
                theme = currentTheme.copy(elements = updatedElements),
                selectedElementId = newElem.id,
                isDirty = true,
                showAddElementDialog = false
            )
        }
    }

    fun deleteSelectedElement() {
        val selectedId = _uiState.value.selectedElementId ?: return
        val currentTheme = _uiState.value.theme ?: return
        val updatedElements = currentTheme.elements.filterNot { it.id == selectedId }
        _uiState.update {
            it.copy(
                theme = currentTheme.copy(elements = updatedElements),
                selectedElementId = updatedElements.firstOrNull()?.id,
                isDirty = true
            )
        }
    }

    fun toggleAddElementDialog(show: Boolean) {
        _uiState.update { it.copy(showAddElementDialog = show) }
    }

    fun saveTheme() {
        val currentTheme = _uiState.value.theme ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = saveThemeUseCase.execute(currentTheme)) {
                is Result.Success -> {
                    saveThemeUseCase.setActive(currentTheme.id)
                    try {
                        if (Settings.canDrawOverlays(context)) {
                            AODForegroundService.startService(context)
                            settingsRepository.setAodEnabled(true)
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    isDirty = false,
                                    userMessage = "Theme saved and active on AOD!"
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    isDirty = false,
                                    userMessage = "Theme saved! Grant System Overlay in Settings to start AOD."
                                )
                            }
                        }
                    } catch (e: Throwable) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isDirty = false,
                                errorMessage = "Saved theme, but could not start AOD service: ${e.message}"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
