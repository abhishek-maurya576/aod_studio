package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Retro Digital template definition.
 * Retro Cyber Digital Clock with Monospace Accents.
 */
object RetroDigitalTemplate : TemplateDefinition {
    override val id: String = "builtin_retro_digital"
    override val name: String = "Retro Digital"
    override val category: String = "Digital"
    override val description: String = "Retro Cyber Digital Clock with Monospace Accents"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_retro",
            name = "Retro Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 850.0f,
            width = 500.0f,
            height = 200.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#E8A838",
                accentColor = "#E8A838",
                fontSize = 88.0f,
                fontWeight = "BOLD",
                fontFamily = "CYBER",
                letterSpacing = 4.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "HH:mm")
        ),
        AODElement(
            id = "date_retro",
            name = "Retro Date",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1000.0f,
            width = 400.0f,
            height = 50.0f,
            opacity = 0.8f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 16.0f,
                fontWeight = "NORMAL",
                fontFamily = "MONO",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEE • MMM dd")
        ),
        AODElement(
            id = "battery_retro",
            name = "Battery Bar",
            type = AODElementType.BATTERY,
            x = 540.0f,
            y = 1100.0f,
            width = 200.0f,
            height = 40.0f,
            zIndex = 3,
            style = AODStyle(
                color = "#E8A838",
                accentColor = "#E8A838",
                fontSize = 14.0f,
                fontWeight = "MEDIUM",
                fontFamily = "CYBER",
                letterSpacing = 1.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_BATTERY_STYLE to "PERCENTAGE")
        )
    )
}
