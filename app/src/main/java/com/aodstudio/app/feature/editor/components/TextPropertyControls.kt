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
import androidx.compose.material3.OutlinedTextField
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
 * Dedicated property controls for TEXT elements:
 * Custom string content, text alignment, typography, and font size.
 */
@Composable
fun TextPropertyControls(
    element: AODElement,
    onUpdateElement: (AODElement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
    ) {
        // ─── 1. Custom Text Input ──────────────────────────────────────
        OutlinedTextField(
            value = element.properties["text"] ?: element.name.ifEmpty { "AOD Studio" },
            onValueChange = { newText ->
                val newProps = element.properties.toMutableMap().apply {
                    put("text", newText)
                }
                onUpdateElement(element.copy(properties = newProps))
            },
            label = { Text("Custom Text Content") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // ─── 2. Text Alignment ─────────────────────────────────────────
        Text(
            text = "Text Alignment",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("LEFT", "CENTER", "RIGHT").forEach { align ->
                FilterChip(
                    selected = element.style.alignment.uppercase() == align,
                    onClick = {
                        onUpdateElement(element.copy(style = element.style.copy(alignment = align)))
                    },
                    label = { Text(align) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ─── 3. Font Family Selector ───────────────────────────────────
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

        // ─── 4. Font Size Slider ───────────────────────────────────────
        Text(
            text = "Font Size: ${element.style.fontSize.toInt()} sp",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = element.style.fontSize,
            onValueChange = { newSize ->
                onUpdateElement(element.copy(style = element.style.copy(fontSize = newSize)))
            },
            valueRange = 12f..100f,
            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
        )
    }
}
