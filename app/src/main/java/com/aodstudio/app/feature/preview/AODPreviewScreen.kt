package com.aodstudio.app.feature.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.ui.theme.Primary

/**
 * Full-screen AOD preview that renders the theme exactly as it would appear on the lock screen.
 * Uses immersive AMOLED black background and the same AODRenderView as the real overlay.
 *
 * Tap anywhere to go back — this is a read-only preview, not the editor.
 */
@Composable
fun AODPreviewScreen(
    themeId: String,
    viewModel: AODPreviewViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(themeId) {
        viewModel.loadTheme(themeId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = Primary)
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage ?: "Error loading theme",
                    color = MaterialTheme.colorScheme.error
                )
            }
            uiState.theme != null -> {
                AndroidView(
                    factory = { context ->
                        AODRenderView(context).apply {
                            setTheme(uiState.theme!!)
                        }
                    },
                    update = { renderView ->
                        uiState.theme?.let { renderView.setTheme(it) }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
