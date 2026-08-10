package com.aodstudio.app.domain.model

import com.aodstudio.app.config.AppConfig
import com.aodstudio.app.core.common.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Handles JSON serialization, deserialization, schema validation,
 * and migration mechanism for AODTheme objects.
 */
object ThemeSerializer {

    val jsonInstance = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    /**
     * Serializes an AODTheme object into a JSON string.
     */
    fun serialize(theme: AODTheme): String {
        return jsonInstance.encodeToString(AODTheme.serializer(), theme)
    }

    /**
     * Deserializes a JSON string into an AODTheme, performing schema validation
     * and migration if needed.
     */
    fun deserialize(jsonString: String): Result<AODTheme> {
        return try {
            val jsonElement = jsonInstance.parseToJsonElement(jsonString)
            val jsonObject = jsonElement.jsonObject

            val version = jsonObject["schemaVersion"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1

            if (version > AppConfig.SCHEMA_VERSION) {
                return Result.Error("Unsupported schema version: $version. Current app version is ${AppConfig.SCHEMA_VERSION}.")
            }

            // Migrate if older schema version
            val migratedJson = if (version < AppConfig.SCHEMA_VERSION) {
                migrateSchema(jsonString, version, AppConfig.SCHEMA_VERSION)
            } else {
                jsonString
            }

            val theme = jsonInstance.decodeFromString(AODTheme.serializer(), migratedJson)
            validateTheme(theme)
        } catch (e: Exception) {
            Result.Error("Failed to parse theme JSON: ${e.message}", e)
        }
    }

    /**
     * Validates theme structural integrity.
     */
    fun validateTheme(theme: AODTheme): Result<AODTheme> {
        if (theme.id.isBlank()) {
            return Result.Error("Theme ID cannot be blank.")
        }
        if (theme.name.isBlank()) {
            return Result.Error("Theme name cannot be blank.")
        }
        if (theme.elements.size > AppConfig.Limits.MAX_ELEMENTS_PER_THEME) {
            return Result.Error("Theme exceeds maximum element limit of ${AppConfig.Limits.MAX_ELEMENTS_PER_THEME}.")
        }
        return Result.Success(theme)
    }

    /**
     * Schema migration mechanism.
     */
    private fun migrateSchema(jsonString: String, fromVersion: Int, toVersion: Int): String {
        var currentJson = jsonString
        var currentVer = fromVersion

        while (currentVer < toVersion) {
            currentJson = when (currentVer) {
                1 -> migrateV1ToV2(currentJson)
                else -> currentJson
            }
            currentVer++
        }
        return currentJson
    }

    private fun migrateV1ToV2(jsonString: String): String {
        // Migration placeholder for future schema changes
        return jsonString
    }
}
