package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODAnimation
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Orbit Radial template definition.
 * Concentric radial orbit dial with emerald accents.
 */
object OrbitRadialTemplate : TemplateDefinition {
    override val id: String = "builtin_orbit_radial"
    override val name: String = "Minimal Orbit Radial"
    override val category: String = "Orbit"
    override val description: String = "Concentric radial orbit dial with emerald accents"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_radial_orbit",
            name = "Radial Orbit Dial",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 950.0f,
            width = 450.0f,
            height = 450.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#5EC98A",
                fontSize = 48.0f,
                fontWeight = "LIGHT",
                fontFamily = "DISPLAY",
                letterSpacing = 1.0f,
                alignment = "CENTER",
                strokeWidth = 3.0f,
                fill = false
            ),
            animation = AODAnimation(type = "ORBIT"),
            properties = mapOf("clockStyle" to "ORBIT")
        ),
        AODElement(
            id = "battery_radial_ring",
            name = "Battery Arc Ring",
            type = AODElementType.BATTERY,
            x = 540.0f,
            y = 1250.0f,
            width = 180.0f,
            height = 40.0f,
            opacity = 0.9f,
            zIndex = 2,
            style = AODStyle(
                color = "#5EC98A",
                accentColor = "#5EC98A",
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
