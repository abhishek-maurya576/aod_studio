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
 * Dedicated property controls for SHAPE, RING, LINE, and PROGRESS elements:
 * Shape geometry, stroke width, fill toggle, and corner radius.
 */
@Composable
fun ShapePropertyControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        // ─── 1. Shape Type Selector ────────────────────────────────────
        Text(
            text = "Shape Geometry",
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
            val shapes = listOf("CIRCLE", "RECTANGLE", "LINE", "RING", "ARC")
            val currentShape = element.properties[AODElement.PROP_SHAPE_TYPE]?.uppercase() ?: "RECTANGLE"

            shapes.forEach { shape ->
                FilterChip(
                    selected = currentShape == shape,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put(AODElement.PROP_SHAPE_TYPE, shape)
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(shape) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 2. Fill Geometry Toggle ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Solid Fill (vs Outline Stroke)", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = element.style.fill,
                onCheckedChange = { fill ->
                    onUpdateElement(element.copy(style = element.style.copy(fill = fill)))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = SurfaceVariant
                )
            )
        }

        // ─── 3. Stroke Width Slider ────────────────────────────────────
        Text(
            text = "Stroke Width: ${element.style.strokeWidth.toInt()} px",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.style.strokeWidth,
            onValueChange = { newWidth ->
                onUpdateElement(element.copy(style = element.style.copy(strokeWidth = newWidth)))
            },
            valueRange = 1f..30f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        // ─── 4. Corner Radius Slider ───────────────────────────────────
        Text(
            text = "Corner Radius: ${element.style.cornerRadius.toInt()} px",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.style.cornerRadius,
            onValueChange = { newRadius ->
                onUpdateElement(element.copy(style = element.style.copy(cornerRadius = newRadius)))
            },
            valueRange = 0f..80f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )
    }
}
