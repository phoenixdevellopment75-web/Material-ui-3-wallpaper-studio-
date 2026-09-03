package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.StaticThemePreset
import com.example.ui.ThemeMode

// --- Default Monet / Fallback Palettes ---
private val DefaultDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerHigh = Color(0xFF2B2930),
    onBackground = Color(0xFFE6E0E9),
    onSurface = Color(0xFFE6E0E9)
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFFEF7FF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFECE6F0),
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20)
)

// --- Curated Minimalist Static Themes ---
// 1. Warm Sand
private val WarmSandLight = lightColorScheme(
    primary = Color(0xFF8C4F28),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF370E00),
    secondary = Color(0xFF765748),
    surface = Color(0xFFFFF8F6),
    background = Color(0xFFFFF8F6),
    surfaceContainerLow = Color(0xFFFBF1EC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF0E3DC),
    onSurface = Color(0xFF221A15)
)

private val WarmSandDark = darkColorScheme(
    primary = Color(0xFFFFB590),
    onPrimary = Color(0xFF542100),
    primaryContainer = Color(0xFF713713),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFE6BEAB),
    surface = Color(0xFF19120E),
    background = Color(0xFF19120E),
    surfaceContainerLow = Color(0xFF221A15),
    surfaceContainerLowest = Color(0xFF140D0A),
    surfaceContainerHigh = Color(0xFF2E241F),
    onSurface = Color(0xFFEFE0D9)
)

// 2. Nordic Slate
private val NordicSlateLight = lightColorScheme(
    primary = Color(0xFF385E8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF545F70),
    surface = Color(0xFFF8F9FF),
    background = Color(0xFFF8F9FF),
    surfaceContainerLow = Color(0xFFF0F3FA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    onSurface = Color(0xFF191C20)
)

private val NordicSlateDark = darkColorScheme(
    primary = Color(0xFFA5C8FE),
    onPrimary = Color(0xFF00315D),
    primaryContainer = Color(0xFF1E4772),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFBCC7DB),
    surface = Color(0xFF111418),
    background = Color(0xFF111418),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainerLowest = Color(0xFF0C0E12),
    surfaceContainerHigh = Color(0xFF23262B),
    onSurface = Color(0xFFE2E2E8)
)

// 3. OLED Obsidian
private val OledObsidianDark = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF2A2338),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    surface = Color(0xFF000000),
    background = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerHigh = Color(0xFF141414),
    onSurface = Color(0xFFF0F0F0)
)

private val OledObsidianLight = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    surface = Color(0xFFFCFCFF),
    background = Color(0xFFFCFCFF),
    surfaceContainerLow = Color(0xFFF5F5FA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFEAEAF0),
    onSurface = Color(0xFF1A1A1E)
)

@Composable
fun MyApplicationTheme(
    dynamicMonetEnabled: Boolean = true,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    staticPreset: StaticThemePreset = StaticThemePreset.WARM_SAND,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val context = LocalContext.current

    val colorScheme: ColorScheme = if (dynamicMonetEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (isDark) DefaultDarkColorScheme else DefaultLightColorScheme
        }
    } else {
        when (staticPreset) {
            StaticThemePreset.WARM_SAND -> if (isDark) WarmSandDark else WarmSandLight
            StaticThemePreset.NORDIC_SLATE -> if (isDark) NordicSlateDark else NordicSlateLight
            StaticThemePreset.OLED_OBSIDIAN -> if (isDark) OledObsidianDark else OledObsidianLight
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
