package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODAnimation
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Fading Dash template definition.
 * Fading Dash layout with Rose Cursive typography.
 */
object FadingDashTemplate : TemplateDefinition {
    override val id: String = "builtin_fading_dash"
    override val name: String = "Fading Dash"
    override val category: String = "Minimal"
    override val description: String = "Fading Dash layout with Rose Cursive typography"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_dash",
            name = "Dash Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 850.0f,
            width = 500.0f,
            height = 200.0f,
            opacity = 0.9f,
            zIndex = 1,
            style = AODStyle(
                color = "#E87C7C",
                accentColor = "#E87C7C",
                fontSize = 84.0f,
                fontWeight = "NORMAL",
                fontFamily = "CURSIVE",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            animation = AODAnimation(type = "FADE", durationMs = 1500),
            properties = mapOf(AODElement.PROP_FORMAT to "hh:mm a")
        ),
        AODElement(
            id = "date_dash",
            name = "Dash Date",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1000.0f,
            width = 400.0f,
            height = 50.0f,
            opacity = 0.7f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E87C7C",
                fontSize = 16.0f,
                fontWeight = "MEDIUM",
                fontFamily = "CURSIVE",
                letterSpacing = 1.5f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEE, MMM dd")
        )
    )
}
