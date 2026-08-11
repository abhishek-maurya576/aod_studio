package com.aodstudio.app.feature.home

import com.aodstudio.app.domain.model.AODTheme

/**
 * UI State for the Home screen.
 */
data class HomeUiState(
    val activeTheme: AODTheme? = null,
    val isAodActive: Boolean = false,
    val isLoading: Boolean = true
)
