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
    val isProportional1to1: Boolean = true,
    val category: ShapeCategory = ShapeCategory.CORE_PRIMITIVES,
    val m3Token: M3ShapeToken = M3ShapeToken.CIRCLE
) {
    // 1. Core Primitives
    CIRCLE("Circle", "Pure circular token", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.CIRCLE),
    SQUARE_SQUIRCLE("Square / Squircle", "Continuous curvature superellipse", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.SQUARE),
    SLANTED_SQUIRCLE("Slanted", "Slanted architectural parallelogram", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.SLANTED),
    M3_ARCH("M3 Arch", "Pure tangent rounded arch with vertical drop walls", false, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.ARCH),
    SEMICIRCLE("Semicircle", "Pure tangent half-circle dome", false, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.SEMICIRCLE),
    OVAL("Oval", "M3 expressive rounded oblong oval", false, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.OVAL),
    STADIUM_PILL("Pill", "Smooth elongated capsule with semi-circle caps", false, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.PILL),
    TRIANGLE("Triangle", "Equilateral triangle with filleted corners", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.TRIANGLE),
    ARROW("Arrow", "Directional chevron indicator", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.ARROW),
    FAN("Fan", "90-degree fan wedge with rounded outer arc", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.FAN),
    DIAMOND("Diamond", "Filleted 4-point diamond", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.DIAMOND),
    CLAMSHELL("Clamshell", "Multi-fluted scallop fan", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.CLAMSHELL),
    PENTAGON("Pentagon", "Filleted 5-sided polygon", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.PENTAGON),
    GEM("M3 Gem", "Filleted polygonal faceted gem", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.GEM),
    PEBBLE("Organic Pebble", "Asymmetrical continuous organic curvature", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.COOKIE_4),
    TORUS_RING("Circle & Torus", "Pure circular ring token", true, ShapeCategory.CORE_PRIMITIVES, M3ShapeToken.CIRCLE),

    // 2. Cookies & Sunbursts
    VERY_SUNNY("Very Sunny", "16-point expressive sunburst", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.VERY_SUNNY),
    SUNNY_BADGE("Sunny Badge", "Expressive sunburst star cookie badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.SUNNY),
    COOKIE("Cookie", "Scalloped perimeter flower cookie badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_4),
    COOKIE_4("4-Sided Cookie", "4-sided rounded flower cookie", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_4),
    COOKIE_6("6-Sided Cookie", "6-sided soft scallop badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_6),
    SCALLOP_6("6-Point Scallop", "6-sided soft scallop badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_6),
    COOKIE_7("7-Sided Cookie", "7-sided rounded scallop badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_7),
    SCALLOP_8("8-Point Scallop", "8-sided rounded scallop badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_7),
    COOKIE_9("9-Sided Cookie", "9-sided rounded flower cookie", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_9),
    COOKIE_12("12-Sided Cookie", "12-sided flower cookie badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_12),
    SCALLOP_12("12-Point Scallop", "12-sided flower cookie badge", true, ShapeCategory.COOKIES_SUNBURSTS, M3ShapeToken.COOKIE_12),

    // 3. Blooms & Bursts
    CLOVER_4("4-Leaf Clover", "Material 3 expressive 4-lobe clover with filleted radii", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.CLOVER_4),
    CLOVER_8("8-Leaf Clover", "Expressive 8-lobe clover bloom", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.CLOVER_8),
    BURST("Burst", "Sharp 12-point energetic burst", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.BURST),
    SOFT_BURST("Soft Burst", "Softened 10-point radial burst", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.SOFT_BURST),
    BOOM("Boom", "Dramatic 14-point comic boom badge", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.BOOM),
    SOFT_BOOM("Soft Boom", "Softened 12-point expressive boom", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.SOFT_BOOM),
    FLOWER("Flower", "6-petal rounded bloom", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.FLOWER),
    PUFFY("Puffy", "Multi-puff organic cloud badge", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.PUFFY),
    PUFFY_DIAMOND("Puffy Diamond", "Curved puffy diamond with rounded corners", true, ShapeCategory.BLOOMS_BURSTS, M3ShapeToken.PUFFY_DIAMOND),

    // 4. Specialty Tokens
    GHOSTISH("Ghost-ish", "Playful curved dome with scalloped wavy foot", true, ShapeCategory.SPECIALTY, M3ShapeToken.GHOSTISH),
    PIXEL_CIRCLE("Pixel Circle", "Stepped quantized 8-bit circle token", true, ShapeCategory.SPECIALTY, M3ShapeToken.PIXEL_CIRCLE),
    PIXEL_TRIANGLE("Pixel Triangle", "Stepped quantized 8-bit triangle token", true, ShapeCategory.SPECIALTY, M3ShapeToken.PIXEL_TRIANGLE),
    BUN("M3 Bun", "Smooth rounded oblong bun shape", false, ShapeCategory.SPECIALTY, M3ShapeToken.BUN),
    HEART("Heart", "Filleted expressive heart token", true, ShapeCategory.SPECIALTY, M3ShapeToken.HEART)
}

/**
 * An individual interactive shape in the Studio Canvas.
 */
typealias StudioShape = CustomCanvasShape

data class CustomCanvasShape(
    override val id: String = UUID.randomUUID().toString(),
    val type: CustomShapeType = CustomShapeType.CLOVER_4,
    override val normalizedX: Float = 0.5f,       // 0.0 .. 1.0 (relative to canvas width)
    override val normalizedY: Float = 0.5f,       // 0.0 .. 1.0 (relative to canvas height)
    val normalizedWidth: Float = 0.38f,  // 0.1 .. 1.2
    val normalizedHeight: Float = 0.38f, // 0.1 .. 1.2
    val rotationDeg: Float = 0.0f,       // 0.0 .. 360.0
    val colorIndex: Int = 0,             // Palette color index (0 .. size-1)
    val customColorHex: Long? = null,    // Optional override color
    override val opacity: Float = 1.0f,           // 0.0 .. 1.0
    val shadowRadius: Float = 16.0f,     // 0.0 .. 32.0 dp (drop shadow blur radius)
    val blurRadius: Float = 0.0f,        // 0.0 .. 32.0 dp (soft edge / atmospheric glow)
    override val zIndex: Int = 0,                 // Layer ordering
    val scallopLobes: Int = 8,           // 6, 8, 10, or 12 lobes
    val isWireframe: Boolean = false,
    val strokeWidth: Float = 2.5f,
    val lockProportions: Boolean = false
) : StudioItem {
    override val scale: Float get() = normalizedWidth
    override val scaleX: Float get() = normalizedWidth
    override val scaleY: Float get() = normalizedHeight
    override val rotation: Float get() = rotationDeg
    val shadowElevation: Float get() = shadowRadius
    val glowRadius: Float get() = blurRadius

    override fun contains(
        touchPoint: androidx.compose.ui.geometry.Offset,
        canvasW: Float,
        canvasH: Float,
        baseDim: Float,
        globalScale: Float
    ): Boolean {
        val cx = normalizedX * canvasW
        val cy = normalizedY * canvasH
        val scaleFactor = globalScale * baseDim
        val shapeW = normalizedWidth * scaleFactor
        val shapeH = if (lockProportions || type == CustomShapeType.CIRCLE) shapeW else normalizedHeight * scaleFactor

        val rad = -Math.toRadians(rotationDeg.toDouble())
        val cos = kotlin.math.cos(rad).toFloat()
        val sin = kotlin.math.sin(rad).toFloat()
        val dx = touchPoint.x - cx
        val dy = touchPoint.y - cy
        val localX = dx * cos - dy * sin
        val localY = dx * sin + dy * cos

        val halfW = (shapeW / 2f) + 20f
        val halfH = (shapeH / 2f) + 20f
        return localX in -halfW..halfW && localY in -halfH..halfH
    }
}

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
        displayName = "Pills",
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
    BAUHAUS_SEMICIRCLE(
        displayName = "Blocks",
        description = "Alternating bands, clean architectural semi-circles, and stark high-contrast geometry",
        subTypes = listOf("Bauhaus Primary", "Constructivist Arch", "Geometric Vault", "Minimal Half-Moon", "Studio Bauhaus")
    ),
    FLUTED_ARCHES(
        displayName = "Arches",
        description = "Romanesque arches, fluted columns, depth gradients, and stepped architectural frames",
        subTypes = listOf("Romanesque Arcade", "Fluted Monolith", "Colonnade Hall", "Stepped Portal", "Terra Flutes")
    ),
    LAVA_BLOB(
        displayName = "Liquid",
        description = "Smooth organic Metaball physics, fluid field blending, and specular highlight reflections",
        subTypes = listOf("Liquid Lava", "Floating Blobs", "Mercury Drops", "Thermal Chamber", "Bioluminescent Goo")
    ),
    STUDIO(
        displayName = "Studio",
        description = "Interactive touch canvas to freely place, drag, scale, rotate, and style Material 3 shapes",
        subTypes = listOf("Freeform Studio", "Golden Ratio", "Minimalist Duo", "Pebble Zen", "Clover Badge")
    ),
    GRIDS(
        displayName = "Grids",
        description = "Minimalist multi-scale architectural grid lines, intersecting accents, and coordinate ticks",
        subTypes = listOf("Mono Grid", "Blueprint", "Metric 10", "Cross Grid", "Zen Coordinate")
    ),
    SLICES(
        displayName = "Slices",
        description = "Angular geometric planar diagonal cuts, layered color divisions, and sharp diagonal paper slices",
        subTypes = listOf("Diagonal Cut", "Tri-Slice", "Prism Blade", "Folded Slice", "Dual Angle")
    ),
    CROSS_CURVES(
        displayName = "Cross Curves",
        description = "Interlocking bi-directional bezier wave ribbons and cross-curved woven flow bands",
        subTypes = listOf("Woven Flow", "Sine Crossing", "Dual Ribbon", "Braid Curve", "Zen Weave")
    ),
    PAPER_RIDGES(
        displayName = "Paper",
        description = "Tactile embossed stepped paper folds, geometric shadow creases, and origami topography ridges",
        subTypes = listOf("Origami Fold", "Stepped Crease", "Prism Ridge", "V-Fold", "Shadow Ridge")
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
    val pillWidth: Float = 0.72f,       // Stacked pills width ratio (0.4f .. 1.0f)
    val pillHeight: Float = 0.065f,     // Stacked pills height ratio (0.02f .. 0.15f)
    val pillSpacing: Float = 0.025f,    // Stacked pills vertical spacing (0.0f .. 0.08f)
    val pillCurvature: Float = 1.0f,    // Stacked pills corner roundness (0.1f .. 1.0f)
    val palette: ColorPalette = PaletteEngine.PRESET_ALPINE_MIST,
    val aspectRatio: AspectRatioPreset = AspectRatioPreset.PHONE_TALL,
    val filmGrain: Float = 0.0f,        // Film Grain Noise intensity (0.0f .. 1.0f)
    val customShapes: List<CustomCanvasShape> = defaultInitialShapes(),
    val customTexts: List<StudioTextLayer> = emptyList(),
    val customImages: List<StudioImageLayer> = emptyList(),
    val backgroundConfig: CanvasBackgroundConfig = CanvasBackgroundConfig()
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
