package com.aodstudio.app.domain.template

import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.domain.template.definitions.DigitalBoldTemplate
import com.aodstudio.app.domain.template.definitions.FadingDashTemplate
import com.aodstudio.app.domain.template.definitions.MinimalAnalogTemplate
import com.aodstudio.app.domain.template.definitions.MinimalOrbitTemplate
import com.aodstudio.app.domain.template.definitions.NeonDigitalTemplate
import com.aodstudio.app.domain.template.definitions.NeonPulseTemplate
import com.aodstudio.app.domain.template.definitions.OrbitRadialTemplate
import com.aodstudio.app.domain.template.definitions.PixelFlexTemplate
import com.aodstudio.app.domain.template.definitions.RadialOrbitChronographTemplate
import com.aodstudio.app.domain.template.definitions.RetroDigitalTemplate
import com.aodstudio.app.domain.template.definitions.TypographyStackedTemplate
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized registry for all available AOD template blueprints.
 * Provides dynamic template discovery, category derivation, and reset blueprints.
 */
@Singleton
class TemplateRegistry @Inject constructor() {

    private val templates = CopyOnWriteArrayList<TemplateDefinition>().apply {
        addAll(
            listOf(
                MinimalOrbitTemplate,
                DigitalBoldTemplate,
                MinimalAnalogTemplate,
                RetroDigitalTemplate,
                TypographyStackedTemplate,
                OrbitRadialTemplate,
                RadialOrbitChronographTemplate,
                NeonPulseTemplate,
                PixelFlexTemplate,
                FadingDashTemplate,
                NeonDigitalTemplate
            )
        )
    }

    /**
     * Returns all registered template definitions.
     */
    fun getAllTemplates(): List<TemplateDefinition> = templates.toList()

    /**
     * Finds a template definition by its unique identifier.
     */
    fun getTemplateById(id: String): TemplateDefinition? {
        return templates.find { it.id == id }
    }

    /**
     * Dynamically discovers all unique categories from registered templates,
     * prefixed by the "All" category filter.
     */
    fun getCategories(): List<String> {
        val distinctCategories = templates
            .map { it.category }
            .distinct()
            .sorted()
        return listOf("All") + distinctCategories
    }

    /**
     * Filters template definitions by category name.
     */
    fun getTemplatesByCategory(category: String): List<TemplateDefinition> {
        return if (category.equals("All", ignoreCase = true)) {
            getAllTemplates()
        } else {
            templates.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    /**
     * Builds and returns a fresh, unmodified default AODTheme instance for a template ID.
     * Used for resetting modified themes back to their original factory defaults.
     */
    fun getOriginalTemplateTheme(id: String): AODTheme? {
        return getTemplateById(id)?.buildTheme()
    }

    /**
     * Allows dynamic registration of new templates at runtime or during testing.
     */
    fun registerTemplate(definition: TemplateDefinition) {
        if (templates.none { it.id == definition.id }) {
            templates.add(definition)
        }
    }

    /**
     * Checks if a theme ID corresponds to a registered built-in template.
     */
    fun isBuiltInTemplate(id: String): Boolean {
        return templates.any { it.id == id }
    }

    /**
     * Checks if a theme has been customized or modified from its factory blueprint.
     */
    fun isThemeCustomized(theme: AODTheme): Boolean {
        val original = getOriginalTemplateTheme(theme.id) ?: return true
        if (original.elements.size != theme.elements.size) return true
        val origMap = original.elements.associateBy { it.id }
        for (elem in theme.elements) {
            val orig = origMap[elem.id] ?: return true
            if (orig != elem) return true
        }
        return false
    }
}
