package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.palette.PaletteEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WallpaperViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

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

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        // Quick Seed Shuffle
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.randomizeSeed()
                            },
                            modifier = Modifier.testTag("topbar_shuffle_button")
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle Seed")
                        }

                        // Toggle Launcher Mockup Overlay
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleFullscreen()
                            },
                            modifier = Modifier.testTag("topbar_fullscreen_button")
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen Preview")
                        }

                        // Settings & Preferences
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.openSettings(true)
                            },
                            modifier = Modifier.testTag("topbar_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !uiState.isFullscreenPreview,
                enter = fadeIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.showExportDialog(true)
                    },
                    icon = { Icon(Icons.Default.Wallpaper, contentDescription = null) },
                    text = { Text("Set Wallpaper", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier
                        .testTag("main_set_wallpaper_fab")
                        .padding(bottom = 8.dp)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Interactive Wallpaper Canvas View
                WallpaperCanvasPreview(
                    bitmap = uiState.previewBitmap,
                    isGenerating = uiState.isGeneratingPreview,
                    aspectRatioPreset = uiState.params.aspectRatio,
                    isFullscreen = uiState.isFullscreenPreview,
                    showLauncherMockup = uiState.showLauncherMockup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Bottom Inspector Toolset
                AnimatedVisibility(
                    visible = !uiState.isFullscreenPreview,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    InspectorContent(
                        state = uiState,
                        viewModel = viewModel,
                        onTabSelected = { tab -> viewModel.setActiveTab(tab) },
                        onParamsChange = { transform -> viewModel.updateParams(transform) },
                        onRollSeed = { viewModel.randomizeSeed() },
                        onOpenColorPicker = { index -> viewModel.showColorPicker(true, index) },
                        onApplyDynamicMonet = {
                            viewModel.applyDynamicTheme(
                                primary = primaryColor,
                                secondary = secondaryColor,
                                tertiary = tertiaryColor,
                                surface = surfaceColor,
                                background = backgroundColor
                            )
                        },
                        onApplyAlgorithmicHarmony = { mode ->
                            val base = uiState.params.palette.colors.firstOrNull() ?: primaryColor
                            viewModel.applyAlgorithmicPalette(mode, base)
                        },
                        onAddColorStop = {
                            viewModel.addColorStop(Color(0xFF00F2FE))
                        },
                        onRemoveColorStop = { index ->
                            viewModel.removeColorStop(index)
                        },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }

            // Floating Controls in Fullscreen Mode
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
            settings = uiState.settings,
            onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
            onResetDefaults = { viewModel.resetToDefaults() },
            onNavigateBack = { viewModel.openSettings(false) }
        )
    }
}
