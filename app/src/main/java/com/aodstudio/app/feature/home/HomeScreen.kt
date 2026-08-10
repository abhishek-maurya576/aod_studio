package com.aodstudio.app.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.PrimaryVariant
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceVariant
import com.aodstudio.app.ui.theme.Tertiary

/**
 * Home screen — the main landing page of AOD Studio.
 *
 * Layout:
 *   - App header with branding
 *   - AOD preview card (shows current/last active theme)
 *   - Quick action buttons (Create, Themes, Settings)
 *   - Status indicator (AOD active/inactive)
 */
@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit = {},
    onNavigateToThemes: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ThemeConfig.Spacing.MD.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XXXL.dp))

        // ─── App Header ────────────────────────────────────────────
        Text(
            text = "AOD Studio",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XXS.dp))

        Text(
            text = "Design your Always-On Display",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XL.dp))

        // ─── AOD Preview Card ──────────────────────────────────────
        AODPreviewCard()

        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.LG.dp))

        // ─── Quick Actions ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Add,
                label = "Create",
                accentColor = Primary,
                onClick = { onNavigateToEditor(null) }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ColorLens,
                label = "Themes",
                accentColor = Secondary,
                onClick = onNavigateToThemes
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Settings,
                label = "Settings",
                accentColor = Tertiary,
                onClick = onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.LG.dp))

        // ─── Status Card ───────────────────────────────────────────
        StatusCard(isAodActive = false, onNavigateToSettings = onNavigateToSettings)

        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XL.dp))
    }
}

/**
 * Preview card that shows a miniature version of the active AOD theme.
 * Currently displays a placeholder clock design.
 */
@Composable
private fun AODPreviewCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        shape = RoundedCornerShape(ThemeConfig.Radius.XL.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ThemeConfig.Elevation.MD.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder clock display
                Text(
                    text = "12:45",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

                Text(
                    text = "MON \u2022 AUG 10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(ThemeConfig.Spacing.MD.dp))

                // Battery indicator placeholder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Primary, PrimaryVariant)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(ThemeConfig.Spacing.XS.dp))
                    Text(
                        text = "78%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Subtle corner label
            Text(
                text = "Preview",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ThemeConfig.Spacing.MD.dp)
            )
        }
    }
}

/**
 * Quick action card with icon, label, and accent color.
 * Includes press animation for tactile feedback.
 */
@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            accentColor.copy(alpha = 0.15f)
        } else {
            SurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_bg"
    )

    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(ThemeConfig.Radius.LG.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ThemeConfig.Spacing.SM.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Status card showing whether the AOD service is currently active.
 */
@Composable
private fun StatusCard(isAodActive: Boolean, onNavigateToSettings: () -> Unit) {
    val statusColor = if (isAodActive) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToSettings),
        shape = RoundedCornerShape(ThemeConfig.Radius.MD.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThemeConfig.Spacing.MD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(ThemeConfig.Spacing.SM.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAodActive) "AOD is active" else "AOD is inactive",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isAodActive) "Your custom display is running"
                    else "Tap to configure and enable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.Dashboard,
                contentDescription = "Status",
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
