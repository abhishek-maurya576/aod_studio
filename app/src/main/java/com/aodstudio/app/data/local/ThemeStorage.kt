package com.aodstudio.app.data.local

import android.content.Context
import com.aodstudio.app.config.AppConfig
import com.aodstudio.app.core.common.Result
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.model.ThemeSerializer
import com.aodstudio.app.domain.template.TemplateRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages theme JSON file persistence in local internal storage and assets.
 * Handles reading, writing, deleting, importing, reset-to-default, and initial template discovery.
 */
@Singleton
class ThemeStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val templateRegistry: TemplateRegistry
) {

    private val themesDir: File
        get() = File(context.filesDir, AppConfig.Storage.THEMES_DIR).apply {
            if (!exists()) mkdirs()
        }

    /**
     * Initializes theme storage — populates built-in asset themes and code-registered templates
     * if local files are missing.
     */
    fun initializeBuiltInThemesIfNeeded(): Result<List<AODTheme>> {
        return try {
            val existingFiles = getThemeFiles()
            if (existingFiles.isEmpty()) {
                loadAndSaveBuiltInAssetThemes()
            }
            syncRegisteredTemplates()
            getAllThemes()
        } catch (e: Exception) {
            Result.Error("Failed to initialize themes: ${e.message}", e)
        }
    }

    /**
     * Synchronizes any templates from TemplateRegistry that are not yet saved to disk.
     */
    private fun syncRegisteredTemplates() {
        for (templateDef in templateRegistry.getAllTemplates()) {
            val file = getFileForTheme(templateDef.id)
            if (!file.exists()) {
                saveTheme(templateDef.buildTheme())
            }
        }
    }

    /**
     * Reads all saved themes from the local themes directory.
     */
    fun getAllThemes(): Result<List<AODTheme>> {
        return try {
            val files = getThemeFiles()
            val themes = mutableListOf<AODTheme>()

            for (file in files) {
                val json = file.readText()
                when (val result = ThemeSerializer.deserialize(json)) {
                    is Result.Success -> themes.add(result.data)
                    is Result.Error -> {
                        // Skip unparseable files
                    }
                    else -> {}
                }
            }

            if (themes.isEmpty()) {
                // Fallback to registered templates or default
                val defaultTheme = templateRegistry.getAllTemplates().firstOrNull()?.buildTheme()
                    ?: AODTheme.createDefaultTheme()
                saveTheme(defaultTheme)
                Result.Success(listOf(defaultTheme))
            } else {
                Result.Success(themes)
            }
        } catch (e: Exception) {
            Result.Error("Failed to read themes: ${e.message}", e)
        }
    }

    /**
     * Reads a single theme by ID.
     */
    fun getThemeById(id: String): Result<AODTheme> {
        return try {
            val file = getFileForTheme(id)
            if (!file.exists()) {
                // Check if it's available in TemplateRegistry
                val registryTheme = templateRegistry.getOriginalTemplateTheme(id)
                if (registryTheme != null) {
                    saveTheme(registryTheme)
                    return Result.Success(registryTheme)
                }
                return Result.Error("Theme not found with ID: $id")
            }
            val json = file.readText()
            ThemeSerializer.deserialize(json)
        } catch (e: Exception) {
            Result.Error("Failed to load theme $id: ${e.message}", e)
        }
    }

    /**
     * Saves a theme to local storage.
     */
    fun saveTheme(theme: AODTheme): Result<AODTheme> {
        return try {
            val validation = ThemeSerializer.validateTheme(theme)
            if (validation is Result.Error) return validation

            val json = ThemeSerializer.serialize(theme)
            val file = getFileForTheme(theme.id)
            file.writeText(json)
            Result.Success(theme)
        } catch (e: Exception) {
            Result.Error("Failed to save theme: ${e.message}", e)
        }
    }

    /**
     * Resets a template back to its factory default definition from TemplateRegistry.
     */
    fun resetThemeToDefault(id: String): Result<AODTheme> {
        val originalTheme = templateRegistry.getOriginalTemplateTheme(id)
            ?: return Result.Error("Template '$id' does not have a factory default definition.")
        return saveTheme(originalTheme)
    }

    /**
     * Deletes a theme file by ID.
     */
    fun deleteTheme(id: String): Result<Boolean> {
        return try {
            val file = getFileForTheme(id)
            if (file.exists()) {
                val deleted = file.delete()
                Result.Success(deleted)
            } else {
                Result.Error("Theme file does not exist for ID: $id")
            }
        } catch (e: Exception) {
            Result.Error("Failed to delete theme: ${e.message}", e)
        }
    }

    /**
     * Imports a theme from a raw JSON string.
     */
    fun importThemeFromJson(jsonString: String): Result<AODTheme> {
        return when (val result = ThemeSerializer.deserialize(jsonString)) {
            is Result.Success -> saveTheme(result.data)
            is Result.Error -> result
            else -> Result.Error("Unknown import error")
        }
    }

    /**
     * Exports a theme to a raw JSON string.
     */
    fun exportThemeToJson(id: String): Result<String> {
        return when (val result = getThemeById(id)) {
            is Result.Success -> Result.Success(ThemeSerializer.serialize(result.data))
            is Result.Error -> result
            else -> Result.Error("Unknown export error")
        }
    }

    private fun getThemeFiles(): Array<File> {
        return themesDir.listFiles { _, name ->
            name.endsWith(AppConfig.Storage.THEME_FILE_EXTENSION) || name.endsWith(".json")
        } ?: emptyArray()
    }

    private fun getFileForTheme(id: String): File {
        val safeId = id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(themesDir, "$safeId${AppConfig.Storage.THEME_FILE_EXTENSION}")
    }

    private fun loadAndSaveBuiltInAssetThemes(): Result<List<AODTheme>> {
        val loadedThemes = mutableListOf<AODTheme>()
        try {
            val assetManager = context.assets
            val assetFiles = assetManager.list(AppConfig.Storage.BUILTIN_THEMES_ASSET_DIR) ?: emptyArray()

            for (fileName in assetFiles) {
                if (fileName.endsWith(".json")) {
                    val json = assetManager.open("${AppConfig.Storage.BUILTIN_THEMES_ASSET_DIR}/$fileName")
                        .bufferedReader().use { it.readText() }

                    when (val result = ThemeSerializer.deserialize(json)) {
                        is Result.Success -> {
                            saveTheme(result.data)
                            loadedThemes.add(result.data)
                        }
                        else -> {}
                    }
                }
            }

            if (loadedThemes.isEmpty()) {
                for (templateDef in templateRegistry.getAllTemplates()) {
                    val theme = templateDef.buildTheme()
                    saveTheme(theme)
                    loadedThemes.add(theme)
                }
            }
        } catch (e: Exception) {
            for (templateDef in templateRegistry.getAllTemplates()) {
                val theme = templateDef.buildTheme()
                saveTheme(theme)
                loadedThemes.add(theme)
            }
        }
        return Result.Success(loadedThemes)
    }
}
