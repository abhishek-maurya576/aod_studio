package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODAnimation
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Neon Digital template definition.
 * Futuristic digital layout with high-contrast amber and emerald accents.
 */
object NeonDigitalTemplate : TemplateDefinition {
    override val id: String = "builtin_neon_digital"
    override val name: String = "Neon Digital"
    override val category: String = "Digital"
    override val description: String = "Futuristic digital clock with date, battery, and media status"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "neon_dig_clock",
            name = "Digital Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 800.0f,
            width = 600.0f,
            height = 220.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#E8A838",
                accentColor = "#5EC98A",
                fontSize = 88.0f,
                fontWeight = "BOLD",
                fontFamily = "NEON",
                letterSpacing = 4.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(
                AODElement.PROP_FORMAT to "HH:mm",
                "clockStyle" to "DIGITAL"
            )
        ),
        AODElement(
            id = "neon_dig_date",
            name = "Date Display",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 960.0f,
            width = 450.0f,
            height = 45.0f,
            opacity = 0.85f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 16.0f,
                fontWeight = "MEDIUM",
                fontFamily = "MONO",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEE • MMM dd")
        ),
        AODElement(
            id = "neon_dig_battery",
            name = "Battery Indicator",
            type = AODElementType.BATTERY,
            x = 540.0f,
            y = 1060.0f,
            width = 220.0f,
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
        ),
        AODElement(
            id = "neon_dig_notif",
            name = "Notifications",
            type = AODElementType.NOTIFICATION,
            x = 540.0f,
            y = 1160.0f,
            width = 300.0f,
            height = 50.0f,
            zIndex = 4,
            style = AODStyle(
                color = "#E8A838",
                accentColor = "#E8A838",
                fontSize = 18.0f
            ),
            properties = mapOf("visibilityMode" to "ICONS_ONLY")
        )
    )
}
