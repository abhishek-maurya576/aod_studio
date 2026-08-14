package com.aodstudio.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.feature.editor.AODEditorScreen
import com.aodstudio.app.feature.home.HomeScreen
import com.aodstudio.app.feature.library.ThemeLibraryScreen
import com.aodstudio.app.feature.onboarding.VivoOnboardingScreen
import com.aodstudio.app.feature.preview.AODPreviewScreen
import com.aodstudio.app.feature.settings.SettingsScreen

/**
 * Root navigation host for AOD Studio.
 * Single-Activity architecture: all screens are Compose destinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AODNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        // ─── Home Screen ───────────────────────────────────────────
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToEditor = { themeId ->
                    navController.navigate(EditorRoute(themeId))
                },
                onNavigateToThemes = {
                    navController.navigate(ThemeLibraryRoute)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
                }
            )
        }

        // ─── Theme Library Destination ─────────────────────────────
        composable<ThemeLibraryRoute> {
            ThemeLibraryScreen(
                onNavigateToEditor = { themeId ->
                    navController.navigate(EditorRoute(themeId))
                },
                onNavigateToPreview = { themeId ->
                    navController.navigate(PreviewRoute(themeId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Theme Editor Destination ──────────────────────────────
        composable<EditorRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<EditorRoute>()
            AODEditorScreen(
                themeId = route.themeId,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Preview Destination ───────────────────────────────────
        composable<PreviewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PreviewRoute>()
            AODPreviewScreen(
                themeId = route.themeId,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Settings Destination ──────────────────────────────────
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Vivo Onboarding Destination ──────────────────────────────
        // Shown on first launch for Vivo/OriginOS devices.
        composable<OnboardingRoute> {
            VivoOnboardingScreen(
                onFinish = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Clean placeholder screen used for non-crash navigation to destinations
 * being built in future phases.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(ThemeConfig.Spacing.MD.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
