package com.aodstudio.app.feature.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aodstudio.app.aod.renderer.AODRenderView
import com.aodstudio.app.config.ThemeConfig
import com.aodstudio.app.domain.model.AODTheme
import com.aodstudio.app.ui.theme.Primary
import com.aodstudio.app.ui.theme.Secondary
import com.aodstudio.app.ui.theme.SurfaceContainer
import com.aodstudio.app.ui.theme.SurfaceVariant
import com.aodstudio.app.ui.theme.Tertiary

/**
 * Template Library Screen — dynamic template discovery grid, live card rendering previews,
 * auto-refreshing pool on resume, category filtering, customized template indicators,
 * styled top-right action menu, 3-dot card overflow menu with Reset to Default, and bridged Apply/Active controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeLibraryScreen(
    viewModel: ThemeLibraryViewModel = hiltViewModel(),
    onNavigateToEditor: (String?) -> Unit = {},
    onNavigateToPreview: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var themeToDelete by remember { mutableStateOf<AODTheme?>(null) }
    var topMenuExpanded by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh themes whenever returning from the editor or resuming the screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadThemes()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                        text = "Template Library",
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
                    Box(modifier = Modifier.padding(end = 10.dp)) {
                        // Outstanding 3-Dot Container Button with surface glow & border
                        Surface(
                            onClick = { topMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainer,
                            border = BorderStroke(1.dp, Color(0xFF383838)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "Library Options",
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false },
                            modifier = Modifier.background(SurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Create New Template",
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    topMenuExpanded = false
                                    onNavigateToEditor(null)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Refresh Library",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.RestartAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    topMenuExpanded = false
                                    viewModel.loadThemes()
                                }
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
            // ─── Dynamic Category Filter Chips ────────────────────────
            CategoryFilterRow(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.SM.dp))

            // ─── Template Pool Grid ──────────────────────────────────
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.filteredThemes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No templates found in this category",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(ThemeConfig.Spacing.MD.dp),
                    horizontalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp),
                    verticalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.SM.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredThemes, key = { it.id }) { theme ->
                        TemplateCard(
                            theme = theme,
                            isActive = theme.id == uiState.activeThemeId,
                            isBuiltIn = viewModel.isBuiltInTemplate(theme.id),
                            isCustomized = viewModel.isThemeCustomized(theme),
                            viewModel = viewModel,
                            onActivate = { viewModel.activateTheme(theme.id) },
                            onEdit = { onNavigateToEditor(theme.id) },
                            onPreview = { onNavigateToPreview(theme.id) },
                            onResetToDefault = { viewModel.resetThemeToDefault(theme.id) },
                            onDelete = { themeToDelete = theme }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    themeToDelete?.let { theme ->
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = { Text("Delete Template") },
            text = { Text("Are you sure you want to delete '${theme.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTheme(theme.id)
                        themeToDelete = null
                    }
                ) {
                    Text("Delete", color = Secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ThemeConfig.Spacing.MD.dp),
        horizontalArrangement = Arrangement.spacedBy(ThemeConfig.Spacing.XS.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val selected = category.equals(selectedCategory, ignoreCase = true)
            FilterChip(
                selected = selected,
                onClick = { onSelectCategory(category) },
                label = { Text(text = category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.Black,
                    containerColor = SurfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

/**
 * Independent Template Card in the template pool.
 * Features live AOD rendering preview, customized indicator badge, 3-dot overflow menu, and bridged Apply/Active action.
 */
@Composable
private fun TemplateCard(
    theme: AODTheme,
    isActive: Boolean,
    isBuiltIn: Boolean,
    isCustomized: Boolean,
    viewModel: ThemeLibraryViewModel,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onResetToDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview),
        shape = RoundedCornerShape(ThemeConfig.Radius.LG.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) SurfaceContainer else SurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThemeConfig.Spacing.SM.dp)
        ) {
            // ─── 1. Top Section: Live Mini Preview Canvas ───────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(ThemeConfig.Radius.MD.dp))
                    .background(Color.Black)
            ) {
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

                // Category & Customized Tag Overlays
                val category = theme.metadata[AODTheme.META_CATEGORY] ?: "Custom"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    if (isCustomized && isBuiltIn) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CUSTOMIZED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Active Status Badge
                if (isActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Active",
                            tint = Tertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XS.dp))

            // ─── 2. Information Header & 3-Dot Overflow Menu ───────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${theme.elements.size} elements",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // 3-Dot Menu Anchor
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfaceVariant)
                    ) {
                        // Preview Fullscreen Option
                        DropdownMenuItem(
                            text = { Text("Preview Fullscreen") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onPreview()
                            }
                        )

                        // Reset to Default Option (Available for built-in template definitions)
                        if (isBuiltIn) {
                            DropdownMenuItem(
                                text = { Text(if (isCustomized) "Reset to Default" else "Restore Default") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.RestartAlt,
                                        contentDescription = null,
                                        tint = if (isCustomized) Secondary else Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onResetToDefault()
                                }
                            )
                        }

                        // Delete Option
                        DropdownMenuItem(
                            text = { Text("Delete", color = Secondary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ThemeConfig.Spacing.XXS.dp))

            // ─── 3. Action Row (Bridged Apply / Active & Edit) ─────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Tertiary.copy(alpha = 0.2f) else Primary,
                        contentColor = if (isActive) Tertiary else Color.Black
                    ),
                    shape = RoundedCornerShape(ThemeConfig.Radius.SM.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Tertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Apply",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Template",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
