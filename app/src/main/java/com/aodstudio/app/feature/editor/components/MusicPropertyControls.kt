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
 * Dedicated property controls for MUSIC elements:
 * Player size, Android 16 wavy progress styles, wave amplitude, album thumbnail toggle, and media buttons.
 */
@Composable
fun MusicPropertyControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        // ─── 1. Player Size (Small vs Large Mode) ──────────────────────
        Text(
            text = "Player Size Mode",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val currentSize = element.properties[AODElement.PROP_PLAYER_SIZE]?.uppercase() ?: "LARGE"
            listOf("SMALL", "LARGE").forEach { size ->
                FilterChip(
                    selected = currentSize == size,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put(AODElement.PROP_PLAYER_SIZE, size)
                            put("showAlbumArt", if (size == "SMALL") "false" else "true")
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(if (size == "SMALL") "Compact Mode" else "Expanded Mode") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 2. Android 16 Music Player Style ──────────────────────────
        Text(
            text = "Android 16 Music Player Style",
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
            val styles = listOf("WAVY_PROGRESS", "CLASSIC", "COMPACT", "MINIMAL", "NEON_WAVE")
            val currentStyle = element.properties["musicStyle"]?.uppercase() ?: "WAVY_PROGRESS"

            styles.forEach { style ->
                FilterChip(
                    selected = currentStyle == style,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put("musicStyle", style)
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(style.replace("_", " ")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 3. Squiggly Wave Amplitude ────────────────────────────────
        Text(
            text = "Squiggly Wave Amplitude",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("LOW", "MEDIUM", "HIGH").forEach { intensity ->
                val currentIntensity = element.properties["waveIntensity"]?.uppercase() ?: "MEDIUM"
                FilterChip(
                    selected = currentIntensity == intensity,
                    onClick = {
                        val newProps = element.properties.toMutableMap().apply {
                            put("waveIntensity", intensity)
                        }
                        onUpdateElement(element.copy(properties = newProps))
                    },
                    label = { Text(intensity) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 4. Show Album Thumbnail Toggle ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show Album Thumbnail", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = element.properties["showAlbumArt"]?.toBoolean() ?: true,
                onCheckedChange = { checked ->
                    val newProps = element.properties.toMutableMap().apply {
                        put("showAlbumArt", checked.toString())
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

        // ─── 5. Show Controls Toggle ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show Media Control Buttons", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = element.properties["showControls"]?.toBoolean() ?: true,
                onCheckedChange = { checked ->
                    val newProps = element.properties.toMutableMap().apply {
                        put("showControls", checked.toString())
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
