package com.aodstudio.app.domain.template

import com.aodstudio.app.config.AppConfig
import com.aodstudio.app.domain.model.AODCanvas
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODTheme

/**
 * Self-contained contract representing an independent AOD template blueprint.
 * Each template specifies its metadata, category, and initial element configuration.
 */
interface TemplateDefinition {
    val id: String
    val name: String
    val category: String
    val description: String
    val author: String
    val canvas: AODCanvas get() = AODCanvas()
    val elements: List<AODElement>

    /**
     * Builds a concrete AODTheme instance from this template definition.
     */
    fun buildTheme(): AODTheme {
        return AODTheme(
            id = id,
            name = name,
            schemaVersion = AppConfig.SCHEMA_VERSION,
            canvas = canvas,
            elements = elements,
            metadata = mapOf(
                AODTheme.META_AUTHOR to author,
                AODTheme.META_DESCRIPTION to description,
                AODTheme.META_CATEGORY to category
            )
        )
    }
}
