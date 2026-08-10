package com.aodstudio.app.domain.model

import com.aodstudio.app.config.AppConfig
import com.aodstudio.app.core.util.generateId
import kotlinx.serialization.Serializable

/**
 * Root domain entity representing a complete AOD theme definition.
 * Holds theme metadata, canvas configuration, and a list of AODElements.
 */
@Serializable
data class AODTheme(
    val id: String = generateId(),
    val name: String,
    val schemaVersion: Int = AppConfig.SCHEMA_VERSION,
    val canvas: AODCanvas = AODCanvas(),
    val elements: List<AODElement> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        const val META_AUTHOR = "author"
        const val META_DESCRIPTION = "description"
        const val META_CATEGORY = "category"
        const val META_CREATED_AT = "createdAt"
        const val META_MODIFIED_AT = "modifiedAt"

        /**
         * Creates a minimal default clock theme.
         */
        fun createDefaultTheme(name: String = "Minimal Orbit"): AODTheme {
            val clockElement = AODElement(
                id = generateId(),
                name = "Digital Clock",
                type = AODElementType.CLOCK,
                x = 540f,
                y = 900f,
                width = 500f,
                height = 200f,
                style = AODStyle(fontSize = 72f, fontWeight = "THIN"),
                properties = mapOf(AODElement.PROP_FORMAT to "HH:mm")
            )

            val dateElement = AODElement(
                id = generateId(),
                name = "Date Display",
                type = AODElementType.DATE,
                x = 540f,
                y = 1050f,
                width = 400f,
                height = 50f,
                style = AODStyle(fontSize = 16f, fontWeight = "MEDIUM", color = "#99FFFFFF"),
                properties = mapOf(AODElement.PROP_FORMAT to "EEE • MMM dd")
            )

            val batteryElement = AODElement(
                id = generateId(),
                name = "Battery Display",
                type = AODElementType.BATTERY,
                x = 540f,
                y = 1150f,
                width = 200f,
                height = 40f,
                style = AODStyle(fontSize = 14f, color = "#E8A838"),
                properties = mapOf(AODElement.PROP_BATTERY_STYLE to "PERCENTAGE")
            )

            return AODTheme(
                name = name,
                elements = listOf(clockElement, dateElement, batteryElement),
                metadata = mapOf(
                    META_AUTHOR to "AOD Studio",
                    META_DESCRIPTION to "Minimalist default clock theme",
                    META_CATEGORY to "Minimal"
                )
            )
        }
    }
}
