package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.AspectRatioPreset
import com.example.engine.WallpaperPatternType
import com.example.palette.PaletteEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WallpaperViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dynamic Monet Scheme Colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    // Snackbar notifications
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
        }
    }

    // Button spring bounce physics
    val generateButtonScale = remember { Animatable(1f) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(bottom = if (uiState.isFullscreenPreview) 16.dp else 96.dp)
                    .navigationBarsPadding()
            )
        },
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isFullscreenPreview,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Wallpaper Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.params.patternType.displayName} • ${uiState.params.subTypeName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // Quick Shuffle / Re-roll
                        IconButton(
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.onGenerateOrShuffleClicked()
                            },
                            modifier = Modifier.testTag("topbar_shuffle_button")
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle Layout / Seed")
                        }

                        // Toggle Launcher Mockup Overlay
                        IconButton(
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.toggleLauncherMockup()
                            },
                            modifier = Modifier.testTag("topbar_mockup_toggle")
                        ) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = "Toggle Launcher Mockup",
                                tint = if (uiState.showLauncherMockup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Fullscreen Preview Toggle
                        IconButton(
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.toggleFullscreen()
                            },
                            modifier = Modifier.testTag("topbar_fullscreen_button")
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen Preview")
                        }

                        // Export & Set Wallpaper
                        IconButton(
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showExportDialog(true)
                            },
                            modifier = Modifier.testTag("topbar_export_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Save / Export Wallpaper")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            // 1. Interactive Studio Canvas OR Procedural Bitmap Canvas
            if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                CustomStudioCanvas(
                    params = uiState.params,
                    selectedShapeId = uiState.selectedShapeId,
                    onSelectShape = { id -> viewModel.selectShape(id) },
                    onUpdateShapePosition = { id, x, y -> viewModel.updateShapePosition(id, x, y) },
                    onUpdateShapeScale = { id, factor -> viewModel.updateShapeScale(id, factor) },
                    onUpdateShapeRotation = { id, delta -> viewModel.updateShapeRotation(id, delta) },
                    onSetShapeRotation = { id, deg -> viewModel.setShapeRotation(id, deg) },
                    onSetShapeColorIndex = { id, idx -> viewModel.setShapeColorIndex(id, idx) },
                    onBringShapeToFront = { id -> viewModel.bringShapeToFront(id) },
                    onSendShapeToBack = { id -> viewModel.sendShapeToBack(id) },
                    onDeleteShape = { id -> viewModel.deleteShape(id) },
                    onDuplicateShape = { id -> viewModel.duplicateShape(id) },
                    onToggleShapeWireframe = { id -> viewModel.toggleShapeWireframe(id) },
                    isFullscreen = uiState.isFullscreenPreview,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                WallpaperCanvasPreview(
                    bitmap = uiState.previewBitmap,
                    isGenerating = uiState.isGeneratingPreview,
                    aspectRatioPreset = uiState.params.aspectRatio,
                    isFullscreen = uiState.isFullscreenPreview,
                    showLauncherMockup = uiState.showLauncherMockup,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Minimal Floating Frosted Action Deck (Over canvas)
            AnimatedVisibility(
                visible = !uiState.isFullscreenPreview,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = uiState.settings.motionScale.damping,
                        stiffness = uiState.settings.motionScale.stiffness
                    )
                ) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    ),
                    shadowElevation = 10.dp,
                    modifier = Modifier.testTag("floating_action_deck")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Style Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.showStyleSheet(true)
                                }
                                .testTag("style_chip_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getPatternIcon(uiState.params.patternType),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.params.patternType.displayName.split(" ").firstOrNull() ?: "Style",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // 2. Add Shape Button (Visible in Studio Custom Mode)
                        if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        viewModel.showAddShapeSheet(true)
                                    }
                                    .testTag("add_shape_deck_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Shape",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Shape",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // 3. Generate / Shuffle Button (Spring Bounce Physics)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .scale(generateButtonScale.value)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    scope.launch {
                                        if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        // Spring bounce animation
                                        generateButtonScale.animateTo(
                                            targetValue = 0.88f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessHigh
                                            )
                                        )
                                        viewModel.onGenerateOrShuffleClicked()
                                        generateButtonScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                                .testTag("generate_re-roll_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.params.patternType == WallpaperPatternType.STUDIO) Icons.Default.Shuffle else Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (uiState.params.patternType == WallpaperPatternType.STUDIO) "Shuffle" else "Generate",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // 4. Palette Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.showPaletteSheet(true)
                                }
                                .testTag("palette_chip_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Mini 4-dot preview of active palette
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    uiState.params.palette.colors.take(3).forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                                Text(
                                    text = "Palette",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // 5. Settings Gear
                        IconButton(
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.openSettings(true)
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .testTag("settings_gear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // Exit Fullscreen Button
            if (uiState.isFullscreenPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { viewModel.toggleFullscreen() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .testTag("exit_fullscreen_button")
                    ) {
                        Icon(
                            Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // ADD SHAPE MODAL SHEET (Studio Custom)
    // -------------------------------------------------------------
    if (uiState.showAddShapeSheet) {
        AddShapeBottomSheet(
            onShapeChosen = { type -> viewModel.addCustomShape(type) },
            onDismiss = { viewModel.showAddShapeSheet(false) }
        )
    }

    // -------------------------------------------------------------
    // STYLE SELECTION MODAL SHEET
    // -------------------------------------------------------------
    if (uiState.showStyleSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.showStyleSheet(false) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generator Style & Geometry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.showStyleSheet(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // 1. Patterns Grid / Row
                Text(
                    text = "Pattern Architecture",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperPatternType.entries.forEach { pattern ->
                        val isSelected = pattern == uiState.params.patternType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.setPatternType(pattern)
                            },
                            leadingIcon = {
                                Icon(
                                    getPatternIcon(pattern),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(pattern.displayName) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // 2. Subtypes Selector
                Text(
                    text = "Geometry Variation",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.params.patternType.subTypes.forEachIndexed { index, subTypeName ->
                        val isSelected = index == uiState.params.subTypeIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.setSubType(index)
                            },
                            label = { Text(subTypeName) },
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // 3. Quick Tuning Sliders
                Text(
                    text = "Parametric Controls",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Scale Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Scale", style = MaterialTheme.typography.bodySmall)
                        Text(text = String.format("%.2fx", uiState.params.scale), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = uiState.params.scale,
                        onValueChange = { scale -> viewModel.updateParams { it.copy(scale = scale) } },
                        valueRange = 0.5f..2.0f
                    )
                }

                // Complexity Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Complexity & Layers", style = MaterialTheme.typography.bodySmall)
                        Text(text = String.format("%.1fx", uiState.params.complexity), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = uiState.params.complexity,
                        onValueChange = { comp -> viewModel.updateParams { it.copy(complexity = comp) } },
                        valueRange = 0.4f..2.0f
                    )
                }

                // Distortion Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Curvature Distortion", style = MaterialTheme.typography.bodySmall)
                        Text(text = String.format("%.2f", uiState.params.distortion), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = uiState.params.distortion,
                        onValueChange = { dist -> viewModel.updateParams { it.copy(distortion = dist) } },
                        valueRange = 0.2f..2.0f
                    )
                }

                // Aspect Ratio Selector
                Text(
                    text = "Aspect Ratio",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioPreset.entries.forEach { aspect ->
                        val isSelected = aspect == uiState.params.aspectRatio
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAspectRatio(aspect) },
                            label = { Text(aspect.displayName) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // -------------------------------------------------------------
    // PALETTE SELECTION MODAL SHEET
    // -------------------------------------------------------------
    if (uiState.showPaletteSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.showPaletteSheet(false) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tonal Harmony & Monet Palette",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.showPaletteSheet(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Dynamic Monet Extraction Action
                FilledTonalButton(
                    onClick = {
                        if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        viewModel.applyDynamicMonet(
                            primary = primaryColor,
                            secondary = secondaryColor,
                            tertiary = tertiaryColor,
                            surface = surfaceColor,
                            background = backgroundColor
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract Dynamic Monet From System Theme", fontWeight = FontWeight.Bold)
                }

                // Active Color Stops Editor
                Text(
                    text = "Active Tonal Ramp (${uiState.params.palette.colors.size} stops - Tap to Edit):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    uiState.params.palette.colors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(color)
                                .clickable {
                                    viewModel.showColorPicker(true, index)
                                }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.addColorStop(Color(0xFF80D8FF)) },
                        enabled = uiState.params.palette.colors.size < 8,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stop", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.removeColorStop(uiState.params.palette.colors.size - 1) },
                        enabled = uiState.params.palette.colors.size > 2,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Stop", style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Curated Style-Matched Palettes
                val styleMatched = PaletteEngine.getPalettesForPattern(uiState.params.patternType)
                Text(
                    text = "Signature Palettes for ${uiState.params.patternType.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    styleMatched.forEach { palette ->
                        val isSelected = palette.id == uiState.params.palette.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.setPalette(palette)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = palette.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    palette.colors.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .background(color)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // All Material 3 Presets
                Text(
                    text = "All Tonal Harmony Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val otherPresets = PaletteEngine.allPresets.filterNot { p -> styleMatched.any { it.id == p.id } }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    otherPresets.forEach { palette ->
                        val isSelected = palette.id == uiState.params.palette.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.setPalette(palette)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = palette.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    palette.colors.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .background(color)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Color Picker Modal
    if (uiState.showColorPickerModal) {
        val initialColor = uiState.params.palette.colors.getOrElse(uiState.activeColorStopIndex) { Color.Cyan }
        ColorPickerModal(
            initialColor = initialColor,
            onColorSelected = { selected ->
                viewModel.updateColorStop(uiState.activeColorStopIndex, selected)
            },
            onDismiss = { viewModel.showColorPicker(false) }
        )
    }

    // Export & Apply Bottom Sheet
    if (uiState.showExportDialog) {
        ExportDialog(
            isExporting = uiState.isExporting,
            onSaveToGallery = { viewModel.saveToGallery(context) },
            onSetWallpaper = { target -> viewModel.setSystemWallpaper(context, target) },
            onShare = { viewModel.shareWallpaper(context) },
            onDismiss = { viewModel.showExportDialog(false) }
        )
    }

    // Settings Screen Overlay
    if (uiState.isSettingsOpen) {
        SettingsScreen(
            viewModel = viewModel,
            settings = uiState.settings,
            onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
            onResetDefaults = { viewModel.resetToDefaults() },
            onNavigateBack = { viewModel.openSettings(false) }
        )
    }
}

private fun getPatternIcon(pattern: WallpaperPatternType): ImageVector {
    return when (pattern) {
        WallpaperPatternType.MOUNTAINS -> Icons.Default.Landscape
        WallpaperPatternType.WAVES -> Icons.Default.Waves
        WallpaperPatternType.STACKED_PILLS -> Icons.Default.ViewWeek
        WallpaperPatternType.DOT_GRID -> Icons.Default.BlurCircular
        WallpaperPatternType.CONTOURS -> Icons.Default.Terrain
        WallpaperPatternType.STUDIO -> Icons.Default.TouchApp
    }
}
