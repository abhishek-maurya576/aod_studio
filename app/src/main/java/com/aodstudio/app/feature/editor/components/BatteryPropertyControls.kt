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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.ui.theme.Primary

/**
 * Dedicated property controls for BATTERY elements:
 * Battery display style, font/icon size, and stroke/ring thickness.
 */
@Composable
fun BatteryPropertyControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        // ─── 1. Battery Display Style ──────────────────────────────────
        Text(
            text = "Battery Display Style",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )

        data class BatteryStyleOption(val key: String, val label: String)
        val styleOptions = listOf(
            BatteryStyleOption("PERCENTAGE", "% Only"),
            BatteryStyleOption("ICON", "Icon Only"),
            BatteryStyleOption("ICON_PERCENTAGE", "Icon + %"),
            BatteryStyleOption("RING", "Ring Gauge"),
            BatteryStyleOption("BAR", "Battery Bar")
        )

        val currentStyle = element.properties[AODElement.PROP_BATTERY_STYLE]?.uppercase() ?: "PERCENTAGE"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styleOptions.forEach { option ->
                val isSelected = currentStyle == option.key ||
                    (option.key == "ICON_PERCENTAGE" && (currentStyle == "ICON_AND_PERCENTAGE" || currentStyle == "PERCENTAGE_ICON"))
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put(AODElement.PROP_BATTERY_STYLE, option.key)
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 2. Battery Size / Font Size ───────────────────────────────
        Text(
            text = "Battery Size: ${element.style.fontSize.toInt()} sp",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.style.fontSize,
            onValueChange = { newSize ->
                onUpdateElement(element.copy(style = element.style.copy(fontSize = newSize)))
            },
            valueRange = 10f..48f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        // ─── 3. Stroke / Ring Thickness ────────────────────────────────
        val currentStroke = if (element.style.strokeWidth > 0f) element.style.strokeWidth else 4.5f
        Text(
            text = "Stroke / Ring Thickness: ${"%.1f".format(currentStroke)} dp",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = currentStroke,
            onValueChange = { newStroke ->
                onUpdateElement(element.copy(style = element.style.copy(strokeWidth = newStroke)))
            },
            valueRange = 1.5f..12f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )
    }
}
