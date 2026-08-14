package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Pixel Flex template definition.
 * Pixel Flex layout with Emerald Neon typography.
 */
object PixelFlexTemplate : TemplateDefinition {
    override val id: String = "builtin_pixel_flex"
    override val name: String = "Pixel Flex"
    override val category: String = "Digital"
    override val description: String = "Pixel Flex layout with Emerald Neon typography"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_pixel",
            name = "Pixel Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 850.0f,
            width = 500.0f,
            height = 200.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#5EC98A",
                accentColor = "#5EC98A",
                fontSize = 92.0f,
                fontWeight = "BOLD",
                fontFamily = "NEON",
                letterSpacing = 3.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "HH:mm")
        ),
        AODElement(
            id = "date_pixel",
            name = "Pixel Date",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1000.0f,
            width = 400.0f,
            height = 50.0f,
            opacity = 0.85f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#5EC98A",
                fontSize = 18.0f,
                fontWeight = "MEDIUM",
                fontFamily = "DISPLAY",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEEE • MMM dd")
        ),
        AODElement(
            id = "battery_pixel",
            name = "Pixel Battery",
            type = AODElementType.BATTERY,
            x = 540.0f,
            y = 1100.0f,
            width = 200.0f,
            height = 40.0f,
            zIndex = 3,
            style = AODStyle(
                color = "#5EC98A",
                accentColor = "#5EC98A",
                fontSize = 14.0f,
                fontWeight = "MEDIUM",
                fontFamily = "NEON",
                letterSpacing = 1.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_BATTERY_STYLE to "PERCENTAGE")
        )
    )
}
