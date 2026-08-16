package com.aodstudio.app.domain.template.definitions

import com.aodstudio.app.domain.model.AODAnimation
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.domain.model.AODStyle
import com.aodstudio.app.domain.template.TemplateDefinition

/**
 * Radial Orbit Chronograph AOD Template Definition.
 * Features a large hour display, dual-chamber highlight capsule with minute indicator,
 * precision rotating seconds orbit dial, and uppercase date/day stack.
 */
object RadialOrbitChronographTemplate : TemplateDefinition {
    override val id: String = "builtin_radial_orbit_chrono"
    override val name: String = "Radial Orbit Chrono"
    override val category: String = "Orbit"
    override val description: String = "Dual-chamber capsule with rotating seconds orbit chronograph dial"
    override val author: String = "AOD Studio"

    override val elements: List<AODElement> = listOf(
        AODElement(
            id = "clock_radial_orbit_chrono",
            name = "Radial Orbit Chronograph",
            type = AODElementType.CLOCK,
            x = 540.0f,
            y = 1100.0f,
            width = 800.0f,
            height = 500.0f,
            zIndex = 1,
            style = AODStyle(
                color = "#FFFFFF",
                accentColor = "#FFFFFF",
                fontSize = 180.0f,
                fontWeight = "BOLD",
                fontFamily = "SANS_SERIF",
                letterSpacing = 1.0f,
                alignment = "CENTER",
                strokeWidth = 3.0f,
                fill = false
            ),
            animation = AODAnimation(type = "CONTINUOUS_ROTATION"),
            properties = mapOf("clockStyle" to "RADIAL_ORBIT")
        )
    )
}
