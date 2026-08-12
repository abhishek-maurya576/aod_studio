package com.aodstudio.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceVariant
import com.aodstudio.app.ui.theme.Tertiary

/**
 * VivoOnboardingScreen — 3-step onboarding wizard for Vivo/OriginOS survival setup.
 *
 * Shown automatically on first launch when [AppConfig.Features.SHOW_VIVO_ONBOARDING] is true
 * and the device is a Vivo device. Teaches the user to:
 *   Step 0: Enable "High Background Power Consumption" in Vivo battery settings
 *   Step 1: Enable Autostart in iManager
 *   Step 2: Lock the app in the Recents screen (padlock gesture — no deep-link possible)
 *
 * Each step's "Open Settings" button tries the OEM deep-link first, falls back gracefully
 * to App Info settings if the OEM intent does not resolve.
 *
 * [onFinish] is called when the user completes all steps or taps Skip.
 */
@Composable
fun VivoOnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinish: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate away once onboarding is done.
    LaunchedEffect(uiState.hasCompletedOnboarding) {
        if (uiState.hasCompletedOnboarding) onFinish()
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ThemeConfig.Spacing.LG.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XL.dp))

            // ── Header ──────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OriginOS Setup",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A few extra steps are required on Vivo / OriginOS to prevent\n" +
                            "the system from killing the AOD background service.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Step indicator dots ──────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(OnboardingViewModel.TOTAL_STEPS) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == uiState.currentStep) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == uiState.currentStep) Primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // ── Step content (animated slide) ────────────────────────────────
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
                label = "OnboardingStep"
            ) { step ->
                when (step) {
                    0 -> OnboardingStepCard(
                        stepNumber = 1,
                        title = "Enable High Background Power",
                        body = "Vivo maintains its own battery whitelist separate from Android's standard " +
                                "battery optimisation. AOD Studio MUST be added to the \"High Background " +
                                "Power Consumption\" list, or OriginOS will kill the service within minutes.\n\n" +
                                "Tap Open Settings → find AOD Studio → switch it ON.",
                        warningText = "The standard \"Ignore Battery Optimizations\" toggle alone " +
                                "is insufficient on OriginOS. Both must be enabled.",
                        settingsButtonLabel = "Open Battery Settings",
                        onOpenSettings = {
                            val intent = viewModel.getBatteryHighBackgroundIntent()
                            intent?.let { context.startActivity(it) }
                        }
                    )

                    1 -> OnboardingStepCard(
                        stepNumber = 2,
                        title = "Enable Autostart in iManager",
                        body = "OriginOS restricts which apps can start automatically. Without autostart " +
                                "permission, the AOD service will not restart after the phone reboots.\n\n" +
                                "Tap Open iManager → find AOD Studio → toggle ON.",
                        warningText = null,
                        settingsButtonLabel = "Open iManager",
                        onOpenSettings = {
                            val intent = viewModel.getAutostartIntent()
                            intent?.let { context.startActivity(it) }
                        }
                    )

                    2 -> OnboardingStepCard(
                        stepNumber = 3,
                        title = "Lock App in Recents Screen",
                        body = "Open the Recents screen (swipe up and hold, or press the square button). " +
                                "Find the AOD Studio card. Tap the padlock icon (or long-press the card " +
                                "and select \"Lock\").\n\n" +
                                "This prevents OriginOS from sweeping the app from memory when the user " +
                                "clears all recent apps.",
                        warningText = "There is no deep-link for this action — it must be done manually " +
                                "in the Recents screen each time the app is freshly installed.",
                        settingsButtonLabel = null, // No deep-link possible for this step
                        onOpenSettings = {}
                    )
                }
            }

            // ── Navigation buttons ───────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (uiState.currentStep == 0) viewModel.skipOnboarding()
                            else viewModel.previousStep()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(if (uiState.currentStep == 0) "Skip" else "Back")
                    }

                    Button(
                        onClick = { viewModel.nextStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = if (uiState.currentStep == OnboardingViewModel.TOTAL_STEPS - 1)
                                "Done" else "Next",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(ThemeConfig.Spacing.LG.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step card component
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingStepCard(
    stepNumber: Int,
    title: String,
    body: String,
    warningText: String?,
    settingsButtonLabel: String?,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ThemeConfig.Radius.LG.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThemeConfig.Spacing.LG.dp),
            verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.MD.dp)
        ) {
            // Step badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(ThemeConfig.Spacing.SM.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Body text
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            // Warning banner (optional)
            if (warningText != null) {
                Card(
                    shape = RoundedCornerShape(ThemeConfig.Radius.SM.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary.copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = "⚠ $warningText",
                        modifier = Modifier.padding(ThemeConfig.Spacing.SM.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Open Settings button (only if a deep-link action exists)
            if (settingsButtonLabel != null) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary)
                ) {
                    Text(
                        text = settingsButtonLabel,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
