package com.aodstudio.app.feature.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceVariant

/**
 * Property inspector panel for editing selected element attributes.
 * Includes Layer Selector Bar, Position Presets (Top, Center, Bottom, Left, Right), Text & Music controls (Android 16 Squiggly Wave),
 * Fancy Fonts, Color Swatches, Sliders, and Hex input.
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
            // ─── 1. Element Layer Selector Bar (Horizontal Chips) ─────────────
            Row(
                modifier = Modifier
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
                        else -> Icons.Outlined.TextFields
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
                        label = { Text(elem.name.ifEmpty { elem.type.name }) },
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
                    label = { Text("Add New") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Primary.copy(alpha = 0.15f),
                        labelColor = Primary
                    )
                )
            }

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

            // ─── 2. Sticky Header Row: Element Name & Type + Delete Button ───
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

            // ─── 3. Scrollable Body: Customization Controls ───
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
            ) {
                // ─── Position Quick Presets (Top, Center, Bottom, Left, Right) ──
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
                    listOf("TOP", "CENTER", "BOTTOM", "LEFT", "RIGHT").forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val (newX, newY) = when (preset) {
                                    "TOP" -> 540f to 400f
                                    "BOTTOM" -> 540f to 1800f
                                    "LEFT" -> 250f to selectedElement.y
                                    "RIGHT" -> 830f to selectedElement.y
                                    else -> 540f to 1200f
                                }
                                onUpdateElement(selectedElement.copy(x = newX, y = newY))
                            },
                            label = { Text(preset) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // ─── Element Specific Controls ───────────────────────

                // A. TEXT & DATE Specific Controls
                if (selectedElement.type == AODElementType.TEXT || selectedElement.type == AODElementType.DATE) {
                    if (selectedElement.type == AODElementType.TEXT) {
                        OutlinedTextField(
                            value = selectedElement.properties["text"] ?: "AOD Studio",
                            onValueChange = { newText ->
                                val newProps = selectedElement.properties.toMutableMap().apply {
                                    put("text", newText)
                                }
                                onUpdateElement(selectedElement.copy(properties = newProps))
                            },
                            label = { Text("Custom Text Content") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (selectedElement.type == AODElementType.DATE) {
                        Text(
                            text = "Date Format",
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
                            val formats = listOf("EEE • MMM dd", "EEEE, MMMM dd", "dd/MM/yyyy", "MMM dd, yyyy")
                            val currentFormat = selectedElement.properties["format"] ?: "EEE • MMM dd"

                            formats.forEach { fmt ->
                                FilterChip(
                                    selected = currentFormat == fmt,
                                    onClick = {
                                        val newProps = selectedElement.properties.toMutableMap().apply {
                                            put("format", fmt)
                                        }
                                        onUpdateElement(selectedElement.copy(properties = newProps))
                                    },
                                    label = { Text(fmt) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // Text Alignment Selector
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
                                selected = selectedElement.style.alignment.uppercase() == align,
                                onClick = {
                                    onUpdateElement(selectedElement.copy(style = selectedElement.style.copy(alignment = align)))
                                },
                                label = { Text(align) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // B. MUSIC Specific Controls (Android 16 Material You Wave Specs)
                if (selectedElement.type == AODElementType.MUSIC) {
                    // Player Size Toggle (Small vs Large Mode)
                    Text(
                        text = "Player Size",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentSize = selectedElement.properties[AODElement.PROP_PLAYER_SIZE]?.uppercase() ?: "LARGE"
                        listOf("SMALL", "LARGE").forEach { size ->
                            FilterChip(
                                selected = currentSize == size,
                                onClick = {
                                    val newProps = selectedElement.properties.toMutableMap().apply {
                                        put(AODElement.PROP_PLAYER_SIZE, size)
                                        if (size == "SMALL") {
                                            put("showAlbumArt", "false")
                                        } else {
                                            put("showAlbumArt", "true")
                                        }
                                    }
                                    onUpdateElement(selectedElement.copy(properties = newProps))
                                },
                                label = { Text(if (size == "SMALL") "Small Mode" else "Large Mode") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

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
                        val currentStyle = selectedElement.properties["musicStyle"]?.uppercase() ?: "WAVY_PROGRESS"

                        styles.forEach { style ->
                            FilterChip(
                                selected = currentStyle == style,
                                onClick = {
                                    val newProps = selectedElement.properties.toMutableMap().apply {
                                        put("musicStyle", style)
                                    }
                                    onUpdateElement(selectedElement.copy(properties = newProps))
                                },
                                label = { Text(style) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Wave Intensity Selector
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
                            val currentIntensity = selectedElement.properties["waveIntensity"]?.uppercase() ?: "MEDIUM"
                            FilterChip(
                                selected = currentIntensity == intensity,
                                onClick = {
                                    val newProps = selectedElement.properties.toMutableMap().apply {
                                        put("waveIntensity", intensity)
                                    }
                                    onUpdateElement(selectedElement.copy(properties = newProps))
                                },
                                label = { Text(intensity) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Widget Scale / Size Slider for Music Widget
                    Text(
                        text = "Widget Scale: ${"%.1f".format(selectedElement.scale)}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Slider(
                        value = selectedElement.scale,
                        onValueChange = { newScale ->
                            onUpdateElement(selectedElement.copy(scale = newScale))
                        },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                    )

                    // Show Album Art Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Album Thumbnail", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = selectedElement.properties["showAlbumArt"]?.toBoolean() ?: true,
                            onCheckedChange = { checked ->
                                val newProps = selectedElement.properties.toMutableMap().apply {
                                    put("showAlbumArt", checked.toString())
                                }
                                onUpdateElement(selectedElement.copy(properties = newProps))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = SurfaceVariant
                            )
                        )
                    }

                    // Show Controls Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Media Control Buttons", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = selectedElement.properties["showControls"]?.toBoolean() ?: true,
                            onCheckedChange = { checked ->
                                val newProps = selectedElement.properties.toMutableMap().apply {
                                    put("showControls", checked.toString())
                                }
                                onUpdateElement(selectedElement.copy(properties = newProps))
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

                // C. CLOCK Specific Controls
                if (selectedElement.type == AODElementType.CLOCK) {
                    Text(
                        text = "Clock Style",
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
                        val styles = listOf("DIGITAL", "ANALOG", "STACKED", "RADIAL", "ORBIT")
                        val currentStyle = selectedElement.properties["clockStyle"]?.uppercase() ?: "DIGITAL"

                        styles.forEach { style ->
                            FilterChip(
                                selected = currentStyle == style,
                                onClick = {
                                    val newProps = selectedElement.properties.toMutableMap().apply {
                                        put("clockStyle", style)
                                    }
                                    onUpdateElement(selectedElement.copy(properties = newProps))
                                },
                                label = { Text(style) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // D. BATTERY Specific Controls
                if (selectedElement.type == AODElementType.BATTERY) {
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
                        val styles = listOf("PERCENTAGE", "BAR", "RING")
                        val currentStyle = selectedElement.properties["batteryStyle"]?.uppercase() ?: "PERCENTAGE"

                        styles.forEach { style ->
                            FilterChip(
                                selected = currentStyle == style,
                                onClick = {
                                    val newProps = selectedElement.properties.toMutableMap().apply {
                                        put("batteryStyle", style)
                                    }
                                    onUpdateElement(selectedElement.copy(properties = newProps))
                                },
                                label = { Text(style) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // E. NOTIFICATION Specific Controls
                if (selectedElement.type == AODElementType.NOTIFICATION) {
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
                        val currentVis = selectedElement.properties["visibilityMode"]?.uppercase() ?: "COUNT_BADGE"

                        visibilities.forEach { vis ->
                            FilterChip(
                                selected = currentVis == vis,
                                onClick = {
                                    val newProps = selectedElement.properties.toMutableMap().apply {
                                        put("visibilityMode", vis)
                                    }
                                    onUpdateElement(selectedElement.copy(properties = newProps))
                                },
                                label = { Text(vis) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // ─── Fancy Font Style Selector ─────────────────────────
                Text(
                    text = "Fancy Font Style",
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
                    val fancyFonts = listOf("DEFAULT", "MONO", "SERIF", "DISPLAY", "CYBER", "CURSIVE", "NEON")
                    val currentFont = selectedElement.style.fontFamily.uppercase()

                    fancyFonts.forEach { font ->
                        FilterChip(
                            selected = currentFont == font,
                            onClick = {
                                onUpdateElement(selectedElement.copy(style = selectedElement.style.copy(fontFamily = font)))
                            },
                            label = { Text(font) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // ─── Color Swatch Selector ─────────────────────────────
                Text(
                    text = "Color Swatches",
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
                        "#FFFFFF"  // Pure White
                    )

                    swatches.forEach { hex ->
                        val colorInt = RendererUtils.parseColor(hex)
                        val isSelected = selectedElement.style.color.equals(hex, ignoreCase = true)

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
                                    onUpdateElement(selectedElement.copy(style = selectedElement.style.copy(color = hex)))
                                }
                        )
                    }
                }

                // ─── Position X Slider ──────────────────────────────────
                Text(
                    text = "Position X: ${selectedElement.x.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = selectedElement.x,
                    onValueChange = { newX -> onUpdateElement(selectedElement.copy(x = newX)) },
                    valueRange = 0f..1080f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // ─── Position Y Slider ──────────────────────────────────
                Text(
                    text = "Position Y: ${selectedElement.y.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = selectedElement.y,
                    onValueChange = { newY -> onUpdateElement(selectedElement.copy(y = newY)) },
                    valueRange = 0f..2400f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // ─── Font Size Slider ───────────────────────────────────
                Text(
                    text = "Font Size: ${selectedElement.style.fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = selectedElement.style.fontSize,
                    onValueChange = { newSize ->
                        onUpdateElement(selectedElement.copy(style = selectedElement.style.copy(fontSize = newSize)))
                    },
                    valueRange = 12f..140f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // ─── Opacity / Brightness Slider ────────────────────────
                Text(
                    text = "Brightness / Opacity: ${(selectedElement.opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = selectedElement.opacity,
                    onValueChange = { newOpacity ->
                        onUpdateElement(selectedElement.copy(opacity = newOpacity))
                    },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )

                // ─── Custom Hex Input ──────────────────────────────────
                OutlinedTextField(
                    value = selectedElement.style.color,
                    onValueChange = { newColor ->
                        onUpdateElement(selectedElement.copy(style = selectedElement.style.copy(color = newColor)))
                    },
                    label = { Text("Custom Color (Hex #RRGGBB)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
