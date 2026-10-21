package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Root Navigation destinations for Wallpaper Studio.
 */
sealed class RootDestination(val title: String, val routeName: String) {
    data object Main : RootDestination("Studio", "main")
    data object Settings : RootDestination("Settings", "settings")
    data object SettingsHub : RootDestination("Settings", "settings_hub")
    data object Personalization : RootDestination("Personalization", "personalization")
    data object AiIntegration : RootDestination("AI Integration", "ai_integration")
    data object Advanced : RootDestination("Engine & Advanced", "advanced")
    data object About : RootDestination("About Wallpaper Studio", "about")
}

/**
 * Clean, production root navigation controller with zero ghosting.
 */
@Composable
fun RootNavigation(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navStack = remember { mutableStateListOf<RootDestination>(RootDestination.Main) }
    val currentDestination = navStack.lastOrNull() ?: RootDestination.Main

    // Sync if settings is opened or closed from ViewModel
    LaunchedEffect(uiState.isSettingsOpen) {
        if (uiState.isSettingsOpen && navStack.size == 1 && navStack.last() == RootDestination.Main) {
            navStack.add(RootDestination.Settings)
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

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            val isDrillDown = navStack.indexOf(targetState) >= navStack.indexOf(initialState)
            NavigationTransitions.createTransition(isDrillDown)
        },
        label = "wallpaper_studio_navigation",
        modifier = modifier.fillMaxSize()
    ) { destination ->
        when (destination) {
            RootDestination.Main -> {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        viewModel.openSettings(true)
                        if (navStack.lastOrNull() != RootDestination.Settings) {
                            navStack.add(RootDestination.Settings)
                        }
                    }
                )
            }

            else -> {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = uiState.settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetDefaults = { viewModel.resetToDefaults() },
                    onNavigateBack = {
                        popBack()
                    }
                )
            }
        }
    }
}
