package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Minimal Analog template definition.
 * Minimalist analog clock face with hour, minute, and second hands.
 */
object MinimalAnalogTemplate : TemplateDefinition {
    override val id: String = "builtin_minimal_analog"
    override val name: String = "Minimal Analog"
    override val category: String = "Analog"
    override val description: String = "Minimalist analog clock face with hour, minute, and second hands"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_analog_main",
            name = "Analog Clock Face",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 950.0f,
            width = 500.0f,
            height = 500.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 24.0f,
                fontWeight = "NORMAL",
                fontFamily = "DEFAULT",
                letterSpacing = 0.0f,
                alignment = "CENTER",
                strokeWidth = 4.0f,
                fill = false
            ),
            properties = mapOf(
                "clockStyle" to "ANALOG",
                "showSeconds" to "true",
                "showMarkers" to "true",
                "markerType" to "TICKS"
            )
        ),
        AODElement(
            id = "date_analog_sub",
            name = "Sub Dial Date",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1250.0f,
            width = 400.0f,
            height = 40.0f,
            opacity = 0.7f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 14.0f,
                fontWeight = "MEDIUM",
                fontFamily = "DEFAULT",
                letterSpacing = 1.5f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEE • MMM dd")
        )
    )
}
