package com.aodstudio.app.feature.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aodstudio.app.aod.renderer.RendererUtils
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.SurfaceVariant

/**
 * Reusable common controls for all visual AOD elements:
 * Position presets, X/Y coordinate sliders, scale, rotation, opacity, and color palette.
 */
@Composable
fun CommonElementControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        // ─── 1. Position Presets ───────────────────────────────────────
        Text(
            text = "Position Presets",
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
            listOf("TOP", "CENTER", "BOTTOM", "LEFT", "RIGHT", "TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT").forEach { preset ->
                FilterChip(
                    selected = false,
                    onClick = {
                        val (newX, newY) = when (preset) {
                            "TOP" -> 540f to 450f
                            "BOTTOM" -> 540f to 1800f
                            "LEFT" -> 270f to element.y
                            "RIGHT" -> 810f to element.y
                            "TOP_LEFT" -> 270f to 450f
                            "TOP_RIGHT" -> 810f to 450f
                            "BOTTOM_LEFT" -> 270f to 1800f
                            "BOTTOM_RIGHT" -> 810f to 1800f
                            else -> 540f to 1200f // CENTER
                        }
                        onUpdateElement(element.copy(x = newX, y = newY))
                    },
                    label = { Text(preset.replace("_", " ")) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // ─── 2. Position X & Y Sliders ─────────────────────────────────
        Text(
            text = "Position X: ${element.x.toInt()} px",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.x,
            onValueChange = { newX -> onUpdateElement(element.copy(x = newX)) },
            valueRange = 0f..1080f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        Text(
            text = "Position Y: ${element.y.toInt()} px",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.y,
            onValueChange = { newY -> onUpdateElement(element.copy(y = newY)) },
            valueRange = 0f..2400f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        // ─── 3. Scale & Rotation Sliders ───────────────────────────────
        Text(
            text = "Element Scale: ${"%.2f".format(element.scale)}x",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.scale,
            onValueChange = { newScale -> onUpdateElement(element.copy(scale = newScale)) },
            valueRange = 0.5f..3.0f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        Text(
            text = "Rotation: ${element.rotation.toInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.rotation,
            onValueChange = { newRot -> onUpdateElement(element.copy(rotation = newRot)) },
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        // ─── 4. Opacity / Brightness Slider ────────────────────────────
        Text(
            text = "Brightness / Opacity: ${(element.opacity * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.opacity,
            onValueChange = { newOpacity -> onUpdateElement(element.copy(opacity = newOpacity)) },
            valueRange = 0.1f..1.0f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )

        // ─── 5. Color Swatches ─────────────────────────────────────────
        Text(
            text = "Color Palette",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val swatches = listOf(
                "#E8A838", // Amber
                "#5EC98A", // Emerald
                "#E87C7C", // Rose
                "#E2E8F0", // Slate
                "#38BDF8", // Cyan
                "#A855F7", // Purple
                "#FFFFFF", // Pure White
                "#99FFFFFF" // 60% Dim White
            )

            swatches.forEach { hex ->
                val colorInt = RendererUtils.parseColor(hex)
                val isSelected = element.style.color.equals(hex, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            onUpdateElement(element.copy(style = element.style.copy(color = hex)))
                        }
                )
            }
        }

        // ─── 6. Custom Hex Input ───────────────────────────────────────
        OutlinedTextField(
            value = element.style.color,
            onValueChange = { newColor ->
                onUpdateElement(element.copy(style = element.style.copy(color = newColor)))
            },
            label = { Text("Custom Color (Hex #RRGGBB)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
