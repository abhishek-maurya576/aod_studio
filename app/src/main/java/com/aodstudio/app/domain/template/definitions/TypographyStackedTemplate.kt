package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Typography Stacked template definition.
 * Bold stacked typography digits AOD style.
 */
object TypographyStackedTemplate : TemplateDefinition {
    override val id: String = "builtin_typography_stacked"
    override val name: String = "Typography Stacked"
    override val category: String = "Typography"
    override val description: String = "Bold stacked typography digits AOD style"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_typography_stacked",
            name = "Stacked Digits Clock",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 900.0f,
            width = 500.0f,
            height = 400.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#E8A838",
                fontSize = 120.0f,
                fontWeight = "BOLD",
                fontFamily = "DISPLAY",
                letterSpacing = 2.0f,
                alignment = "CENTER"
            ),
            properties = mapOf("clockStyle" to "STACKED")
        ),
        AODElement(
            id = "date_typo_sub",
            name = "Sub Date Text",
            type = AODElementType.DATE,
            x = 540.0f,
            y = 1150.0f,
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
            properties = mapOf(AODElement.PROP_FORMAT to "MON • AUG 10")
        )
    )
}
