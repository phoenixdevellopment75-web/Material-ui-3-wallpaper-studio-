package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.palette.ColorPalette
import com.example.palette.PaletteEngine
import java.util.UUID

/**
 * Shape types supported in the Custom Studio & M3 Procedural Assembler.
 * Official Material 3 Expressive shape token library.
 */
enum class CustomShapeType(
    val displayName: String,
    val iconDescription: String,
    val isProportional1to1: Boolean = true
) {
    CLOVER_4("4-Leaf Clover", "Material 3 expressive 4-lobe clover with filleted radii", true),
    CLOVER_8("8-Leaf Clover", "Expressive 8-lobe clover bloom", true),
    SCALLOP_12("12-Point Scallop", "12-sided flower cookie badge", true),
    SCALLOP_8("8-Point Scallop", "8-sided rounded scallop badge", true),
    SCALLOP_6("6-Point Scallop", "6-sided soft scallop badge", true),
    M3_ARCH("M3 Arch", "Pure tangent rounded arch with vertical drop walls", false),
    SEMICIRCLE("Semicircle", "Pure tangent half-circle dome", false),
    PUFFY_DIAMOND("Puffy Diamond", "Curved puffy diamond with rounded corners", true),
    GEM("M3 Gem", "Filleted polygonal faceted gem", true),
    BUN("M3 Bun", "Smooth rounded oblong bun shape", false),
    STADIUM_PILL("Stadium Pill", "Smooth elongated capsule with semi-circle caps", false),
    SLANTED_SQUIRCLE("Slanted Squircle", "Angled superellipse with soft curvature", true),
    PEBBLE("Organic Pebble", "Asymmetrical continuous organic curvature", true),
    TORUS_RING("Circle & Torus", "Pure circular ring token", true)
}

/**
 * An individual interactive shape in the Studio Canvas.
 */
data class CustomCanvasShape(
    val id: String = UUID.randomUUID().toString(),
    val type: CustomShapeType = CustomShapeType.CLOVER_4,
    val normalizedX: Float = 0.5f,       // 0.0 .. 1.0 (relative to canvas width)
    val normalizedY: Float = 0.5f,       // 0.0 .. 1.0 (relative to canvas height)
    val normalizedWidth: Float = 0.38f,  // 0.1 .. 1.2
    val normalizedHeight: Float = 0.38f, // 0.1 .. 1.2
    val rotationDeg: Float = 0.0f,       // 0.0 .. 360.0
    val colorIndex: Int = 0,             // Palette color index (0 .. size-1)
    val customColorHex: Long? = null,    // Optional override color
    val opacity: Float = 1.0f,           // 0.2 .. 1.0
    val zIndex: Int = 0,                 // Layer ordering
    val scallopLobes: Int = 8,           // 6, 8, 10, or 12 lobes
    val isWireframe: Boolean = false,
    val strokeWidth: Float = 2.5f
)

/**
 * Curated Procedural Pattern Families with clean, direct naming.
 */
enum class WallpaperPatternType(
    val displayName: String,
    val description: String,
    val subTypes: List<String>
) {
    MOUNTAINS(
        displayName = "Mountains",
        description = "Atmospheric multi-layer mountain silhouettes with misty depth haze and pine tree ridges",
        subTypes = listOf("Alpine Mist", "Nordic Pine", "Smoky Ridge", "Terracotta Canyon", "Solitude")
    ),
    WAVES(
        displayName = "Waves",
        description = "Flowing vertical S-curve gradient bands calculated via continuous cubic Bézier splines",
        subTypes = listOf("S-Bands", "Dune Drift", "Cascade", "Duo Stream", "Celadon Flow")
    ),
    STACKED_PILLS(
        displayName = "Stacked Pills",
        description = "Centered vertical stack of horizontal stadium pills with tonal ramp",
        subTypes = listOf("Warm Ochre Stack", "Sunset Horizon", "Nordic Pine Stack", "Crimson Plum", "Minimal Rungs")
    ),
    DOT_GRID(
        displayName = "Dot Grid",
        description = "Minimalist 4x7 matrix of circular tokens with vertical tonal lightness ramp",
        subTypes = listOf("Sage Matrix", "Cobalt Matrix", "Terracotta Matrix", "Nordic Grid", "Mono Matrix")
    ),
    CONTOURS(
        displayName = "Contours",
        description = "Continuous edge-to-edge scalar contour lines over soft organic paper textures",
        subTypes = listOf("Alpine Iso", "Ridge Lines", "Oceanic Trench", "Basin", "Continuous Iso")
    ),
    STUDIO(
        displayName = "Studio",
        description = "Interactive touch canvas to freely place, drag, scale, rotate, and style Material 3 shapes",
        subTypes = listOf("Freeform Studio", "Golden Ratio", "Minimalist Duo", "Pebble Zen", "Clover Badge")
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
    val patternType: WallpaperPatternType = WallpaperPatternType.MOUNTAINS,
    val subTypeIndex: Int = 0,
    val seed: Long = 133742L,
    val scale: Float = 1.0f,            // Density & Frequency (0.2f .. 4.0f)
    val complexity: Float = 1.0f,       // Step count, layers (0.2f .. 3.0f)
    val distortion: Float = 0.5f,       // Curve curvature, organic warping (0.0f .. 2.0f)
    val lineWidth: Float = 2.0f,        // Stroke width for lines and outlines (0.5f .. 8.0f)
    val colorCycleFreq: Float = 1.0f,   // Color progression step rate (0.5f .. 3.0f)
    val rotationDegrees: Float = 0.0f,  // 0f .. 360f
    val isWireframe: Boolean = false,   // Wireframe / outline emphasis vs filled render
    val contrast: Float = 1.0f,         // 0.5f .. 2.0f
    val brightness: Float = 0.0f,       // -0.5f .. 0.5f
    val palette: ColorPalette = PaletteEngine.PRESET_ALPINE_MIST,
    val aspectRatio: AspectRatioPreset = AspectRatioPreset.PHONE_TALL,
    val customShapes: List<CustomCanvasShape> = defaultInitialShapes()
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

    companion object {
        fun defaultInitialShapes(): List<CustomCanvasShape> {
            return listOf(
                CustomCanvasShape(
                    id = "shape_1",
                    type = CustomShapeType.CLOVER_4,
                    normalizedX = 0.38f,
                    normalizedY = 0.42f,
                    normalizedWidth = 0.36f,
                    normalizedHeight = 0.36f,
                    rotationDeg = 0f,
                    colorIndex = 1,
                    zIndex = 0
                ),
                CustomCanvasShape(
                    id = "shape_2",
                    type = CustomShapeType.SCALLOP_12,
                    normalizedX = 0.64f,
                    normalizedY = 0.62f,
                    normalizedWidth = 0.44f,
                    normalizedHeight = 0.44f,
                    rotationDeg = 15f,
                    scallopLobes = 12,
                    colorIndex = 2,
                    zIndex = 1
                ),
                CustomCanvasShape(
                    id = "shape_3",
                    type = CustomShapeType.PEBBLE,
                    normalizedX = 0.72f,
                    normalizedY = 0.30f,
                    normalizedWidth = 0.28f,
                    normalizedHeight = 0.28f,
                    rotationDeg = 35f,
                    colorIndex = 3,
                    zIndex = 2
                )
            )
        }
    }
}
