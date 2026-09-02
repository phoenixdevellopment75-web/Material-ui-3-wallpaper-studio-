package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.palette.ColorPalette
import com.example.palette.PaletteEngine

/**
 * Procedural Pattern Families - strictly curated 5 Minimalist Material 3 styles.
 */
enum class WallpaperPatternType(
    val displayName: String,
    val description: String,
    val subTypes: List<String>
) {
    NESTED_ARCHES(
        displayName = "Nested Arches & Pills",
        description = "Concentric rounded arch vectors, layered elevation shadows, and tonal step progression",
        subTypes = listOf("Concentric Portal", "Staggered Colonnade", "Asymmetric Bauhaus Arch", "Inverted Reflection Arch", "Modernist Cascade")
    ),
    TOPOGRAPHIC_CONTOURS(
        displayName = "Topographic Contours",
        description = "Clean iso-line contour vector paths generated with smooth 2D noise isolines",
        subTypes = listOf("Alpine Iso-Bands", "Minimalist Ridge Lines", "Oceanic Trench", "Subtle Basin", "Dual Peak Topography")
    ),
    DESERT_DUNES(
        displayName = "Desert Dunes & Shadows",
        description = "Parametric cubic Bézier ridgelines with chiaroscuro light/shadow division",
        subTypes = listOf("Sahara Crests", "Erg Ribbons", "Golden Hour Dunes", "Wind Ripples & Crests", "Sunset Dune Horizon")
    ),
    ORGANIC_SCALLOPS(
        displayName = "Organic M3 Scallops",
        description = "Procedural M3 flower tokens, squiggles, starbursts, pebbles, and ring accents",
        subTypes = listOf("Scalloped Flower Grid", "Organic Pebble Drift", "M3 Badge Mosaic", "Floating Token Rings", "Playful Modernist")
    ),
    PASTURE_FOLIAGE(
        displayName = "Pasture & Foliage",
        description = "Smooth rolling hills with geometric minimalist foliage crowns and vector stems",
        subTypes = listOf("Rolling Meadow & Pines", "Nordic Orchard", "Tuscan Cypress", "Minimal Birch Grove", "Sunset Savanna")
    )
}

/**
 * Aspect Ratio Presets for phone, tablet, and desktop export.
 */
enum class AspectRatioPreset(
    val displayName: String,
    val widthRatio: Float,
    val heightRatio: Float,
    val defaultExportWidth: Int,
    val defaultExportHeight: Int
) {
    PHONE_TALL("Phone (9:20)", 9f, 20f, 1080, 2400),
    PHONE_STANDARD("Phone (9:19.5)", 9f, 19.5f, 1170, 2532),
    PHONE_ULTRA_HD("Phone 4K (9:20)", 9f, 20f, 1440, 3200),
    TABLET("Tablet (16:10)", 10f, 16f, 1600, 2560),
    DESKTOP("Desktop (16:9)", 16f, 9f, 3840, 2160),
    SQUARE("Square (1:1)", 1f, 1f, 2048, 2048);

    val ratio: Float get() = widthRatio / heightRatio
}

/**
 * Comprehensive parameter configuration defining a procedural wallpaper.
 */
data class WallpaperParams(
    val patternType: WallpaperPatternType = WallpaperPatternType.NESTED_ARCHES,
    val subTypeIndex: Int = 0,
    val seed: Long = 133742L,
    val scale: Float = 1.0f,            // Density & Frequency (0.2f .. 4.0f)
    val complexity: Float = 1.0f,       // Step count, layers, foliage density (0.2f .. 3.0f)
    val distortion: Float = 0.5f,       // Curve curvature, organic warping (0.0f .. 2.0f)
    val lineWidth: Float = 2.0f,        // Stroke width for lines and outlines (0.5f .. 8.0f)
    val colorCycleFreq: Float = 1.0f,   // Color progression step rate (0.5f .. 3.0f)
    val rotationDegrees: Float = 0.0f,  // 0f .. 360f
    val isWireframe: Boolean = false,   // Wireframe / outline emphasis vs filled render
    val contrast: Float = 1.0f,         // 0.5f .. 2.0f
    val brightness: Float = 0.0f,       // -0.5f .. 0.5f
    val palette: ColorPalette = PaletteEngine.PRESET_WARM_SUNSET,
    val aspectRatio: AspectRatioPreset = AspectRatioPreset.PHONE_TALL
) {
    val subTypeName: String
        get() = patternType.subTypes.getOrElse(subTypeIndex) { patternType.subTypes.first() }

    fun withNextSubType(): WallpaperParams {
        val nextIndex = (subTypeIndex + 1) % patternType.subTypes.size
        return copy(subTypeIndex = nextIndex)
    }

    fun withPreviousSubType(): WallpaperParams {
        val prevIndex = if (subTypeIndex - 1 < 0) patternType.subTypes.size - 1 else subTypeIndex - 1
        return copy(subTypeIndex = prevIndex)
    }
}
