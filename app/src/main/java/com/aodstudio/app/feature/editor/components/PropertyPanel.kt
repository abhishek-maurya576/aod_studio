package com.aodstudio.app.feature.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElement
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceVariant

/**
 * Generic Property Panel for AOD Studio Editor.
 * Dynamically composes modular element property editors based on the selected element's type,
 * coupled with common position, scale, rotation, opacity, and color palette controls.
 */
@Composable
fun PropertyPanel(
    elements: List<AODElement>,
    selectedElementId: String?,
    onSelectElement: (String) -> Unit,
    onUpdateElement: (AODElement) -> Unit,
    onDeleteElement: () -> Unit,
    onAddElementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedElement = elements.firstOrNull { it.id == selectedElementId }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ThemeConfig.Spacing.MD.dp)
        ) {
            // ─── 1. Horizontal Layer Selector Bar ────────────────────────
            ElementSelectorRow(
                elements = elements,
                selectedElementId = selectedElementId,
                onSelectElement = onSelectElement,
                onAddElementClick = onAddElementClick
            )

            if (selectedElement == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ThemeConfig.Spacing.MD.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap any element chip above to customize its properties",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }

            // ─── 2. Header Row: Element Name & Type + Delete Button ──────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ThemeConfig.Spacing.SM.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedElement.name.ifEmpty { selectedElement.type.name },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Type: ${selectedElement.type.name}",
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

            // ─── 3. Scrollable Inspector Body ───────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.MD.dp)
            ) {
                // Type-Specific Property Controls (Extensible Dispatcher)
                when (selectedElement.type) {
                    AODElementType.CLOCK -> {
                        ClockPropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    AODElementType.DATE -> {
                        DatePropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    AODElementType.BATTERY -> {
                        BatteryPropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    AODElementType.NOTIFICATION -> {
                        NotificationPropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    AODElementType.MUSIC -> {
                        MusicPropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    AODElementType.TEXT -> {
                        TextPropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    AODElementType.SHAPE, AODElementType.LINE, AODElementType.RING, AODElementType.PROGRESS -> {
                        ShapePropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                    else -> {
                        // Fallback for custom or future element types
                        TextPropertyControls(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                }

                // Generic / Common Element Controls (Position, Scale, Rotation, Opacity, Color)
                CommonElementControls(
                    element = selectedElement,
                    onUpdateElement = onUpdateElement
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
