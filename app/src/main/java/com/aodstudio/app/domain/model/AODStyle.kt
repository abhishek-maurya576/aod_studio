package com.aodstudio.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Styling properties for individual AOD elements.
 * All visual attributes are customizable per element.
 */
@Serializable
data class AODStyle(
    val color: String = "#FFFFFF",
    val accentColor: String = "#E8A838",
    val backgroundColor: String = "#00000000", // Transparent by default
    val fontSize: Float = 24f,
    val fontWeight: String = "NORMAL",        // THIN, LIGHT, NORMAL, MEDIUM, SEMIBOLD, BOLD
    val fontFamily: String = "DEFAULT",       // DEFAULT, MONO, DISPLAY
    val letterSpacing: Float = 0f,
    val alignment: String = "CENTER",          // LEFT, CENTER, RIGHT
    val strokeWidth: Float = 2f,
    val fill: Boolean = true,
    val cornerRadius: Float = 0f
)
