package com.aodstudio.app.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.feature.editor.components.PropertyPanel
import com.aodstudio.app.ui.theme.Primary

/**
 * AOD Editor Screen — interactive visual editor for building and customizing AOD themes.
 *
 * Features:
 *   - Long-press / tap full-screen preview with touch capture overlay and hardware BackHandler.
 *   - Top Section (45% height): Expanded & Centered live preview canvas.
 *   - Bottom Section (55% height): Modular Property Panel with Layer Selector, Position Presets, Element Controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AODEditorScreen(
    themeId: String?,
    viewModel: AODEditorViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isFullPreviewActive by remember { mutableStateOf(false) }

    LaunchedEffect(themeId) {
        viewModel.loadTheme(themeId)
    }

    LaunchedEffect(uiState.userMessage, uiState.errorMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.theme?.name ?: "Custom Theme",
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
                    actions = {
                        // Undo Button
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }

                        // Redo Button
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }

                        // Preview Fullscreen Button
                        IconButton(onClick = { isFullPreviewActive = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = "Preview Fullscreen",
                                tint = Primary
                            )
                        }

                        // Add Element Button
                        IconButton(onClick = { viewModel.toggleAddElementDialog(true) }) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add Element",
                                tint = Primary
                            )
                        }

                        // Save Button
                        IconButton(
                            onClick = { viewModel.saveTheme() },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = "Save Theme",
                                    tint = if (uiState.isDirty) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ─── 1. Top Section: Preview Canvas (Tap & Hold for Full Preview) ───
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Primary)
                    } else {
                        uiState.theme?.let { currentTheme ->
                            AndroidView(
                                factory = { context ->
                                    AODRenderView(context).apply {
                                        setMediaRepository(viewModel.mediaRepository)
                                        setNotificationRepository(viewModel.notificationRepository)
                                        setTheme(currentTheme)
                                    }
                                },
                                update = { renderView ->
                                    renderView.setMediaRepository(viewModel.mediaRepository)
                                    renderView.setNotificationRepository(viewModel.notificationRepository)
                                    renderView.setTheme(currentTheme)
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Touch overlay to reliably intercept tap and long-press gestures
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                isFullPreviewActive = true
                                            },
                                            onTap = {
                                                isFullPreviewActive = true
                                            }
                                        )
                                    }
                            )

                            // Visual hint pill indicating tap/hold to preview full-screen
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tap to Preview",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── 2. Bottom Section: Editor Panel (Constrain & Scroll: 55% Height) ───
                Box(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxWidth()
                ) {
                    PropertyPanel(
                        elements = uiState.theme?.elements ?: emptyList(),
                        selectedElementId = uiState.selectedElementId,
                        onSelectElement = { id -> viewModel.selectElement(id) },
                        onUpdateElement = { updatedElem ->
                            viewModel.updateElementStyle(updatedElem.id, updatedElem)
                        },
                        onDeleteElement = { viewModel.deleteSelectedElement() },
                        onAddElementClick = { viewModel.toggleAddElementDialog(true) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ─── Full-Screen AOD Hold Preview Overlay ─────────────────────────
        if (isFullPreviewActive) {
            BackHandler(enabled = true) {
                isFullPreviewActive = false
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .zIndex(100f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                isFullPreviewActive = false
                            },
                            onPress = {
                                tryAwaitRelease()
                                isFullPreviewActive = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                uiState.theme?.let { currentTheme ->
                    AndroidView(
                        factory = { context ->
                            AODRenderView(context).apply {
                                setMediaRepository(viewModel.mediaRepository)
                                setNotificationRepository(viewModel.notificationRepository)
                                setTheme(currentTheme)
                            }
                        },
                        update = { renderView ->
                            renderView.setMediaRepository(viewModel.mediaRepository)
                            renderView.setNotificationRepository(viewModel.notificationRepository)
                            renderView.setTheme(currentTheme)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dismiss hint banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                ) {
                    Text(
                        text = "Tap anywhere to exit preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // ─── Add Element Dialog ─────────────────────────────────────────
    if (uiState.showAddElementDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddElementDialog(false) },
            title = { Text("Add Element") },
            text = {
                Column {
                    val availableTypes = listOf(
                        AODElementType.CLOCK to "Clock Face",
                        AODElementType.DATE to "Date Display",
                        AODElementType.BATTERY to "Battery Indicator",
                        AODElementType.NOTIFICATION to "Notifications",
                        AODElementType.MUSIC to "Music Player",
                        AODElementType.TEXT to "Custom Text",
                        AODElementType.SHAPE to "Geometric Shape"
                    )
                    availableTypes.forEach { (type, label) ->
                        TextButton(
                            onClick = { viewModel.addElement(type) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddElementDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}
