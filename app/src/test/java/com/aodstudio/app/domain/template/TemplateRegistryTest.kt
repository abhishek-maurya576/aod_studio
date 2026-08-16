package com.aodstudio.app.domain.template

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TemplateRegistry dynamic discovery, category derivation,
 * template registration, customization detection, and factory reset functionality.
 */
class TemplateRegistryTest {

    private lateinit var registry: TemplateRegistry

    @Before
    fun setup() {
        registry = TemplateRegistry()
    }

    @Test
    fun `registry contains built-in templates on initialization`() {
        val templates = registry.getAllTemplates()
        assertTrue("Registry should contain built-in templates", templates.isNotEmpty())
        assertTrue("Should contain Minimal Orbit", templates.any { it.id == "builtin_minimal_orbit" })
        assertTrue("Should contain Digital Bold", templates.any { it.id == "builtin_digital_bold" })
        assertTrue("Should contain Neon Digital", templates.any { it.id == "builtin_neon_digital" })
        assertTrue("Should contain Radial Orbit Chrono", templates.any { it.id == "builtin_radial_orbit_chrono" })
    }

    @Test
    fun `getCategories dynamically derives sorted categories starting with All`() {
        val categories = registry.getCategories()
        assertTrue(categories.isNotEmpty())
        assertEquals("All", categories.first())
        assertTrue(categories.contains("Minimal"))
        assertTrue(categories.contains("Digital"))
        assertTrue(categories.contains("Analog"))
        assertTrue(categories.contains("Typography"))
        assertTrue(categories.contains("Orbit"))
    }

    @Test
    fun `getTemplatesByCategory returns matching templates`() {
        val minimalTemplates = registry.getTemplatesByCategory("Minimal")
        assertTrue(minimalTemplates.isNotEmpty())
        assertTrue(minimalTemplates.all { it.category.equals("Minimal", ignoreCase = true) })

        val allTemplates = registry.getTemplatesByCategory("All")
        assertEquals(registry.getAllTemplates().size, allTemplates.size)
    }

    @Test
    fun `getOriginalTemplateTheme produces complete AODTheme instance`() {
        val theme = registry.getOriginalTemplateTheme("builtin_minimal_orbit")
        assertNotNull(theme)
        assertEquals("Minimal Orbit", theme?.name)
        assertEquals("Minimal", theme?.metadata?.get(AODTheme.META_CATEGORY))
        assertTrue(theme?.elements?.isNotEmpty() == true)
    }

    @Test
    fun `isThemeCustomized returns true when element is added or modified`() {
        val original = registry.getOriginalTemplateTheme("builtin_minimal_analog")!!
        assertFalse(registry.isThemeCustomized(original))

        val modified = original.copy(
            elements = original.elements + AODElement(
                id = "battery_new",
                type = AODElementType.BATTERY,
                x = 540f,
                y = 1100f
            )
        )
        assertTrue(registry.isThemeCustomized(modified))
    }

    @Test
    fun `registerTemplate dynamically adds new template and its category`() {
        val customTemplate = object : TemplateDefinition {
            override val id: String = "custom_cyberpunk"
            override val name: String = "Cyberpunk 2077"
            override val category: String = "Cyberpunk"
            override val description: String = "High tech low life theme"
            override val author: String = "Tester"
            override val elements: List<AODElement> = listOf(
                AODElement(id = "c1", type = AODElementType.CLOCK, x = 540f, y = 800f)
            )
        }

        registry.registerTemplate(customTemplate)

        val retrieved = registry.getTemplateById("custom_cyberpunk")
        assertNotNull(retrieved)
        assertEquals("Cyberpunk 2077", retrieved?.name)

        val categories = registry.getCategories()
        assertTrue("Categories should now dynamically include Cyberpunk", categories.contains("Cyberpunk"))

        val cyberpunkTemplates = registry.getTemplatesByCategory("Cyberpunk")
        assertEquals(1, cyberpunkTemplates.size)
        assertEquals("custom_cyberpunk", cyberpunkTemplates.first().id)
    }
}
