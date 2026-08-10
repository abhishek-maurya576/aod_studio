package com.aodstudio.app.feature.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceVariant

/**
 * Property inspector panel for editing selected element attributes
 * (X/Y position sliders, size, colors, typography, formats, delete).
 */
@Composable
fun PropertyPanel(
    element: AODElement?,
    onUpdateElement: (AODElement) -> Unit,
    onDeleteElement: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (element == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ThemeConfig.Spacing.MD.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap any element on canvas to inspect and edit properties",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThemeConfig.Spacing.MD.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Element Name & Type + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = element.name.ifEmpty { element.type.name },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Type: ${element.type.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary
                    )
                }

                Button(
                    onClick = onDeleteElement,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.SM.dp))

            // Position X Slider
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

            // Position Y Slider
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

            // Font Size Slider (for text/clock elements)
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
                valueRange = 12f..120f,
                colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
            )

            // Hex Color Input
            OutlinedTextField(
                value = element.style.color,
                onValueChange = { newColor ->
                    onUpdateElement(element.copy(style = element.style.copy(color = newColor)))
                },
                label = { Text("Color (Hex #RRGGBB)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
