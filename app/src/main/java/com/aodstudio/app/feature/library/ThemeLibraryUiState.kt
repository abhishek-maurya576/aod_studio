package com.aodstudio.app.feature.library

import com.aodstudio.app.domain.model.AODTheme

/**
 * UI State for the Theme Library Screen.
 */
data class ThemeLibraryUiState(
    val isLoading: Boolean = true,
    val themes: List<AODTheme> = emptyList(),
    val activeThemeId: String? = null,
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Minimal", "Digital", "Analog", "Typography", "Orbit"),
    val errorMessage: String? = null,
    val userMessage: String? = null
) {
    val filteredThemes: List<AODTheme>
        get() = if (selectedCategory == "All") {
            themes
        } else {
            themes.filter { it.metadata[AODTheme.META_CATEGORY].equals(selectedCategory, ignoreCase = true) }
        }
}
