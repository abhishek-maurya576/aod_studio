package com.aodstudio.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aodstudio.app.config.AppConfig
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.core.util.generateId
import com.aodstudio.app.data.local.ThemeStorage
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.repository.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = AppConfig.Storage.PREFERENCES_NAME)

/**
 * Repository implementation for managing AOD themes and active selection.
 * Integrates ThemeStorage for file persistence and DataStore for active theme selection.
 */
@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val themeStorage: ThemeStorage
) : ThemeRepository {

    private val KEY_ACTIVE_THEME_ID = stringPreferencesKey("active_theme_id")
    private val _themesFlow = MutableStateFlow<Result<List<AODTheme>>>(Result.Loading)

    init {
        // Initialize themes from assets/storage on startup
        refreshThemes()
    }

    override fun observeAllThemes(): Flow<Result<List<AODTheme>>> = _themesFlow.asStateFlow()

    override suspend fun getAllThemes(): Result<List<AODTheme>> {
        val result = themeStorage.initializeBuiltInThemesIfNeeded()
        _themesFlow.value = result
        return result
    }

    override suspend fun getThemeById(id: String): Result<AODTheme> {
        return themeStorage.getThemeById(id)
    }

    override suspend fun saveTheme(theme: AODTheme): Result<AODTheme> {
        val result = themeStorage.saveTheme(theme)
        if (result is Result.Success) {
            refreshThemes()
        }
        return result
    }

    override suspend fun duplicateTheme(id: String, newName: String?): Result<AODTheme> {
        return when (val getResult = getThemeById(id)) {
            is Result.Success -> {
                val original = getResult.data
                val duplicated = original.copy(
                    id = generateId(),
                    name = newName ?: "${original.name} (Copy)"
                )
                saveTheme(duplicated)
            }
            is Result.Error -> getResult
            else -> Result.Error("Duplicate error")
        }
    }

    override suspend fun deleteTheme(id: String): Result<Boolean> {
        val result = themeStorage.deleteTheme(id)
        if (result is Result.Success) {
            refreshThemes()
        }
        return result
    }

    override suspend fun importTheme(jsonString: String): Result<AODTheme> {
        val result = themeStorage.importThemeFromJson(jsonString)
        if (result is Result.Success) {
            refreshThemes()
        }
        return result
    }

    override suspend fun exportTheme(id: String): Result<String> {
        return themeStorage.exportThemeToJson(id)
    }

    override suspend fun setActiveThemeId(id: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_ACTIVE_THEME_ID] = id
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to set active theme: ${e.message}", e)
        }
    }

    override suspend fun getActiveThemeId(): Result<String> {
        return try {
            val preferences = context.dataStore.data.first()
            val activeId = preferences[KEY_ACTIVE_THEME_ID]
            if (activeId != null) {
                Result.Success(activeId)
            } else {
                // Return default theme ID if none selected
                when (val themes = getAllThemes()) {
                    is Result.Success -> {
                        val firstId = themes.data.firstOrNull()?.id ?: ""
                        if (firstId.isNotEmpty()) setActiveThemeId(firstId)
                        Result.Success(firstId)
                    }
                    else -> Result.Error("No themes available")
                }
            }
        } catch (e: Exception) {
            Result.Error("Failed to get active theme ID: ${e.message}", e)
        }
    }

    private fun refreshThemes() {
        _themesFlow.value = themeStorage.getAllThemes()
    }
}
