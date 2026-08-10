package com.aodstudio.app.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODElementType
import com.aodstudio.app.feature.editor.components.PropertyPanel
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary

/**
 * AOD Editor Screen — interactive visual editor for building and customizing AOD themes.
 * Features live AOD Canvas preview, drag & drop element positioning, property panel, and saving.
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.theme?.name ?: "AOD Editor",
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
            // ─── Canvas Preview Area ───────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
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
                                    setTheme(currentTheme)
                                }
                            },
                            update = { renderView ->
                                renderView.setTheme(currentTheme)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // ─── Property Inspector Panel ──────────────────────────
            PropertyPanel(
                element = uiState.selectedElement,
                onUpdateElement = { updatedElem ->
                    viewModel.updateElementStyle(updatedElem.id, updatedElem)
                },
                onDeleteElement = { viewModel.deleteSelectedElement() }
            )
        }
    }

    // Add Element Dialog
    if (uiState.showAddElementDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddElementDialog(false) },
            title = { Text("Add Element to Canvas") },
            text = {
                Column {
                    listOf(
                        AODElementType.CLOCK,
                        AODElementType.DATE,
                        AODElementType.BATTERY,
                        AODElementType.TEXT,
                        AODElementType.NOTIFICATION,
                        AODElementType.MUSIC,
                        AODElementType.SHAPE
                    ).forEach { type ->
                        TextButton(
                            onClick = { viewModel.addElement(type) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = type.name,
                                modifier = Modifier.fillMaxWidth(),
                                color = Primary
                            )
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
