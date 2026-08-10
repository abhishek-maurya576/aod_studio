package com.aodstudio.app.domain.model

import com.aodstudio.app.core.util.generateId
import kotlinx.serialization.Serializable

/**
 * Fundamental visual component of an AOD theme.
 * Every clock digit, date string, battery bar, or shape is an AODElement.
 */
@Serializable
data class AODElement(
    val id: String = generateId(),
    val name: String = "",
    val type: AODElementType,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 200f,
    val height: Float = 100f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val visibility: Boolean = true,
    val zIndex: Int = 0,
    val style: AODStyle = AODStyle(),
    val animation: AODAnimation = AODAnimation(),
    val properties: Map<String, String> = emptyMap()
) {
    companion object {
        // Known property keys
        const val PROP_FORMAT = "format"            // e.g. "HH:mm", "hh:mm a", "MON • AUG 10"
        const val PROP_TEXT = "text"                // Custom text content
        const val PROP_SHAPE_TYPE = "shapeType"      // CIRCLE, RECTANGLE, LINE, RING, ARC
        const val PROP_SHOW_SECONDS = "showSeconds"  // "true" / "false"
        const val PROP_BATTERY_STYLE = "batteryStyle"// PERCENTAGE, ICON, RING, BAR
        const val PROP_MUSIC_INFO = "musicInfo"      // TITLE, ARTIST, ALBUM, ARTWORK
        const val PROP_NOTIF_MAX_COUNT = "maxNotifs" // e.g. "5"
    }
}
