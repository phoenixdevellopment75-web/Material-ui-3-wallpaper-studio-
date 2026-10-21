package com.example.ui

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Personalization preferences managing visual aesthetic settings.
 */
object PersonalizationPreferences

/**
 * Safe backdrop blur modifier that applies blur strictly on API 31+ (Android 12+)
 * only when displayBlurEffectsEnabled is true and only to designated wallpaper canvas backdrops
 * or modal scrims. Never applied to foreground text or card containers to prevent text corruption.
 */
fun Modifier.studioBackdropBlur(
    blurEnabled: Boolean = false,
    radius: Dp = 16.dp
): Modifier {
    return if (blurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && radius > 0.dp) {
        this.blur(radius = radius)
    } else {
        this
    }
}

/**
 * Resolves surface container color for Material 3 surfaces.
 * Returns crisp solid M3 surfaceContainer color or translucent tinted color when blur is active.
 */
@Composable
fun getBackdropSurfaceColor(
    blurEnabled: Boolean = false,
    defaultAlphaWhenBlurred: Float = 0.85f
): Color {
    val base = MaterialTheme.colorScheme.surfaceContainer
    return if (blurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        base.copy(alpha = defaultAlphaWhenBlurred)
    } else {
        base
    }
}
