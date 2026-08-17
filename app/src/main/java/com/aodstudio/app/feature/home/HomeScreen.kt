package com.aodstudio.app.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceContainer
import com.aodstudio.app.ui.theme.SurfaceVariant
import com.aodstudio.app.ui.theme.Tertiary
import kotlinx.coroutines.launch

/**
 * Home screen — main landing page of AOD Studio.
 * Features a device-framed live AOD preview card with animated swipe-down reveal & tap to edit,
 * quick action buttons, and active status monitor.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToEditor: (String?) -> Unit = {},
    onNavigateToThemes: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

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

        // ─── AOD Preview Card with Animated Swipe-Down & Tap ───────
        AODPreviewCard(
            activeTheme = uiState.activeTheme,
            isLoading = uiState.isLoading,
            viewModel = viewModel,
            onClick = { onNavigateToEditor(uiState.activeTheme?.id) }
        )

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
        StatusCard(
            isAodActive = uiState.isAodActive,
            onNavigateToSettings = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XL.dp))
    }
}

/**
 * Preview card that renders live AODRenderView inside an authentic device-proportional phone frame.
 * Features an animated swipe-down drag physics & reveal transition to smoothly open the editor.
 */
@Composable
private fun AODPreviewCard(
    activeTheme: AODTheme?,
    isLoading: Boolean,
    viewModel: HomeViewModel,
    onClick: () -> Unit
) {
    val dragOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        if (dragOffsetY.value > 70f) {
                            coroutineScope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = 200f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                                onClick()
                                dragOffsetY.snapTo(0f)
                            }
                        } else {
                            coroutineScope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffsetY.animateTo(0f, spring())
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount > 0 || dragOffsetY.value > 0f) {
                            change.consume()
                            val newY = (dragOffsetY.value + dragAmount * 0.7f).coerceIn(0f, 220f)
                            coroutineScope.launch {
                                dragOffsetY.snapTo(newY)
                            }
                        }
                    }
                )
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ThemeConfig.Radius.XL.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pull-down reveal banner behind/above the phone frame
            val revealAlpha = (dragOffsetY.value / 60f).coerceIn(0f, 1f)
            if (revealAlpha > 0.05f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .graphicsLayer { alpha = revealAlpha }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Release to Edit Theme",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(color = Primary)
            } else {
                activeTheme?.let { theme ->
                    val canvasW = if (theme.canvas.width > 0) theme.canvas.width.toFloat() else 1080f
                    val canvasH = if (theme.canvas.height > 0) theme.canvas.height.toFloat() else 2400f
                    val phoneAspectRatio = canvasW / canvasH

                    // Device-Proportional Phone Frame with swipe-down animated displacement
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(phoneAspectRatio)
                            .graphicsLayer {
                                translationY = dragOffsetY.value
                                val scaleFactor = 1f - (dragOffsetY.value / 3000f)
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                            }
                            .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp))
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF383838),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black)
                    ) {
                        // Live AOD Canvas RenderView scaled to 100% of device frame
                        AndroidView(
                            factory = { context ->
                                AODRenderView(context).apply {
                                    setBatteryRepository(viewModel.batteryRepository)
                                    setNotificationRepository(viewModel.notificationRepository)
                                    setMediaRepository(viewModel.mediaRepository)
                                    setTheme(theme)
                                }
                            },
                            update = { renderView ->
                                renderView.setTheme(theme)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Camera punch-hole indicator at top center
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(7.dp)
                                .align(Alignment.TopCenter)
                                .background(Color(0xFF222222), shape = CircleShape)
                                .border(0.5.dp, Color(0xFF3A3A3A), CircleShape)
                        )

                        // Gesture interceptor overlay on the phone frame for tap & swipe-down
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures(
                                        onDragStart = {},
                                        onDragEnd = {
                                            if (dragOffsetY.value > 70f) {
                                                coroutineScope.launch {
                                                    dragOffsetY.animateTo(
                                                        targetValue = 200f,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                    )
                                                    onClick()
                                                    dragOffsetY.snapTo(0f)
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    dragOffsetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                                            stiffness = Spring.StiffnessLow
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                dragOffsetY.animateTo(0f, spring())
                                            }
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            if (dragAmount > 0 || dragOffsetY.value > 0f) {
                                                change.consume()
                                                val newY = (dragOffsetY.value + dragAmount * 0.7f).coerceIn(0f, 220f)
                                                coroutineScope.launch {
                                                    dragOffsetY.snapTo(newY)
                                                }
                                            }
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onClick() }
                                    )
                                }
                        )
                    }
                }
            }

            // Discreet bottom-right label: "Live Theme"
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ThemeConfig.Spacing.XS.dp)
            ) {
                Text(
                    text = "Live Theme",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

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
        targetValue = if (isPressed) accentColor.copy(alpha = 0.15f) else SurfaceVariant,
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
