package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    viewModel: WallpaperViewModel = viewModel(),
    onNavigateToSettings: (() -> Unit)? = null
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

    val isAnySheetOpen = uiState.showStyleSheet ||
        uiState.showPaletteSheet ||
        uiState.showAddShapeSheet ||
        uiState.showExportDialog ||
        uiState.isSettingsOpen ||
        uiState.showColorPickerModal ||
        uiState.showCustomPaletteBuilder

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(bottom = if (uiState.isFullscreenPreview || isAnySheetOpen) 16.dp else 96.dp)
                    .navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 1. Interactive Studio Canvas OR Procedural Bitmap Canvas
            if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                StudioTouchCanvas(
                    params = uiState.params,
                    selectedShapeId = uiState.selectedShapeId,
                    onSelectShape = { id -> viewModel.selectShape(id) },
                    onCommitShapePosition = { id, x, y -> viewModel.commitShapePosition(id, x, y) },
                    onCommitShapeScale = { id, w -> viewModel.commitShapeScale(id, w) },
                    onCommitShapeRotation = { id, deg -> viewModel.commitShapeRotation(id, deg) },
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
                WallpaperViewport(
                    bitmap = uiState.previewBitmap,
                    isGenerating = uiState.isGeneratingPreview,
                    aspectRatioPreset = uiState.params.aspectRatio,
                    isFullscreen = uiState.isFullscreenPreview,
                    showLauncherMockup = uiState.showLauncherMockup,
                    paletteColors = uiState.params.palette.colors,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Solid Opaque Top Bar (100% solid surface fill, bold branding, zero ghosting)
            AnimatedVisibility(
                visible = !uiState.isFullscreenPreview && !isAnySheetOpen,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = uiState.settings.motionScale.damping,
                        stiffness = uiState.settings.motionScale.stiffness
                    )
                ) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                WallpaperStudioTopBar(
                    activePatternName = uiState.params.patternType.displayName,
                    aspectRatio = uiState.params.aspectRatio,
                    hapticStrength = uiState.settings.hapticStrength,
                    onTitleClick = { viewModel.showStyleSheet(true) },
                    onAspectRatioClick = {
                        val entries = AspectRatioPreset.entries
                        val nextIdx = (entries.indexOf(uiState.params.aspectRatio) + 1) % entries.size
                        viewModel.setAspectRatio(entries[nextIdx])
                    },
                    onFullscreenClick = { viewModel.toggleFullscreen() },
                    onExportClick = { viewModel.showExportDialog(true) },
                    onSettingsClick = {
                        if (onNavigateToSettings != null) {
                            onNavigateToSettings()
                        } else {
                            viewModel.openSettings(true)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Minimal Floating Frosted Action Deck (Over canvas)
            AnimatedVisibility(
                visible = !uiState.isFullscreenPreview && !isAnySheetOpen,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .testTag("floating_action_deck")
                ) {
                    if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                        // 4 equal-spaced pills in Studio mode: Style, Shape, Shuffle, Palette
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Pill 1: Style
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = null,
                                modifier = Modifier
                                    .weight(1f)
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = getPatternIcon(uiState.params.patternType),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Style",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Pill 2: Shape
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = null,
                                modifier = Modifier
                                    .weight(1f)
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Shape",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Shape",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Pill 3: Shuffle (Primary)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(generateButtonScale.value)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        scope.launch {
                                            if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Shuffle",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Pill 4: Palette
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = null,
                                modifier = Modifier
                                    .weight(1f)
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        uiState.params.palette.colors.take(3).forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Colors",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard mode dock: Style, Generate, Palette, Settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. Style Chip
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = null,
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

                            // 2. Generate Button (Spring Bounce Physics)
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(generateButtonScale.value)
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        scope.launch {
                                            if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
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
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Generate",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // 3. Palette Chip
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = null,
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

                            // 4. Settings Gear
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding()
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // 2. High-Impact Parametric Controls
                Text(
                    text = "Parametric Controls",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Scale / Zoom Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Scale / Zoom", style = MaterialTheme.typography.bodySmall)
                        Text(text = String.format("%.2fx", uiState.params.scale), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = uiState.params.scale,
                        onValueChange = { scale -> viewModel.updateParams { it.copy(scale = scale) } },
                        valueRange = 0.5f..2.0f
                    )
                }

                // Stacked Pills Dedicated Controls
                if (uiState.params.patternType == WallpaperPatternType.STACKED_PILLS) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text(
                        text = "Stacked Pills Architecture",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Pill Width Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Pill Width", style = MaterialTheme.typography.bodySmall)
                            Text(text = String.format("%.0f%%", uiState.params.pillWidth * 100f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.params.pillWidth,
                            onValueChange = { viewModel.setPillWidth(it) },
                            valueRange = 0.4f..1.0f
                        )
                    }

                    // Pill Thickness / Height Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Pill Thickness (Height)", style = MaterialTheme.typography.bodySmall)
                            Text(text = String.format("%.1f%%", uiState.params.pillHeight * 100f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.params.pillHeight,
                            onValueChange = { viewModel.setPillHeight(it) },
                            valueRange = 0.02f..0.15f
                        )
                    }
                }

                // Contours Dedicated Controls
                if (uiState.params.patternType == WallpaperPatternType.CONTOURS) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text(
                        text = "Contour Architecture",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Line Spacing
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Line Spacing", style = MaterialTheme.typography.bodySmall)
                            Text(text = String.format("%.1fx", uiState.params.complexity), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.params.complexity,
                            onValueChange = { comp -> viewModel.updateParams { it.copy(complexity = comp) } },
                            valueRange = 0.5f..2.0f
                        )
                    }

                    // Stroke Thickness
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Stroke Thickness", style = MaterialTheme.typography.bodySmall)
                            Text(text = String.format("%.1fdp", uiState.params.lineWidth), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.params.lineWidth,
                            onValueChange = { lw -> viewModel.updateParams { it.copy(lineWidth = lw) } },
                            valueRange = 1.0f..6.0f
                        )
                    }
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding()
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

                // Create Custom Palette Action
                Button(
                    onClick = {
                        viewModel.showPaletteSheet(false)
                        viewModel.showCustomPaletteBuilder(true)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_custom_palette_builder_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Custom Palette", fontWeight = FontWeight.Bold)
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

                // My Custom Palettes (Persistent DataStore)
                if (uiState.userCustomPalettes.isNotEmpty()) {
                    Text(
                        text = "My Custom Palettes (${uiState.userCustomPalettes.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.userCustomPalettes.forEach { palette ->
                            val isSelected = palette.id == uiState.params.palette.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = null,
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                            .width(90.dp)
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

                                    IconButton(
                                        onClick = { viewModel.deleteCustomPalette(palette.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete palette",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
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
                }

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
                            border = null,
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
                            border = null,
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

    // Custom Palette Builder Modal Sheet
    if (uiState.showCustomPaletteBuilder) {
        CustomPaletteBuilderSheet(
            initialPalette = uiState.params.palette,
            onSavePalette = { newPalette ->
                viewModel.saveCustomPalette(newPalette)
                viewModel.showCustomPaletteBuilder(false)
            },
            onDismiss = { viewModel.showCustomPaletteBuilder(false) }
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

    // Settings Screen Overlay (fallback if not using RootNavigation)
    if (uiState.isSettingsOpen && onNavigateToSettings == null) {
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
        WallpaperPatternType.BAUHAUS_SEMICIRCLE -> Icons.Default.FilterVintage
        WallpaperPatternType.FLUTED_ARCHES -> Icons.Default.Architecture
        WallpaperPatternType.LAVA_BLOB -> Icons.Default.AutoAwesome
        WallpaperPatternType.STUDIO -> Icons.Default.TouchApp
    }
}
