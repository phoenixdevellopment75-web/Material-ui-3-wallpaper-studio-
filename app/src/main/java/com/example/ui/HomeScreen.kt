package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * HomeScreen alias and entrypoint for the primary wallpaper generation workspace.
 * Employs edge-to-edge full-width top bar, clean frosted bottom action deck,
 * and decluttered high-impact parametric tuning sheet.
 */
@Composable
fun HomeScreen(
    viewModel: WallpaperViewModel,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    MainScreen(
        viewModel = viewModel,
        onNavigateToSettings = onNavigateToSettings
    )
}
