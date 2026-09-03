package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation destinations for Wallpaper Studio following PixelPlayer hierarchy.
 */
sealed class RootDestination(val title: String, val routeName: String) {
    data object Main : RootDestination("Studio", "main")
    data object SettingsHub : RootDestination("Settings", "settings_hub")
    data object Personalization : RootDestination("Personalization", "personalization")
    data object AiIntegration : RootDestination("AI Integration", "ai_integration")
    data object Advanced : RootDestination("Engine & Advanced", "advanced")
    data object About : RootDestination("About Wallpaper Studio", "about")
}

/**
 * Root Navigation Controller implementing authentic PixelPlayer drill-down & return motion.
 */
@Composable
fun RootNavigation(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navStack = remember { mutableStateListOf<RootDestination>(RootDestination.Main) }
    val currentDestination = navStack.lastOrNull() ?: RootDestination.Main

    // Sync if settings is opened from MainScreen
    LaunchedEffect(uiState.isSettingsOpen) {
        if (uiState.isSettingsOpen && navStack.size == 1 && navStack.last() == RootDestination.Main) {
            navStack.add(RootDestination.SettingsHub)
        } else if (!uiState.isSettingsOpen && navStack.size > 1) {
            navStack.clear()
            navStack.add(RootDestination.Main)
        }
    }

    val canPop = navStack.size > 1
    val popBack: () -> Unit = {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.size - 1)
            if (navStack.size == 1 && navStack.first() == RootDestination.Main) {
                viewModel.openSettings(false)
            }
        }
    }

    BackHandler(enabled = canPop) {
        popBack()
    }

    val springSpec = spring<Float>(dampingRatio = 0.85f, stiffness = 420f)
    val intOffsetSpec = spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = 0.85f, stiffness = 420f)

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            val isDrillDown = navStack.indexOf(targetState) >= navStack.indexOf(initialState)

            if (isDrillDown) {
                // PixelPlayer Drill-Down:
                // Parent scales to 0.94f, slides slightly left, and fades out.
                // Child slides smoothly in from the right and fades in.
                val enter = slideInHorizontally(
                    animationSpec = intOffsetSpec,
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = springSpec)

                val exit = scaleOut(
                    animationSpec = springSpec,
                    targetScale = 0.94f
                ) + slideOutHorizontally(
                    animationSpec = intOffsetSpec,
                    targetOffsetX = { -it / 6 }
                ) + fadeOut(animationSpec = springSpec)

                enter togetherWith exit
            } else {
                // PixelPlayer Return:
                // Child slides away to the right with spring physics.
                // Parent glides forward into focus from 0.94f to 1.0f.
                val enter = scaleIn(
                    animationSpec = springSpec,
                    initialScale = 0.94f
                ) + slideInHorizontally(
                    animationSpec = intOffsetSpec,
                    initialOffsetX = { -it / 6 }
                ) + fadeIn(animationSpec = springSpec)

                val exit = slideOutHorizontally(
                    animationSpec = intOffsetSpec,
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = springSpec)

                enter togetherWith exit
            }
        },
        label = "pixelplayer_navigation",
        modifier = modifier.fillMaxSize()
    ) { destination ->
        when (destination) {
            RootDestination.Main -> {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        viewModel.openSettings(true)
                        if (navStack.lastOrNull() != RootDestination.SettingsHub) {
                            navStack.add(RootDestination.SettingsHub)
                        }
                    }
                )
            }

            RootDestination.SettingsHub -> {
                SettingsCategoryHubScreen(
                    settings = uiState.settings,
                    onNavigateToCategory = { catDestination ->
                        navStack.add(catDestination)
                    },
                    onResetDefaults = { viewModel.resetToDefaults() },
                    onNavigateBack = {
                        popBack()
                    }
                )
            }

            RootDestination.Personalization -> {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = uiState.settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetDefaults = { viewModel.resetToDefaults() },
                    onNavigateBack = { popBack() },
                    initialTab = SettingsTab.PERSONALIZATION
                )
            }

            RootDestination.AiIntegration -> {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = uiState.settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetDefaults = { viewModel.resetToDefaults() },
                    onNavigateBack = { popBack() },
                    initialTab = SettingsTab.AI_INTEGRATION
                )
            }

            RootDestination.Advanced -> {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = uiState.settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetDefaults = { viewModel.resetToDefaults() },
                    onNavigateBack = { popBack() },
                    initialTab = SettingsTab.ADVANCED
                )
            }

            RootDestination.About -> {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = uiState.settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetDefaults = { viewModel.resetToDefaults() },
                    onNavigateBack = { popBack() },
                    initialTab = SettingsTab.ABOUT
                )
            }
        }
    }
}

/**
 * PixelPlayer Parent Settings Category Hub.
 * Clean, borderless cards that drill down into sub-screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCategoryHubScreen(
    settings: AppSettingsState,
    onNavigateToCategory: (RootDestination) -> Unit,
    onResetDefaults: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_hub_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Workspace"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (settings.hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onResetDefaults()
                        },
                        modifier = Modifier.testTag("settings_hub_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Defaults"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Preferences & System",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            SettingsCategoryTile(
                title = "Personalization",
                subtitle = "Themes, Dynamic Monet colors, Display Blur toggle, and motion physics",
                icon = Icons.Default.Palette,
                onClick = {
                    if (settings.hapticStrength != HapticStrength.OFF) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onNavigateToCategory(RootDestination.Personalization)
                }
            )

            SettingsCategoryTile(
                title = "AI Integration",
                subtitle = "Gemini API key setup, generative color palettes, and smart presets",
                icon = Icons.Default.AutoAwesome,
                onClick = {
                    if (settings.hapticStrength != HapticStrength.OFF) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onNavigateToCategory(RootDestination.AiIntegration)
                }
            )

            SettingsCategoryTile(
                title = "Engine & Advanced",
                subtitle = "Rendering resolution, export formats, anti-aliasing, and haptic strength",
                icon = Icons.Default.Tune,
                onClick = {
                    if (settings.hapticStrength != HapticStrength.OFF) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onNavigateToCategory(RootDestination.Advanced)
                }
            )

            SettingsCategoryTile(
                title = "About Wallpaper Studio",
                subtitle = "Version specifications, hardware-accelerated canvas architecture",
                icon = Icons.Default.Info,
                onClick = {
                    if (settings.hapticStrength != HapticStrength.OFF) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onNavigateToCategory(RootDestination.About)
                }
            )
        }
    }
}

@Composable
private fun SettingsCategoryTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
