package com.example.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.roundToInt

/**
 * Gradient distribution style for rendering mathematical procedural textures.
 */
enum class GradientType(val displayName: String) {
    LINEAR("Linear"),
    RADIAL("Radial"),
    SWEEP("Angular Sweep"),
    DIAMOND("Diamond")
}

/**
 * Represents an ordered color palette with interpolation and shading helpers.
 */
data class ColorPalette(
    val id: String,
    val name: String,
    val colors: List<Color>,
    val gradientType: GradientType = GradientType.LINEAR,
    val isDarkBackground: Boolean = true
) {
    init {
        require(colors.isNotEmpty()) { "Color palette must have at least 1 color" }
    }

    /**
     * Interpolates color along [0.0f, 1.0f] smoothly across all stops in the palette.
     */
    fun getColorAt(fraction: Float): Color {
        if (colors.size == 1) return colors[0]
        val clamped = fraction.coerceIn(0f, 1f)
        val scaled = clamped * (colors.size - 1)
        val index = scaled.toInt().coerceIn(0, colors.size - 2)
        val localFraction = scaled - index

        val c1 = colors[index]
        val c2 = colors[index + 1]

        return Color(
            red = c1.red + (c2.red - c1.red) * localFraction,
            green = c1.green + (c2.green - c1.green) * localFraction,
            blue = c1.blue + (c2.blue - c1.blue) * localFraction,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * localFraction
        )
    }

    /**
     * Cycles color periodically across [0.0f, 1.0f] with frequency multiplier.
     */
    fun getCyclicColorAt(fraction: Float, frequency: Float = 1.0f): Color {
        val wrapped = (fraction * frequency) % 1.0f
        val norm = if (wrapped < 0f) wrapped + 1f else wrapped
        return getColorAt(norm)
    }

    fun toArgbList(): IntArray {
        return colors.map { it.toArgb() }.toIntArray()
    }
}

/**
 * Palette engine with strict perceptual color science, tonal ramp matrices,
 * and dynamic algorithmic palette builders.
 */
object PaletteEngine {

    // 1. Pixel Minimal: Warm Clay
    val PRESET_WARM_CLAY = ColorPalette(
        id = "warm_clay",
        name = "Warm Clay",
        colors = listOf(
            Color(0xFF2C1E18), // Deep Umber Base
            Color(0xFF5C3D31), // Toasted Sienna
            Color(0xFF9E654E), // Terracotta
            Color(0xFFC78B72), // Warm Ochre Clay
            Color(0xFFE5BCA7), // Soft Sand
            Color(0xFFF9EDE4)  // Pale Linen
        ),
        gradientType = GradientType.LINEAR
    )

    // 2. Pixel Minimal: Nordic Sage
    val PRESET_NORDIC_SAGE = ColorPalette(
        id = "nordic_sage",
        name = "Nordic Sage",
        colors = listOf(
            Color(0xFF1B2824), // Deep Pine Charcoal
            Color(0xFF2E453E), // Dark Spruce
            Color(0xFF4D6C63), // Muted Eucalyptus
            Color(0xFF75978C), // Nordic Sage
            Color(0xFFA7C2B9), // Pale Lichen
            Color(0xFFE8F1ED)  // Glacial Mist
        ),
        gradientType = GradientType.LINEAR
    )

    // 3. Pixel Minimal: Desert Dune
    val PRESET_DESERT_DUNE = ColorPalette(
        id = "desert_dune",
        name = "Desert Dune",
        colors = listOf(
            Color(0xFF332014), // Espresso Dune Shadow
            Color(0xFF6B4226), // Roasted Amber
            Color(0xFFA86F3E), // Saharan Gold
            Color(0xFFD49E6A), // Sunlit Ochre
            Color(0xFFEACCA5), // Warm Sand
            Color(0xFFFAF3E8)  // Desert Bone
        ),
        gradientType = GradientType.LINEAR
    )

    // 4. Pixel Minimal: Terracotta Dawn
    val PRESET_TERRACOTTA_DAWN = ColorPalette(
        id = "terracotta_dawn",
        name = "Terracotta Dawn",
        colors = listOf(
            Color(0xFF2B131E), // Velvet Plum
            Color(0xFF5E2436), // Deep Burgundy
            Color(0xFF9A3E4E), // Terracotta Crimson
            Color(0xFFD26966), // Coral Blush
            Color(0xFFF09F8D), // Soft Apricot
            Color(0xFFFDF0EC)  // Cream Alabaster
        ),
        gradientType = GradientType.LINEAR
    )

    // 5. Pixel Minimal: OLED Obsidian
    val PRESET_OLED_OBSIDIAN = ColorPalette(
        id = "oled_obsidian",
        name = "OLED Obsidian",
        colors = listOf(
            Color(0xFF000000), // Pure OLED True Black
            Color(0xFF0E131F), // Dark Slate
            Color(0xFF1D2A44), // Midnight Navy
            Color(0xFF344D75), // Deep Cobalt
            Color(0xFF5C7FA8), // Steel Blue
            Color(0xFFBFD7ED)  // Frosted Ice
        ),
        gradientType = GradientType.RADIAL
    )

    // 6. Warm Sunset
    val PRESET_WARM_SUNSET = ColorPalette(
        id = "warm_sunset",
        name = "Warm Sunset",
        colors = listOf(
            Color(0xFF1F0C29),
            Color(0xFF4A1E56),
            Color(0xFF8B2F63),
            Color(0xFFCF4D5F),
            Color(0xFFEC8350),
            Color(0xFFFDCB6E)
        ),
        gradientType = GradientType.LINEAR
    )

    // 7. Alpine Glade
    val PRESET_ALPINE_GLADE = ColorPalette(
        id = "alpine_glade",
        name = "Alpine Glade",
        colors = listOf(
            Color(0xFF142416),
            Color(0xFF284A2C),
            Color(0xFF47724C),
            Color(0xFF74A07A),
            Color(0xFFADD1B2),
            Color(0xFFEBF5EC)
        ),
        gradientType = GradientType.LINEAR
    )

    // 8. M3 Tonal Palette
    val PRESET_M3_TONAL = ColorPalette(
        id = "m3_tonal",
        name = "M3 Tonal Palette",
        colors = listOf(
            Color(0xFF1C1B1F),
            Color(0xFF313033),
            Color(0xFF484649),
            Color(0xFF79747E),
            Color(0xFFCAC4D0),
            Color(0xFFE6E1E5)
        ),
        gradientType = GradientType.LINEAR
    )

    val allPresets = listOf(
        PRESET_WARM_CLAY,
        PRESET_NORDIC_SAGE,
        PRESET_DESERT_DUNE,
        PRESET_TERRACOTTA_DAWN,
        PRESET_OLED_OBSIDIAN,
        PRESET_WARM_SUNSET,
        PRESET_ALPINE_GLADE,
        PRESET_M3_TONAL
    )

    /**
     * Builds a Material You dynamic palette from the current MaterialTheme colors.
     */
    fun createFromDynamicScheme(
        primary: Color,
        secondary: Color,
        tertiary: Color,
        surface: Color,
        background: Color
    ): ColorPalette {
        // Enforce monotonic tonal layering from deep background up to bright highlight
        val rawColors = listOf(
            background,
            surface,
            secondary,
            primary,
            tertiary,
            Color.White
        )
        val sortedMonotonic = rawColors.sortedBy { calculateLuminance(it) }

        return ColorPalette(
            id = "dynamic_monet",
            name = "Material You Monet",
            colors = sortedMonotonic,
            gradientType = GradientType.LINEAR
        )
    }

    /**
     * Generates an algorithmic Monochromatic palette with monotonic 10% lightness steps.
     */
    fun generateMonochromatic(baseColor: Color): ColorPalette {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
        val h = hsv[0]
        val s = hsv[1].coerceIn(0.2f, 0.85f)

        val colors = listOf(
            colorFromHsv(h, s * 0.95f, 0.12f),
            colorFromHsv(h, s * 0.85f, 0.26f),
            colorFromHsv(h, s * 0.75f, 0.44f),
            colorFromHsv(h, s * 0.65f, 0.64f),
            colorFromHsv(h, s * 0.45f, 0.82f),
            colorFromHsv(h, s * 0.20f, 0.96f)
        )
        return ColorPalette(
            id = "mono_${h.toInt()}",
            name = "Monochromatic (${h.toInt()}°)",
            colors = colors
        )
    }

    /**
     * Generates an algorithmic Complementary palette with strict luminance ramp.
     */
    fun generateComplementary(baseColor: Color): ColorPalette {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
        val h = hsv[0]
        val compH = (h + 180f) % 360f

        val colors = listOf(
            colorFromHsv(h, 0.75f, 0.14f),
            colorFromHsv(h, 0.65f, 0.38f),
            colorFromHsv(h, 0.55f, 0.65f),
            colorFromHsv(compH, 0.55f, 0.75f),
            colorFromHsv(compH, 0.40f, 0.88f),
            colorFromHsv(compH, 0.15f, 0.97f)
        )
        return ColorPalette(
            id = "comp_${h.toInt()}",
            name = "Complementary Harmonic",
            colors = colors
        )
    }

    /**
     * Generates an algorithmic Triadic palette with clean tonal progression.
     */
    fun generateTriadic(baseColor: Color): ColorPalette {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
        val h1 = hsv[0]
        val h2 = (h1 + 120f) % 360f
        val h3 = (h1 + 240f) % 360f

        val colors = listOf(
            colorFromHsv(h1, 0.80f, 0.14f),
            colorFromHsv(h1, 0.65f, 0.40f),
            colorFromHsv(h2, 0.60f, 0.66f),
            colorFromHsv(h3, 0.55f, 0.82f),
            colorFromHsv(h1, 0.25f, 0.96f)
        )
        return ColorPalette(
            id = "triad_${h1.toInt()}",
            name = "Triadic Spectrum",
            colors = colors
        )
    }

    /**
     * Generates an algorithmic Analogous palette (subtle 25-degree neighbor steps).
     */
    fun generateAnalogous(baseColor: Color): ColorPalette {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
        val h = hsv[0]
        val h1 = (h - 25f + 360f) % 360f
        val h2 = h
        val h3 = (h + 25f) % 360f

        val colors = listOf(
            colorFromHsv(h1, 0.80f, 0.15f),
            colorFromHsv(h1, 0.70f, 0.38f),
            colorFromHsv(h2, 0.60f, 0.62f),
            colorFromHsv(h3, 0.50f, 0.80f),
            colorFromHsv(h3, 0.20f, 0.96f)
        )
        return ColorPalette(
            id = "analogous_${h.toInt()}",
            name = "Analogous Flow",
            colors = colors
        )
    }

    fun calculateLuminance(color: Color): Float {
        return 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    }

    /**
     * Enforces monotonic lightness progression to eliminate chaotic contrast inversions and muddy steps.
     */
    fun enforceMonotonicLuminance(palette: ColorPalette, ascending: Boolean = true): ColorPalette {
        val sorted = if (ascending) {
            palette.colors.sortedBy { calculateLuminance(it) }
        } else {
            palette.colors.sortedByDescending { calculateLuminance(it) }
        }
        return palette.copy(colors = sorted)
    }

    fun colorFromHsv(hue: Float, saturation: Float, value: Float, alpha: Float = 1.0f): Color {
        val hsv = floatArrayOf(hue.coerceIn(0f, 360f), saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f))
        val argb = android.graphics.Color.HSVToColor((alpha * 255).roundToInt(), hsv)
        return Color(argb)
    }

    fun colorToHex(color: Color): String {
        val r = (color.red * 255).roundToInt()
        val g = (color.green * 255).roundToInt()
        val b = (color.blue * 255).roundToInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun hexToColor(hex: String, fallback: Color = Color.White): Color {
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            val colorInt = when (cleanHex.length) {
                6 -> android.graphics.Color.parseColor("#FF$cleanHex")
                8 -> android.graphics.Color.parseColor("#$cleanHex")
                3 -> {
                    val expanded = "${cleanHex[0]}${cleanHex[0]}${cleanHex[1]}${cleanHex[1]}${cleanHex[2]}${cleanHex[2]}"
                    android.graphics.Color.parseColor("#FF$expanded")
                }
                else -> fallback.toArgb()
            }
            Color(colorInt)
        } catch (_: Exception) {
            fallback
        }
    }
}
