package com.aodstudio.app.feature.library

import com.aodstudio.app.domain.model.AODTheme

/**
 * UI State for the Theme Library Screen.
 * Categories are derived dynamically from registered templates and loaded themes.
 */
data class ThemeLibraryUiState(
    val isLoading: Boolean = true,
    val themes: List<AODTheme> = emptyList(),
    val activeThemeId: String? = null,
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All"),
    val errorMessage: String? = null,
    val userMessage: String? = null
) {
    val filteredThemes: List<AODTheme>
        get() = if (selectedCategory.equals("All", ignoreCase = true)) {
            themes
        } else {
            themes.filter {
                val cat = it.metadata[AODTheme.META_CATEGORY] ?: "Custom"
                cat.equals(selectedCategory, ignoreCase = true)
            }
        }
}
