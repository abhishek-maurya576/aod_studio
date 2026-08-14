package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODAnimation
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Minimal Orbit template definition.
 * Minimalist orbit clock with date and battery status.
 */
object MinimalOrbitTemplate : TemplateDefinition {
    override val id: String = "builtin_minimal_orbit"
    override val name: String = "Minimal Orbit"
    override val category: String = "Minimal"
    override val description: String = "Minimalist orbit clock with date and battery status"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_main",
            name = "Digital Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 900.0f,
            width = 500.0f,
            height = 200.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 72.0f,
                fontWeight = "THIN",
                fontFamily = "DEFAULT",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "HH:mm")
        ),
        AODElement(
            id = "date_main",
            name = "Date Display",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1050.0f,
            width = 400.0f,
            height = 50.0f,
            opacity = 0.7f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 16.0f,
                fontWeight = "MEDIUM",
                fontFamily = "DEFAULT",
                letterSpacing = 1.5f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEE • MMM dd")
        ),
        AODElement(
            id = "battery_main",
            name = "Battery Status",
            type = AODElementType.BATTERY,
            x = 540.0f,
            y = 1150.0f,
            width = 200.0f,
            height = 40.0f,
            zIndex = 3,
            style = AODStyle(
                color = "#E8A838",
                accentColor = "#E8A838",
                fontSize = 14.0f,
                fontWeight = "MEDIUM",
                fontFamily = "DEFAULT",
                letterSpacing = 0.5f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_BATTERY_STYLE to "PERCENTAGE")
        )
    )
}
