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
 * Dedicated property controls for NOTIFICATION elements:
 * Visibility mode, icon scale slider, and maximum notification count.
 */
@Composable
fun NotificationPropertyControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        // ─── 1. Visibility Mode ────────────────────────────────────────
        Text(
            text = "Notification Visibility",
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
            val visibilities = listOf("ICONS_ONLY", "COUNT_BADGE", "DETAILED")
            val currentVis = element.properties["visibilityMode"]?.uppercase() ?: "ICONS_ONLY"

            visibilities.forEach { vis ->
                FilterChip(
                    selected = currentVis == vis,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put("visibilityMode", vis)
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(vis.replace("_", " ")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 2. Notification Icon Scale Slider ─────────────────────────
        Text(
            text = "Notification Icon Scale: ${"%.1f".format(element.scale)}x",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Slider(
            value = element.scale,
            onValueChange = { newScale ->
                onUpdateElement(element.copy(scale = newScale))
            },
            valueRange = 0.5f..3.0f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )
    }
}
