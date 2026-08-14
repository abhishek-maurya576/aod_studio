package com.aodstudio.app.feature.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.SurfaceVariant

/**
 * Dedicated, type-tailored property controls for CLOCK elements.
 * Each clock style (Analog, Digital, Stacked, Orbit) exposes only its relevant properties:
 * - Analog clocks expose dial diameter, marker types (ticks/dots/none), and hand stroke width.
 * - Digital clocks expose 12h/24h formats, font family, and font size.
 * - Stacked clocks expose display typography and stacked font sizes.
 * - Orbit/Radial clocks expose dial diameter and ring stroke widths.
 */
@Composable
fun ClockPropertyControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    val clockStyle = element.properties["clockStyle"]?.uppercase() ?: "DIGITAL"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        when (clockStyle) {
            "ANALOG" -> {
                // ─── A. ANALOG CLOCK CONTROLS ─────────────────────────

                // 1. Clock Face Diameter / Size
                Text(
                    text = "Clock Face Diameter: ${element.width.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.width,
                    onValueChange = { newSize ->
                        onUpdateElement(element.copy(width = newSize, height = newSize))
                    },
                    valueRange = 250f..700f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // 2. Marker Type (Ticks vs Dots vs Clean)
                Text(
                    text = "Dial Markers",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val markerTypes = listOf("TICKS", "DOTS", "NONE")
                    val currentMarker = element.properties["markerType"]?.uppercase() ?: "TICKS"

                    markerTypes.forEach { marker ->
                        FilterChip(
                            selected = currentMarker == marker,
                            onClick = {
                                val newProps = element.properties.toMutableMap().apply {
                                    put("markerType", marker)
                                    put("showMarkers", (marker != "NONE").toString())
                                }
                                onUpdateElement(element.copy(properties = newProps))
                            },
                            label = { Text(marker) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // 3. Hand Stroke Width Slider
                Text(
                    text = "Clock Hand Thickness: ${element.style.strokeWidth.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.style.strokeWidth,
                    onValueChange = { newWidth ->
                        onUpdateElement(element.copy(style = element.style.copy(strokeWidth = newWidth)))
                    },
                    valueRange = 1f..12f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // 4. Show Seconds Hand Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Second Hand", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = element.properties[AODElement.PROP_SHOW_SECONDS]?.toBoolean() ?: true,
                        onCheckedChange = { checked ->
                            val newProps = element.properties.toMutableMap().apply {
                                put(AODElement.PROP_SHOW_SECONDS, checked.toString())
                            }
                            onUpdateElement(element.copy(properties = newProps))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }
            }

            "STACKED", "TYPOGRAPHY" -> {
                // ─── B. STACKED / TYPOGRAPHY CLOCK CONTROLS ───────────

                // 1. Font Family Selector
                Text(
                    text = "Typography Font",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fonts = listOf("DISPLAY", "MONO", "DEFAULT", "CYBER", "NEON", "CURSIVE")
                    val currentFont = element.style.fontFamily.uppercase()

                    fonts.forEach { font ->
                        FilterChip(
                            selected = currentFont == font,
                            onClick = {
                                onUpdateElement(element.copy(style = element.style.copy(fontFamily = font)))
                            },
                            label = { Text(font) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // 2. Font Size Slider
                Text(
                    text = "Stacked Digits Size: ${element.style.fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.style.fontSize,
                    onValueChange = { newSize ->
                        onUpdateElement(element.copy(style = element.style.copy(fontSize = newSize)))
                    },
                    valueRange = 70f..160f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )
            }

            "ORBIT", "RADIAL" -> {
                // ─── C. ORBIT / RADIAL CLOCK CONTROLS ─────────────────

                // 1. Dial Diameter Slider
                Text(
                    text = "Orbit Dial Diameter: ${element.width.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.width,
                    onValueChange = { newSize ->
                        onUpdateElement(element.copy(width = newSize, height = newSize))
                    },
                    valueRange = 250f..650f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // 2. Ring Stroke Width Slider
                Text(
                    text = "Orbit Ring Thickness: ${element.style.strokeWidth.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.style.strokeWidth,
                    onValueChange = { newWidth ->
                        onUpdateElement(element.copy(style = element.style.copy(strokeWidth = newWidth)))
                    },
                    valueRange = 1f..10f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // 3. Center Digits Font Size
                Text(
                    text = "Time Digits Font Size: ${element.style.fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.style.fontSize,
                    onValueChange = { newSize ->
                        onUpdateElement(element.copy(style = element.style.copy(fontSize = newSize)))
                    },
                    valueRange = 24f..72f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )
            }

            else -> {
                // ─── D. DIGITAL CLOCK CONTROLS (DEFAULT) ──────────────

                // 1. Time Format Presets
                Text(
                    text = "Time Format",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formats = listOf("HH:mm", "hh:mm a", "HH:mm:ss", "h:mm", "H:mm")
                    val currentFormat = element.properties[AODElement.PROP_FORMAT] ?: "HH:mm"

                    formats.forEach { fmt ->
                        FilterChip(
                            selected = currentFormat == fmt,
                            onClick = {
                                val newProps = element.properties.toMutableMap().apply {
                                    put(AODElement.PROP_FORMAT, fmt)
                                }
                                onUpdateElement(element.copy(properties = newProps))
                            },
                            label = { Text(fmt) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // 2. Font Family Selector
                Text(
                    text = "Font Family",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fonts = listOf("DEFAULT", "MONO", "SERIF", "DISPLAY", "CYBER", "CURSIVE", "NEON")
                    val currentFont = element.style.fontFamily.uppercase()

                    fonts.forEach { font ->
                        FilterChip(
                            selected = currentFont == font,
                            onClick = {
                                onUpdateElement(element.copy(style = element.style.copy(fontFamily = font)))
                            },
                            label = { Text(font) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // 3. Font Size Slider
                Text(
                    text = "Clock Font Size: ${element.style.fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = element.style.fontSize,
                    onValueChange = { newSize ->
                        onUpdateElement(element.copy(style = element.style.copy(fontSize = newSize)))
                    },
                    valueRange = 36f..140f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // 4. Show Seconds Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Seconds Indicator", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = element.properties["showSeconds"]?.toBoolean() ?: false,
                        onCheckedChange = { checked ->
                            val newProps = element.properties.toMutableMap().apply {
                                put("showSeconds", checked.toString())
                            }
                            onUpdateElement(element.copy(properties = newProps))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
