package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Digital Bold template definition.
 * High contrast bold digital clock with monospace typography.
 */
object DigitalBoldTemplate : TemplateDefinition {
    override val id: String = "builtin_digital_bold"
    override val name: String = "Digital Bold"
    override val category: String = "Digital"
    override val description: String = "High contrast bold digital clock with monospace typography"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_digital",
            name = "Bold Digital Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 800.0f,
            width = 600.0f,
            height = 250.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#E8A838",
                accentColor = "#E8A838",
                fontSize = 96.0f,
                fontWeight = "BOLD",
                fontFamily = "MONO",
                letterSpacing = 4.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "HH:mm")
        ),
        AODElement(
            id = "date_digital",
            name = "Date String",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 980.0f,
            width = 400.0f,
            height = 40.0f,
            opacity = 0.8f,
            zIndex = 2,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 14.0f,
                fontWeight = "NORMAL",
                fontFamily = "DEFAULT",
                letterSpacing = 1.0f,
                alignment = "CENTER"
            ),
            properties = mapOf(AODElement.PROP_FORMAT to "EEEE, dd MMMM")
        )
    )
}
