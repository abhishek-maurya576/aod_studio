package com.aodstudio.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceVariant
import com.aodstudio.app.ui.theme.Tertiary

/**
 * Settings Screen — master AOD toggle, permission management, battery optimization,
 * hardware pocket detection, OLED burn-in protection, and double-tap overlay exit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ThemeConfig.Spacing.MD.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
        ) {
            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

            // ─── Pro Upgrade Banner ────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ThemeConfig.Radius.MD.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ThemeConfig.Spacing.MD.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Pro",
                            tint = Primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.padding(start = ThemeConfig.Spacing.SM.dp))
                        Column {
                            Text(
                                text = "AOD Studio Pro",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                text = "Unlock premium themes & advanced animations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { /* Pro Trigger */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Upgrade", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

            // ─── Master Service Toggle ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ThemeConfig.Radius.MD.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ThemeConfig.Spacing.MD.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Always-On Display",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                uiState.isServiceActuallyRunning -> "Service is running"
                                uiState.isAodEnabled -> "Enabling..."
                                else -> "Tap to activate AOD overlay"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isServiceActuallyRunning) Tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = uiState.isAodEnabled,
                        onCheckedChange = { viewModel.toggleAodService(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Primary,
                            checkedTrackColor = Primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

            // ─── Hardware & Behavior Settings ──────────────────────
            Text(
                text = "HARDWARE & BEHAVIOR",
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            // Pocket Detection Toggle
            SettingToggleCard(
                icon = Icons.Outlined.Security,
                title = "Enable Pocket Detection",
                subtitle = "Turns off AOD screen using proximity sensor when in a pocket",
                isChecked = uiState.pocketDetectionEnabled,
                onCheckedChange = { viewModel.togglePocketDetection(it) }
            )

            // OLED Burn-In Protection Toggle
            SettingToggleCard(
                icon = Icons.Outlined.Shield,
                title = "OLED Burn-In Protection",
                subtitle = "Periodically shifts pixel coordinates to prevent subpixel burn-in",
                isChecked = uiState.burnInProtectionEnabled,
                onCheckedChange = { viewModel.toggleBurnInProtection(it) }
            )

            // Double Tap to Exit Toggle
            SettingToggleCard(
                icon = Icons.Outlined.Layers,
                title = "Double Tap to Exit",
                subtitle = "Double tap anywhere on AOD screen to dismiss overlay",
                isChecked = uiState.doubleTapToExit,
                onCheckedChange = { viewModel.toggleDoubleTapToExit(it) }
            )

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

            // ─── Required Permissions Section ──────────────────────
            Text(
                text = "REQUIRED PERMISSIONS",
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            // 1. System Overlay Permission
            PermissionCard(
                icon = Icons.Outlined.Layers,
                title = "System Overlay",
                description = if (uiState.hasOverlayPermission) "Granted — AOD can draw over lock screen"
                else "Required to display AOD on lock screen",
                isGranted = uiState.hasOverlayPermission,
                onGrant = { context.startActivity(viewModel.openOverlaySettingsIntent()) }
            )

            // 2. Notification Listener Permission
            PermissionCard(
                icon = Icons.Outlined.Notifications,
                title = "Notification Access",
                description = if (uiState.hasNotificationPermission) "Granted — notifications visible on AOD"
                else "Required to show notification icons on AOD",
                isGranted = uiState.hasNotificationPermission,
                onGrant = { context.startActivity(viewModel.openNotificationListenerSettingsIntent()) }
            )

            // 3. Battery Optimization Exemption
            PermissionCard(
                icon = Icons.Outlined.BatteryChargingFull,
                title = "Battery Unrestricted",
                description = if (uiState.isBatteryOptimizationExempt) "Granted — service won't be killed"
                else "Prevents system from killing AOD service",
                isGranted = uiState.isBatteryOptimizationExempt,
                onGrant = { context.startActivity(viewModel.openBatteryOptimizationIntent()) }
            )

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XL.dp))
        }
    }
}

@Composable
private fun SettingToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ThemeConfig.Radius.MD.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThemeConfig.Spacing.MD.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(start = ThemeConfig.Spacing.SM.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Primary,
                    checkedTrackColor = Primary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ThemeConfig.Radius.MD.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThemeConfig.Spacing.MD.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Outlined.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (isGranted) Tertiary else Secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(start = ThemeConfig.Spacing.SM.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isGranted) {
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Grant", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
