package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppLaunchScreen
import com.example.ui.RootNavigation
import com.example.ui.WallpaperViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: WallpaperViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      var isLaunching by rememberSaveable { mutableStateOf(true) }

      MyApplicationTheme(
        dynamicMonetEnabled = uiState.settings.dynamicMonetEnabled,
        themeMode = uiState.settings.themeMode,
        staticPreset = uiState.settings.staticThemePreset
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          RootNavigation(viewModel = viewModel)

          AnimatedVisibility(
            visible = isLaunching,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(450))
          ) {
            AppLaunchScreen(
              onLaunchComplete = { isLaunching = false }
            )
          }
        }
      }
    }
  }
}
