package com.aodstudio.app.feature.editor

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODTheme

/**
 * UI State for the AOD Editor screen.
 */
data class AODEditorUiState(
    val isLoading: Boolean = true,
    val theme: AODTheme? = null,
    val selectedElementId: String? = null,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val zoomScale: Float = 1.0f,
    val showAddElementDialog: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedElement: AODElement?
        get() = theme?.elements?.find { it.id == selectedElementId }
}
