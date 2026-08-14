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
 * Battery display style and font/icon size.
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val styles = listOf("PERCENTAGE", "BAR", "RING", "ICON")
            val currentStyle = element.properties[AODElement.PROP_BATTERY_STYLE]?.uppercase() ?: "PERCENTAGE"

            styles.forEach { style ->
                FilterChip(
                    selected = currentStyle == style,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put(AODElement.PROP_BATTERY_STYLE, style)
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(style) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 2. Battery Size / Font Size ───────────────────────────────
        Text(
            text = "Battery Text Size: ${element.style.fontSize.toInt()} sp",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.style.fontSize,
            onValueChange = { newSize ->
                onUpdateElement(element.copy(style = element.style.copy(fontSize = newSize)))
            },
            valueRange = 10f..36f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )
    }
}
