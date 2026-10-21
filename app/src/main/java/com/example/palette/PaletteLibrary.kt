package com.example.palette

import androidx.compose.ui.graphics.Color

/**
 * Curated Light Mode & High-Luminance Pastel Palette Library for Wallpaper Studio v2.4.20.
 *
 * Designed with perceptual color science (CIELAB L* >= 85 for base surface)
 * to balance dark-heavy palettes with soft, airy, high-contrast light themes.
 *
 * Fully verified for optimal background and foreground contrast across
 * Bauhaus Semicircle, Harmonic Waves, Fluted Arches, and Studio Custom Mode.
 */
object PaletteLibrary {

    // 1. Matcha Latte: Pale matcha green, oat milk, warm toasted bamboo
    val MATCHA_LATTE = ColorPalette(
        id = "light_matcha_latte",
        name = "Matcha Latte",
        colors = listOf(
            Color(0xFFF6F8F2), // Oat milk base (L* ~ 97)
            Color(0xFF8A9A7B), // Pale matcha green
            Color(0xFFD4B996), // Warm toasted bamboo
            Color(0xFFB5C9A4), // Soft matcha foam
            Color(0xFF5E6F50)  // Grounded tea leaf
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    // 2. Strawberry Gelato: Soft blush pink, cream white, strawberry coulis
    val STRAWBERRY_GELATO = ColorPalette(
        id = "light_strawberry_gelato",
        name = "Strawberry Gelato",
        colors = listOf(
            Color(0xFFFFF7F8), // Cream white base (L* ~ 98)
            Color(0xFFF7B5BE), // Soft blush pink
            Color(0xFFC74362), // Strawberry coulis
            Color(0xFFFCE1E4), // Whipped gelato
            Color(0xFF8B263E)  // Ripe berry accent
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    // 3. Nordic Cloud: Crisp cool white, sky blue, muted arctic fog
    val NORDIC_CLOUD = ColorPalette(
        id = "light_nordic_cloud",
        name = "Nordic Cloud",
        colors = listOf(
            Color(0xFFF5F9FC), // Crisp cool white base (L* ~ 97)
            Color(0xFF7CA6D8), // Sky blue
            Color(0xFFA8BCCE), // Muted arctic fog
            Color(0xFF3F6B99), // Deep fjord blue
            Color(0xFFDCE7F0)  // Frosted mist
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    // 4. Warm Biscuit: Golden butter, roasted wheat, fresh cream
    val WARM_BISCUIT = ColorPalette(
        id = "light_warm_biscuit",
        name = "Warm Biscuit",
        colors = listOf(
            Color(0xFFFAF7F0), // Fresh cream base (L* ~ 97)
            Color(0xFFE5B96F), // Golden butter
            Color(0xFFB8824C), // Roasted wheat
            Color(0xFFD9C3A5), // Bisque crust
            Color(0xFF6B4823)  // Toasted crust
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    // 5. Lilac Haze: Pale lavender, pastel violet, clean chalk white
    val LILAC_HAZE = ColorPalette(
        id = "light_lilac_haze",
        name = "Lilac Haze",
        colors = listOf(
            Color(0xFFFBF9FE), // Clean chalk white base (L* ~ 98)
            Color(0xFFC4B5E0), // Pale lavender
            Color(0xFF9075B8), // Pastel violet
            Color(0xFFE6DEFA), // Soft lilac haze
            Color(0xFF5C4582)  // Deep amethyst
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    // 6. Solar Cream: Soft pastel buttercup, vanilla cream, gentle warm apricot
    val SOLAR_CREAM = ColorPalette(
        id = "light_solar_cream",
        name = "Solar Cream",
        colors = listOf(
            Color(0xFFFFFDF5), // Vanilla cream base (L* ~ 98.5)
            Color(0xFFFFDE8A), // Soft pastel buttercup
            Color(0xFFF5AC72), // Gentle warm apricot
            Color(0xFFFEECCB), // Warm morning glow
            Color(0xFFC97332)  // Amber sunset
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    // Classic curated light mode pastels
    val MATCHA_CREAM = ColorPalette(
        id = "light_matcha_cream",
        name = "Matcha Cream",
        colors = listOf(
            Color(0xFFF4F6F0),
            Color(0xFF7A8B6E),
            Color(0xFFA3B899),
            Color(0xFFD8E2DC)
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    val STRAWBERRY_MILK = ColorPalette(
        id = "light_strawberry_milk",
        name = "Strawberry Milk",
        colors = listOf(
            Color(0xFFFFF5F5),
            Color(0xFFE8A598),
            Color(0xFFF3C5C5),
            Color(0xFFC97A7E)
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    val NORDIC_MIST = ColorPalette(
        id = "light_nordic_mist",
        name = "Nordic Mist",
        colors = listOf(
            Color(0xFFF5F7FA),
            Color(0xFF8FA3B0),
            Color(0xFFB8C5D6),
            Color(0xFF5C768D)
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    val SAND_DUNE_CLAY = ColorPalette(
        id = "light_sand_dune_clay",
        name = "Sand Dune & Clay",
        colors = listOf(
            Color(0xFFFDFBF7),
            Color(0xFFD4A373),
            Color(0xFFE6CCB2),
            Color(0xFFCCD5AE)
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    val PAPER_WHITE_INDIGO = ColorPalette(
        id = "light_paper_white_indigo",
        name = "Paper White & Indigo",
        colors = listOf(
            Color(0xFFFAFAFA),
            Color(0xFF2B3A4A),
            Color(0xFF6C7D93),
            Color(0xFFB0C4DE)
        ),
        gradientType = GradientType.LINEAR,
        isDarkBackground = false
    )

    val lightModePalettes: List<ColorPalette> = listOf(
        MATCHA_LATTE,
        STRAWBERRY_GELATO,
        NORDIC_CLOUD,
        WARM_BISCUIT,
        LILAC_HAZE,
        SOLAR_CREAM,
        MATCHA_CREAM,
        STRAWBERRY_MILK,
        NORDIC_MIST,
        SAND_DUNE_CLAY,
        PAPER_WHITE_INDIGO
    )
}
