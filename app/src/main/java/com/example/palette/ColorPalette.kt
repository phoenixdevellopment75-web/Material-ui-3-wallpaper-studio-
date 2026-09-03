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

    // --- CURATED STYLE-MATCHED PALETTES ---

    // Mountain: Alpine Mist
    val PRESET_ALPINE_MIST = ColorPalette(
        id = "alpine_mist",
        name = "Alpine Mist",
        colors = listOf(
            Color(0xFF0F1A17), // Deep Obsidian Pine
            Color(0xFF1E332E), // Shadow Spruce
            Color(0xFF3B564F), // Misty Evergreen
            Color(0xFF6B8A81), // Mountain Slate
            Color(0xFFA5BFB8), // Hazy Glacier
            Color(0xFFF1F5F3)  // Mist Horizon Sky
        ),
        gradientType = GradientType.LINEAR
    )

    // Mountain: Nordic Pine
    val PRESET_NORDIC_PINE = ColorPalette(
        id = "nordic_pine",
        name = "Nordic Pine",
        colors = listOf(
            Color(0xFF111E16), // Nordic Deep Forest
            Color(0xFF223A2C), // Evergreen Pine
            Color(0xFF3F5E4B), // Shadow Fern
            Color(0xFF698B75), // Nordic Moss
            Color(0xFFA3C2AE), // Pale Lichen
            Color(0xFFEAF2ED)  // Glacial Mist
        ),
        gradientType = GradientType.LINEAR
    )

    // Mountain: Smoky Blue
    val PRESET_SMOKY_BLUE = ColorPalette(
        id = "smoky_blue",
        name = "Smoky Blue",
        colors = listOf(
            Color(0xFF101726), // Midnight Ridge
            Color(0xFF1C2C47), // Deep Cobalt Summit
            Color(0xFF364E72), // Smoky Indigo
            Color(0xFF627D9E), // Blue Ridge Haze
            Color(0xFF9EAFD2), // Periwinkle Cloud
            Color(0xFFEFF2F8)  // Pale Dawn Mist
        ),
        gradientType = GradientType.LINEAR
    )

    // Mountain: Terracotta Ridge
    val PRESET_TERRACOTTA_RIDGE = ColorPalette(
        id = "terracotta_ridge",
        name = "Terracotta Ridge",
        colors = listOf(
            Color(0xFF2D1614), // Roasted Umber Canyon
            Color(0xFF5A2A23), // Deep Terracotta
            Color(0xFF944738), // Sunbaked Clay
            Color(0xFFC87865), // Warm Ochre Ridge
            Color(0xFFE5A998), // Soft Sand Mesa
            Color(0xFFFDF0EC)  // Alabaster Dawn Sky
        ),
        gradientType = GradientType.LINEAR
    )

    // Wave: Sage Monolith
    val PRESET_SAGE_MONOLITH = ColorPalette(
        id = "sage_monolith",
        name = "Sage Monolith",
        colors = listOf(
            Color(0xFF14241B), // Deep Forest Green
            Color(0xFF274334), // Jade Depths
            Color(0xFF4C6E5A), // Natural Sage
            Color(0xFF82A68F), // Muted Celadon
            Color(0xFFBAD2C3), // Pale Celadon
            Color(0xFFF2F6F3)  // Muted Cream
        ),
        gradientType = GradientType.LINEAR
    )

    // Wave: Coastal Drift
    val PRESET_COASTAL_DRIFT = ColorPalette(
        id = "coastal_drift",
        name = "Coastal Drift",
        colors = listOf(
            Color(0xFF0D252E), // Oceanic Abyss
            Color(0xFF1B4958), // Deep Marine Teal
            Color(0xFF387B8F), // Coastal Turquoise
            Color(0xFF6DAEC0), // Seafoam Drift
            Color(0xFFB3DCE5), // Foam Crest
            Color(0xFFF0F9FB)  // Salt Spray Cream
        ),
        gradientType = GradientType.LINEAR
    )

    // Wave: Matcha Latte
    val PRESET_MATCHA_LATTE = ColorPalette(
        id = "matcha_latte",
        name = "Matcha Latte",
        colors = listOf(
            Color(0xFF232A15), // Roasted Matcha Olive
            Color(0xFF424F2A), // Deep Matcha
            Color(0xFF6B7E45), // Pistachio Green
            Color(0xFF9CAE74), // Milky Sage
            Color(0xFFCCD9AF), // Soft Oat
            Color(0xFFF8FAF2)  // Vanilla Cream
        ),
        gradientType = GradientType.LINEAR
    )

    // Wave: Deep Ocean
    val PRESET_DEEP_OCEAN = ColorPalette(
        id = "deep_ocean",
        name = "Deep Ocean",
        colors = listOf(
            Color(0xFF091322), // Midnight Abyss
            Color(0xFF152A47), // Cobalt Trench
            Color(0xFF2B4D7B), // Cerulean Swell
            Color(0xFF527EAF), // Wave Crest
            Color(0xFF95BDE3), // Aquamarine Froth
            Color(0xFFEDF5FC)  // Sea Mist
        ),
        gradientType = GradientType.LINEAR
    )

    // Floating Badge: Sky Lilac
    val PRESET_SKY_LILAC = ColorPalette(
        id = "sky_lilac",
        name = "Sky Lilac",
        colors = listOf(
            Color(0xFF3B2F52), // Deep Velvet Orchid
            Color(0xFF5D4D7C), // Muted Iris
            Color(0xFF8573A8), // Soft Lilac Badge
            Color(0xFFAF9FD0), // Periwinkle Squircle
            Color(0xFFD4C8EB), // Lavender Token
            Color(0xFFF1EEF8)  // Sky Lilac Mist Canvas
        ),
        gradientType = GradientType.RADIAL
    )

    // Floating Badge: Soft Lavender
    val PRESET_SOFT_LAVENDER = ColorPalette(
        id = "soft_lavender",
        name = "Soft Lavender",
        colors = listOf(
            Color(0xFF30233D), // Midnight Plum
            Color(0xFF543C6B), // Violet Shadow
            Color(0xFF7E5E9B), // Soft Lavender Bloom
            Color(0xFFA988C7), // Orchid Token
            Color(0xFFD2BEE5), // Pale Violet
            Color(0xFFF7F2FA)  // Lavender Alabaster Canvas
        ),
        gradientType = GradientType.RADIAL
    )

    // Floating Badge: Blush Ochre
    val PRESET_BLUSH_OCHRE = ColorPalette(
        id = "blush_ochre",
        name = "Blush Ochre",
        colors = listOf(
            Color(0xFF38231E), // Deep Toasted Chestnut
            Color(0xFF694136), // Sunbaked Sienna
            Color(0xFFA16858), // Blush Ochre Scallop
            Color(0xFFD09786), // Warm Blush Token
            Color(0xFFE9C5BA), // Sand Almond
            Color(0xFFFBF4F1)  // Ivory Parchment Canvas
        ),
        gradientType = GradientType.RADIAL
    )

    // Floating Badge: Muted Periwinkle
    val PRESET_MUTED_PERIWINKLE = ColorPalette(
        id = "muted_periwinkle",
        name = "Muted Periwinkle",
        colors = listOf(
            Color(0xFF1D2640), // Navy Base
            Color(0xFF34436E), // Steel Indigo
            Color(0xFF5B6E9E), // Periwinkle Scallop
            Color(0xFF8598C7), // Cornflower Token
            Color(0xFFBDC9E7), // Hazy Violet
            Color(0xFFF0F3F9)  // Cloud White Canvas
        ),
        gradientType = GradientType.RADIAL
    )

    // Topography: Cream Parchment
    val PRESET_CREAM_PARCHMENT = ColorPalette(
        id = "cream_parchment",
        name = "Cream Parchment",
        colors = listOf(
            Color(0xFF26201B), // Sepia Charcoal Stroke
            Color(0xFF4A3F36), // Deep Sandstone
            Color(0xFF78695C), // Mineral Slate
            Color(0xFFAA9888), // Warm Dune Isoline
            Color(0xFFD9CFC4), // Parchment Grain
            Color(0xFFFAF7F2)  // Soft Cream Paper Background
        ),
        gradientType = GradientType.LINEAR
    )

    // Topography: Slate Contour
    val PRESET_SLATE_CONTOUR = ColorPalette(
        id = "slate_contour",
        name = "Slate Contour",
        colors = listOf(
            Color(0xFF12151A), // Dark Slate Background
            Color(0xFF202630), // Deep Mineral Layer
            Color(0xFF394354), // Graphite Bedrock
            Color(0xFF5F6F88), // Slate Isoline
            Color(0xFF9BB0CB), // Electric Iso Contour
            Color(0xFFEAF1F8)  // Peak Highlight
        ),
        gradientType = GradientType.LINEAR
    )

    // Topography: Desert Contour
    val PRESET_DESERT_CONTOUR = ColorPalette(
        id = "desert_contour",
        name = "Desert Contour",
        colors = listOf(
            Color(0xFF332014), // Roasted Umber
            Color(0xFF663D22), // Terracotta Line
            Color(0xFFA06437), // Sunlit Ochre
            Color(0xFFD49860), // Dune Contour
            Color(0xFFEACCA5), // Warm Sand Paper
            Color(0xFFFAF2E6)  // Sunbleached Canvas
        ),
        gradientType = GradientType.LINEAR
    )

    // Topography: Mineral Blue
    val PRESET_MINERAL_BLUE = ColorPalette(
        id = "mineral_blue",
        name = "Mineral Blue",
        colors = listOf(
            Color(0xFF13222E), // Deep Trench Stroke
            Color(0xFF243F54), // Marine Contour
            Color(0xFF436B8C), // Mineral Blue
            Color(0xFF759DC0), // Iso Crest
            Color(0xFFB6D0E6), // Glacial Shelf
            Color(0xFFF2F7FA)  // Limestone Paper Background
        ),
        gradientType = GradientType.LINEAR
    )

    // Stacked Pills: Warm Ochre Stack (Reference 1000053776)
    val PRESET_WARM_OCHRE_STACK = ColorPalette(
        id = "warm_ochre_stack",
        name = "Warm Ochre Stack",
        colors = listOf(
            Color(0xFF2E1A2C), // Deep Plum base
            Color(0xFF78253A), // Crimson
            Color(0xFFB64436), // Burnt Sienna
            Color(0xFFD97236), // Sunset Orange
            Color(0xFFE4A446), // Warm Ochre
            Color(0xFFFBF4E8)  // Sand Cream Canvas
        ),
        gradientType = GradientType.LINEAR
    )

    // Stacked Pills: Sunset Horizon
    val PRESET_SUNSET_HORIZON = ColorPalette(
        id = "sunset_horizon",
        name = "Sunset Horizon",
        colors = listOf(
            Color(0xFF1B1B3A), // Twilight Navy
            Color(0xFF49224E), // Deep Boysenberry
            Color(0xFF8B324E), // Rosewood
            Color(0xFFC75D4D), // Coral
            Color(0xFFE6986A), // Apricot Amber
            Color(0xFFFBF3EC)  // Warm Mist Canvas
        ),
        gradientType = GradientType.LINEAR
    )

    // Dot Grid: Sage Matrix (Reference 1000053775)
    val PRESET_SAGE_MATRIX = ColorPalette(
        id = "sage_matrix",
        name = "Sage Matrix",
        colors = listOf(
            Color(0xFF13261C), // Deep Forest Green
            Color(0xFF244433), // Dark Pine
            Color(0xFF456B52), // Natural Olive
            Color(0xFF759980), // Muted Sage
            Color(0xFFB2CBB9), // Pale Celadon
            Color(0xFFF4F7F4)  // Milk Canvas
        ),
        gradientType = GradientType.LINEAR
    )

    // Dot Grid: Cobalt Matrix
    val PRESET_COBALT_MATRIX = ColorPalette(
        id = "cobalt_matrix",
        name = "Cobalt Matrix",
        colors = listOf(
            Color(0xFF122238), // Midnight Cobalt
            Color(0xFF213F63), // Deep Navy
            Color(0xFF3F6996), // Slate Blue
            Color(0xFF6E99C4), // Sky Blue
            Color(0xFFAEC8E4), // Periwinkle Tint
            Color(0xFFF2F6FB)  // Ice Mist Canvas
        ),
        gradientType = GradientType.LINEAR
    )

    val allPresets = listOf(
        // Mountain Presets
        PRESET_ALPINE_MIST,
        PRESET_NORDIC_PINE,
        PRESET_SMOKY_BLUE,
        PRESET_TERRACOTTA_RIDGE,
        // Stacked Pills Presets
        PRESET_WARM_OCHRE_STACK,
        PRESET_SUNSET_HORIZON,
        // Dot Grid Presets
        PRESET_SAGE_MATRIX,
        PRESET_COBALT_MATRIX,
        // Wave Presets
        PRESET_SAGE_MONOLITH,
        PRESET_COASTAL_DRIFT,
        PRESET_MATCHA_LATTE,
        PRESET_DEEP_OCEAN,
        // Studio & Badge Presets
        PRESET_SKY_LILAC,
        PRESET_SOFT_LAVENDER,
        PRESET_BLUSH_OCHRE,
        PRESET_MUTED_PERIWINKLE,
        // Topography Presets
        PRESET_CREAM_PARCHMENT,
        PRESET_SLATE_CONTOUR,
        PRESET_DESERT_CONTOUR,
        PRESET_MINERAL_BLUE,
        // Classics
        PRESET_WARM_CLAY,
        PRESET_NORDIC_SAGE,
        PRESET_DESERT_DUNE,
        PRESET_OLED_OBSIDIAN,
        PRESET_WARM_SUNSET
    )

    /**
     * Returns curated palettes tailored specifically for the active wallpaper style.
     */
    fun getPalettesForPattern(patternType: com.example.engine.WallpaperPatternType): List<ColorPalette> {
        return when (patternType) {
            com.example.engine.WallpaperPatternType.MOUNTAINS -> listOf(
                PRESET_ALPINE_MIST,
                PRESET_NORDIC_PINE,
                PRESET_SMOKY_BLUE,
                PRESET_TERRACOTTA_RIDGE
            )
            com.example.engine.WallpaperPatternType.WAVES -> listOf(
                PRESET_SAGE_MONOLITH,
                PRESET_COASTAL_DRIFT,
                PRESET_MATCHA_LATTE,
                PRESET_DEEP_OCEAN
            )
            com.example.engine.WallpaperPatternType.STACKED_PILLS -> listOf(
                PRESET_WARM_OCHRE_STACK,
                PRESET_SUNSET_HORIZON,
                PRESET_TERRACOTTA_RIDGE,
                PRESET_WARM_SUNSET
            )
            com.example.engine.WallpaperPatternType.DOT_GRID -> listOf(
                PRESET_SAGE_MATRIX,
                PRESET_COBALT_MATRIX,
                PRESET_MATCHA_LATTE,
                PRESET_WARM_CLAY
            )
            com.example.engine.WallpaperPatternType.CONTOURS -> listOf(
                PRESET_CREAM_PARCHMENT,
                PRESET_SLATE_CONTOUR,
                PRESET_DESERT_CONTOUR,
                PRESET_MINERAL_BLUE
            )
            com.example.engine.WallpaperPatternType.STUDIO -> listOf(
                PRESET_SKY_LILAC,
                PRESET_SAGE_MONOLITH,
                PRESET_WARM_CLAY,
                PRESET_BLUSH_OCHRE,
                PRESET_ALPINE_MIST
            )
        }
    }

    /**
     * Default signature palette matching the given style.
     */
    fun getDefaultPaletteForPattern(patternType: com.example.engine.WallpaperPatternType): ColorPalette {
        return getPalettesForPattern(patternType).first()
    }

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
