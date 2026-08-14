package com.aodstudio.app.domain.repository

import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for AOD themes.
 * Abstract storage contract used by domain use cases and ViewModels.
 */
interface ThemeRepository {

    /**
     * Observes the list of all available themes as a Flow.
     */
    fun observeAllThemes(): Flow<Result<List<AODTheme>>>

    /**
     * Fetches all available themes synchronously / asynchronously.
     */
    suspend fun getAllThemes(): Result<List<AODTheme>>

    /**
     * Gets a single theme by ID.
     */
    suspend fun getThemeById(id: String): Result<AODTheme>

    /**
     * Saves or updates a theme.
     */
    suspend fun saveTheme(theme: AODTheme): Result<AODTheme>

    /**
     * Resets a built-in theme back to its original factory default configuration.
     */
    suspend fun resetThemeToDefault(id: String): Result<AODTheme>

    /**
     * Deletes a theme by ID.
     */
    suspend fun deleteTheme(id: String): Result<Boolean>

    /**
     * Imports a theme from a JSON string.
     */
    suspend fun importTheme(jsonString: String): Result<AODTheme>

    /**
     * Exports a theme to a JSON string.
     */
    suspend fun exportTheme(id: String): Result<String>

    /**
     * Sets the active theme ID.
     */
    suspend fun setActiveThemeId(id: String): Result<Unit>

    /**
     * Gets the currently active theme ID.
     */
    suspend fun getActiveThemeId(): Result<String>
}
