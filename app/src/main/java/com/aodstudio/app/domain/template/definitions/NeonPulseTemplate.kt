package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODAnimation
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Neon Pulse template definition.
 * Pulsing Neon Cyber theme with Cyan and Purple accents.
 */
object NeonPulseTemplate : TemplateDefinition {
    override val id: String = "builtin_neon_pulse"
    override val name: String = "Neon Pulse"
    override val category: String = "Digital"
    override val description: String = "Pulsing Neon Cyber theme with Cyan and Purple accents"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_pulse",
            name = "Neon Pulse Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 850.0f,
            width = 500.0f,
            height = 200.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#A855F7",
                accentColor = "#38BDF8",
                fontSize = 96.0f,
                fontWeight = "BOLD",
                fontFamily = "NEON",
                letterSpacing = 4.0f,
                alignment = "CENTER"
            ),
            animation = AODAnimation(type = "PULSE", durationMs = 2000, easing = "EASE_IN_OUT"),
            properties = mapOf(AODElement.PROP_FORMAT to "HH:mm")
        ),
        AODElement(
            id = "date_pulse",
            name = "Neon Date",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1000.0f,
            width = 400.0f,
            height = 50.0f,
            opacity = 0.85f,
            zIndex = 2,
            style = AODStyle(
                color = "#38BDF8",
                accentColor = "#A855F7",
                fontSize = 18.0f,
                fontWeight = "MEDIUM",
                fontFamily = "CYBER",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEE • MMM dd")
        )
    )
}
