package com.aodstudio.app.feature.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.ui.theme.Primary

/**
 * Horizontal Layer Selector Bar — allows switching active element selection
 * or adding new elements to the canvas.
 */
@Composable
fun ElementSelectorRow(
    elements: List<AODElement>,
    selectedElementId: String?,
    onSelectElement: (String) -> Unit,
    onAddElementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ThemeConfig.Spacing.SM.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        elements.forEach { elem ->
            val isSelected = elem.id == selectedElementId
            val icon = when (elem.type) {
                AODElementType.CLOCK -> Icons.Outlined.Schedule
                AODElementType.DATE -> Icons.Outlined.CalendarToday
                AODElementType.BATTERY -> Icons.Outlined.BatteryChargingFull
                AODElementType.NOTIFICATION -> Icons.Outlined.Notifications
                AODElementType.MUSIC -> Icons.Outlined.MusicNote
                AODElementType.TEXT -> Icons.Outlined.TextFields
                AODElementType.IMAGE -> Icons.Outlined.Image
                AODElementType.SHAPE, AODElementType.RING, AODElementType.PROGRESS -> Icons.Outlined.CropSquare
                AODElementType.LINE -> Icons.Outlined.LinearScale
                AODElementType.GROUP -> Icons.Outlined.Category
                AODElementType.FINGERPRINT -> Icons.Outlined.Fingerprint
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectElement(elem.id) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.Black else Primary
                    )
                },
                label = {
                    Text(
                        text = elem.name.ifEmpty { elem.type.name },
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.Black
                )
            )
        }

        // Quick Add Chip
        FilterChip(
            selected = false,
            onClick = onAddElementClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Element",
                    modifier = Modifier.size(16.dp),
                    tint = Primary
                )
            },
            label = { Text("Add Element", color = Primary) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Primary.copy(alpha = 0.15f),
                labelColor = Primary
            )
        )
    }
}
