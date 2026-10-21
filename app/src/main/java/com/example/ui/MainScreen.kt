package com.example.ui

import com.example.engine.filmGrain
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.AspectRatioPreset
import com.example.engine.BackgroundMode
import com.example.engine.WallpaperPatternType
import com.example.palette.PaletteEngine
import com.example.palette.PaletteCategory
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
    var showAiStudioSheet by remember { mutableStateOf(false) }
    var selectedPaletteCategory by remember { mutableStateOf(PaletteCategory.ALL) }

    // Visual Media Photo Picker for Picture-in-Widget Layers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importImageToStudio(context, uri)
        }
    }

    val isAnySheetOpen = uiState.showStyleSheet ||
        uiState.showPaletteSheet ||
        uiState.showAddShapeSheet ||
        uiState.showAddTextSheet ||
        uiState.showExportDialog ||
        uiState.isSettingsOpen ||
        uiState.showColorPickerModal ||
        uiState.showCustomPaletteBuilder ||
        uiState.showSavedDesignsSheet ||
        uiState.isChangelogOpen ||
        showAiStudioSheet

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(bottom = if (uiState.isFullscreenPreview) 16.dp else 96.dp)
                    .navigationBarsPadding()
                    .zIndex(15f)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // ==========================================
            // LAYER 0: Wallpaper Canvas (Background)
            // ==========================================
            val isSheetActive = isAnySheetOpen
            val animatedBlurRadius by animateDpAsState(
                targetValue = if (isSheetActive && uiState.settings.displayBlurEffectsEnabled) 20.dp else 0.dp,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "backdrop_blur"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .pixelPlayerBackdropBlur(animatedBlurRadius)
                    .filmGrain(uiState.params.filmGrain, uiState.params.seed)
            ) {
                // 1. Interactive Studio Canvas OR Procedural Bitmap Canvas
            if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                StudioCanvas(
                    params = uiState.params,
                    selectedShapeId = uiState.selectedShapeId,
                    selectedTextId = uiState.selectedTextId,
                    selectedImageId = uiState.selectedImageId,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    selectedItemIds = uiState.selectedItemIds,
                    onSelectShape = { id -> viewModel.selectShape(id) },
                    onSelectTextLayer = { id -> viewModel.selectTextLayer(id) },
                    onSelectImageLayer = { id -> viewModel.selectImageLayer(id) },
                    onToggleItemSelection = { id -> viewModel.toggleItemSelection(id) },
                    onCommitShapePosition = { id, x, y -> viewModel.commitShapePosition(id, x, y) },
                    onCommitShapeScale = { id, w -> viewModel.commitShapeScale(id, w) },
                    onCommitShapeDimensions = { id, w, h -> viewModel.commitShapeDimensions(id, w, h) },
                    onCommitShapeRotation = { id, deg -> viewModel.commitShapeRotation(id, deg) },
                    onSetShapeColorIndex = { id, idx -> viewModel.setShapeColorIndex(id, idx) },
                    onBringShapeToFront = { id -> viewModel.bringShapeToFront(id) },
                    onSendShapeToBack = { id -> viewModel.sendShapeToBack(id) },
                    onDuplicateShape = { id -> viewModel.duplicateShape(id) },
                    onDeleteShape = { id -> viewModel.deleteShape(id) },
                    onToggleShapeWireframe = { id -> viewModel.toggleShapeWireframe(id) },
                    onUpdateShapeStrokeWidth = { id, sw -> viewModel.updateShapeStrokeWidth(id, sw) },
                    onUpdateOpacity = { id, op -> viewModel.updateShapeOpacity(id, op) },
                    onUpdateShadowRadius = { id, sr -> viewModel.updateShapeShadowRadius(id, sr) },
                    onUpdateBlurRadius = { id, br -> viewModel.updateShapeBlurRadius(id, br) },
                    onUpdateTextContent = { id, text -> viewModel.updateTextLayerContent(id, text) },
                    onCommitTextPosition = { id, x, y -> viewModel.updateTextPosition(id, x, y) },
                    onCommitTextScale = { id, size -> viewModel.updateTextScale(id, size) },
                    onCommitTextDimensions = { id, sx, sy -> viewModel.updateTextDimensions(id, sx, sy) },
                    onCommitTextRotation = { id, deg -> viewModel.updateTextRotation(id, deg) },
                    onSetTextColorIndex = { id, idx -> viewModel.setTextColorIndex(id, idx) },
                    onBringTextToFront = { id -> viewModel.bringTextToFront(id) },
                    onSendTextToBack = { id -> viewModel.sendTextToBack(id) },
                    onDeleteText = { id -> viewModel.deleteTextLayer(id) },
                    onUpdateTextOpacity = { id, op -> viewModel.updateTextOpacity(id, op) },
                    onUpdateTextShadowRadius = { id, sr -> viewModel.updateTextShadowRadius(id, sr) },
                    onUpdateTextTypographyStyle = { id, style -> viewModel.updateTextTypographyStyle(id, style) },
                    onToggleTextOutlineOnly = { id -> viewModel.toggleTextOutlineOnly(id) },
                    onUpdateTextStrokeWidth = { id, sw -> viewModel.updateTextStrokeWidth(id, sw) },
                    onUpdateTextGlowRadius = { id, gr -> viewModel.updateTextGlowRadius(id, gr) },
                    onUpdateTextLetterSpacing = { id, sp -> viewModel.updateTextLetterSpacing(id, sp) },
                    onUpdateTextLineHeight = { id, mult -> viewModel.updateTextLineHeight(id, mult) },
                    onImportCustomFont = { id, uri -> viewModel.importCustomFont(id, uri, context) },
                    onClearCustomFont = { id -> viewModel.clearCustomFont(id) },
                    onCommitImagePosition = { id, x, y -> viewModel.updateImagePosition(id, x, y) },
                    onCommitImageDimensions = { id, w, h -> viewModel.updateImageDimensions(id, w, h) },
                    onCommitImageRotation = { id, deg -> viewModel.updateImageRotation(id, deg) },
                    onUpdateImageShapeType = { id, type -> viewModel.updateImageShapeType(id, type) },
                    onBringImageToFront = { id -> viewModel.bringImageToFront(id) },
                    onSendImageToBack = { id -> viewModel.sendImageToBack(id) },
                    onDuplicateImage = { id -> viewModel.duplicateImageLayer(id) },
                    onDeleteImage = { id -> viewModel.deleteImageLayer(id) },
                    onToggleImageLockProportions = { id -> viewModel.toggleImageLockProportions(id) },
                    onUpdateImageOpacity = { id, op -> viewModel.updateImageOpacity(id, op) },
                    onUpdateImageShadowRadius = { id, sr -> viewModel.updateImageShadowRadius(id, sr) },
                    onCommitGroupTranslation = { dx, dy -> viewModel.commitGroupTranslation(dx, dy) },
                    onCommitGroupScale = { factor -> viewModel.commitGroupScale(factor) },
                    onSelectAllItems = { viewModel.selectAllItems() },
                    onClearSelection = { viewModel.clearSelection() },
                    onDeleteSelectedGroup = { viewModel.deleteSelectedGroup() },
                    onBringGroupForward = { viewModel.bringGroupToFront() },
                    onSendGroupToBack = { viewModel.sendGroupToBack() },
                    onExitMultiSelect = { viewModel.toggleMultiSelectMode() },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (uiState.isFullscreenPreview) {
                                Modifier.clickable { viewModel.toggleFullscreen() }
                            } else Modifier
                        )
                )
            }
        }

        // ==========================================
        // LAYER 1: Foreground UI Chrome (Always Visible)
        // ==========================================
        if (!uiState.isFullscreenPreview) {
            // Top App Bar
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .zIndex(10f)
            )

            // Update Banner Pill (GitHub Releases)
            if (uiState.gitHubReleaseInfo.isUpdateAvailable) {
                UpdateBannerPill(
                    releaseInfo = uiState.gitHubReleaseInfo,
                    onDismiss = { viewModel.dismissUpdateBanner() },
                    context = context,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                        .zIndex(12f)
                )
            }

            // Floating Bottom Command Dock with Material 3 Expressive Animation & Sheet Collision Avoidance
            val isAnySheetActive = isAnySheetOpen

            AnimatedVisibility(
                visible = !isAnySheetActive && !uiState.isFullscreenPreview,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = ExpressiveMotionSpec.SPATIAL_DAMPING,
                        stiffness = ExpressiveMotionSpec.SPATIAL_STIFFNESS
                    )
                ) + fadeIn(
                    animationSpec = spring(
                        dampingRatio = ExpressiveMotionSpec.SETTLING_DAMPING,
                        stiffness = ExpressiveMotionSpec.SETTLING_STIFFNESS
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = ExpressiveMotionSpec.EXIT_DAMPING,
                        stiffness = ExpressiveMotionSpec.EXIT_STIFFNESS
                    )
                ) + fadeOut(
                    animationSpec = spring(
                        dampingRatio = ExpressiveMotionSpec.SETTLING_DAMPING,
                        stiffness = ExpressiveMotionSpec.SETTLING_STIFFNESS
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f)
            ) {
                val dockPaddingModifier = if (uiState.settings.bottomDockMode == BottomDockMode.ATTACHED_BAR) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                }

                Box(modifier = dockPaddingModifier) {
                    if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                        ExpressiveStudioDock(
                            palette = uiState.params.palette,
                            styleIcon = getPatternIcon(uiState.params.patternType),
                            dockMode = uiState.settings.bottomDockMode,
                            isAiEnabled = uiState.settings.isAiEnabled,
                            isMultiSelectActive = uiState.isMultiSelectMode,
                            onOpenStyleSheet = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showStyleSheet(true)
                            },
                            onOpenAddShapeSheet = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showAddShapeSheet(true)
                            },
                            onOpenAddTextSheet = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showAddTextSheet(true)
                            },
                            onPickImage = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onGenerateLayout = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.generateProceduralStudioLayout()
                            },
                            onOpenPaletteSheet = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showPaletteSheet(true)
                            },
                            onOpenAiSheet = {
                                showAiStudioSheet = true
                            },
                            onToggleMultiSelect = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.toggleMultiSelectMode()
                            },
                            onOpenSavedDesigns = {
                                viewModel.showSavedDesignsSheet(true)
                            },
                            onExportWallpaper = {
                                viewModel.saveToGallery(context)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        HomeBottomDock(
                            patternIcon = getPatternIcon(uiState.params.patternType),
                            patternName = uiState.params.patternType.displayName,
                            palette = uiState.params.palette,
                            dockMode = uiState.settings.bottomDockMode,
                            isStudioMode = uiState.params.patternType == WallpaperPatternType.STUDIO,
                            onOpenStyleSheet = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showStyleSheet(true)
                            },
                            onToggleStudio = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                if (uiState.params.patternType == WallpaperPatternType.STUDIO) {
                                    viewModel.setPatternType(WallpaperPatternType.MOUNTAINS)
                                } else {
                                    viewModel.setPatternType(WallpaperPatternType.STUDIO)
                                }
                            },
                            onGenerate = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.onGenerateOrShuffleClicked()
                            },
                            onOpenPaletteSheet = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showPaletteSheet(true)
                            },
                            onSaveStylePreset = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.saveCurrentDesign("${uiState.params.patternType.displayName} Preset")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Style Preset Saved!", duration = SnackbarDuration.Short)
                                }
                            },
                            onOpenSavedDesigns = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.showSavedDesignsSheet(true)
                            },
                            onExportWallpaper = {
                                viewModel.saveToGallery(context)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // High-contrast Exit Preview Pill in Fullscreen Mode
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { viewModel.toggleFullscreen() }
                    .zIndex(20f)
                    .testTag("exit_fullscreen_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Exit Preview",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
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
    // ADD DEPTH TEXT MODAL SHEET (Studio Nagasaki Display)
    // -------------------------------------------------------------
    if (uiState.showAddTextSheet) {
        AddTextBottomSheet(
            onAddText = { text -> viewModel.addTextLayer(text) },
            onDismiss = { viewModel.showAddTextSheet(false) }
        )
    }

    // -------------------------------------------------------------
    // STYLE SELECTION MODAL SHEET (Clamped Height)
    // -------------------------------------------------------------
    if (uiState.showStyleSheet) {
        GeneratorStyleSheet(
            params = uiState.params,
            hapticStrength = uiState.settings.hapticStrength,
            onParamsChange = { updateFn -> viewModel.updateParams(transform = updateFn) },
            onPatternSelect = { pattern -> viewModel.setPatternType(pattern) },
            onAspectRatioChange = { aspect -> viewModel.setAspectRatio(aspect) },
            onDismiss = { viewModel.showStyleSheet(false) }
        )
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

                // AI Palette Studio Action (Gemini BYOK)
                if (uiState.settings.isAiEnabled) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.showPaletteSheet(false)
                            showAiStudioSheet = true
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_ai_palette_studio_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate with AI (Gemini Studio)", fontWeight = FontWeight.Bold)
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

                // Canvas Background Controls (Color & Gradient)
                Text(
                    text = "Canvas Background & Gradient Tuning",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Background Mode Switcher (Palette Auto vs Solid vs Linear Gradient vs Radial Gradient)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BackgroundMode.entries.forEach { mode ->
                        val isSelected = uiState.params.backgroundConfig.mode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.updateParams { p ->
                                    p.copy(backgroundConfig = p.backgroundConfig.copy(mode = mode))
                                }
                            },
                            label = { Text(mode.displayName) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                if (uiState.params.backgroundConfig.mode == BackgroundMode.LINEAR_GRADIENT) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gradient Angle", style = MaterialTheme.typography.bodySmall)
                            Text(String.format("%.0f°", uiState.params.backgroundConfig.angleDegrees), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.params.backgroundConfig.angleDegrees,
                            onValueChange = { angle ->
                                viewModel.updateParams { p ->
                                    p.copy(backgroundConfig = p.backgroundConfig.copy(angleDegrees = angle))
                                }
                            },
                            valueRange = 0f..360f
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // -------------------------------------------------------------
                // CATEGORY FILTER TABS: [All], [Dynamic Monet], [Pastel Light], [Moody Dark]
                // -------------------------------------------------------------
                Text(
                    text = "Palette Categories",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaletteCategory.entries.forEach { category ->
                        val isCatSelected = category == selectedPaletteCategory
                        FilterChip(
                            selected = isCatSelected,
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                selectedPaletteCategory = category
                            },
                            label = { Text(category.displayName) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                when (selectedPaletteCategory) {
                    PaletteCategory.PASTEL_LIGHT -> {
                        // High-Luminance Light Mode (L* >= 85)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Curated Light Mode (L* ≥ 85)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Perceptually balanced high-luminance light palettes with airy surfaces and crisp contrast.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val lightPalettes = PaletteEngine.getPalettesByCategory(PaletteCategory.PASTEL_LIGHT)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lightPalettes.forEach { palette ->
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
                    }

                    PaletteCategory.DYNAMIC_MONET -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Dynamic Monet & Harmonics",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Material You dynamic extraction from wallpaper and device accent colors.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // My Custom Palettes (if any)
                        if (uiState.userCustomPalettes.isNotEmpty()) {
                            Text(
                                text = "Custom Saved Palettes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
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
                        }

                        val monetPalettes = PaletteEngine.getPalettesByCategory(PaletteCategory.DYNAMIC_MONET)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            monetPalettes.forEach { palette ->
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
                    }

                    PaletteCategory.MOODY_DARK -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Moody Dark & OLED Obsidian",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Pure black OLED foundations, velvet shadows, and atmospheric deep contrast tones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val darkPalettes = PaletteEngine.getPalettesByCategory(PaletteCategory.MOODY_DARK)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            darkPalettes.forEach { palette ->
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
                    }

                    PaletteCategory.ALL -> {
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

    // AI Palette Studio Sheet (Gemini BYOK)
    if (showAiStudioSheet && uiState.settings.isAiEnabled) {
        AIPaletteStudioSheet(
            activePalette = uiState.params.palette,
            patternName = uiState.params.patternType.displayName,
            onApplyPalette = { pal ->
                viewModel.setPalette(pal)
            },
            onEditStops = { pal ->
                viewModel.setPalette(pal)
                viewModel.showCustomPaletteBuilder(true)
            },
            onDismiss = { showAiStudioSheet = false }
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

    // First-Launch Changelog Modal Sheet
    if (uiState.isChangelogOpen) {
        WavyChangelogBottomSheet(
            onDismiss = { viewModel.dismissChangelog() },
            onLetsCreateClicked = { viewModel.dismissChangelog() }
        )
    }

    // Saved Styles & Studio Presets Modal Sheet
    if (uiState.showSavedDesignsSheet) {
        SavedStylesBottomSheet(
            designs = uiState.savedDesigns,
            activeParams = uiState.params,
            onDismiss = { viewModel.showSavedDesignsSheet(false) },
            onSaveCurrentDesign = { name ->
                viewModel.saveCurrentDesign(name)
                scope.launch {
                    snackbarHostState.showSnackbar("Preset \"$name\" saved!", duration = SnackbarDuration.Short)
                }
            },
            onLoadDesign = { design ->
                viewModel.loadDesign(design)
                viewModel.showSavedDesignsSheet(false)
                scope.launch {
                    snackbarHostState.showSnackbar("Applied \"${design.name}\"", duration = SnackbarDuration.Short)
                }
            },
            onDuplicateDesign = { id -> viewModel.duplicateDesign(id) },
            onDeleteDesign = { id -> viewModel.deleteDesign(id) }
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

fun getPatternIcon(pattern: WallpaperPatternType): ImageVector {
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
        WallpaperPatternType.GRIDS -> Icons.Default.GridOn
        WallpaperPatternType.SLICES -> Icons.Default.ContentCut
        WallpaperPatternType.CROSS_CURVES -> Icons.Default.Gesture
        WallpaperPatternType.PAPER_RIDGES -> Icons.Default.Layers
    }
}
