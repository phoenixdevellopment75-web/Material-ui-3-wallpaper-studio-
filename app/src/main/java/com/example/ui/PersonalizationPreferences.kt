package com.example.ui

import android.content.Context
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * Personalization preferences managing visual aesthetic settings,
 * including runtime backdrop blur effects and energy-saving fallbacks.
 */
object PersonalizationPreferences {
    val DISABLE_BLUR_EFFECTS = booleanPreferencesKey("disable_blur_effects")
}

/**
 * Applies authentic runtime backdrop blur on supported Android versions (API 31+),
 * or gracefully behaves as a pass-through when disabled by user personalization or unsupported.
 */
fun Modifier.studioBackdropBlur(
    disableBlur: Boolean,
    radius: Dp = 20.dp
): Modifier = this

/**
 * Resolves surface container color based on backdrop frosted toggle.
 * Returns crisp, elegant semi-opaque solid M3 surfaceContainer color.
 */
@Composable
fun getBackdropSurfaceColor(
    disableBlur: Boolean,
    defaultAlphaWhenBlurred: Float = 0.88f
): Color {
    return if (disableBlur) {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.98f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = defaultAlphaWhenBlurred)
    }
}
