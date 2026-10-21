package com.example.engine

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.pill
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shape Category taxonomy corresponding to the Material 3 Expressive Shape Spec.
 */
enum class ShapeCategory(val title: String) {
    CORE_PRIMITIVES("Core Primitives"),
    COOKIES_SUNBURSTS("Cookies & Sunbursts"),
    BLOOMS_BURSTS("Blooms & Bursts"),
    SPECIALTY("Specialty Tokens");

    val displayName: String get() = title
}

/**
 * Comprehensive catalog of all official Material 3 Expressive geometric tokens.
 * Built using Google's androidx.graphics.shapes RoundedPolygon engine.
 */
enum class M3ShapeToken(
    val tokenName: String,
    val category: ShapeCategory,
    val isProportional1to1: Boolean = true,
    val description: String = ""
) {
    // 1. Core Primitives
    CIRCLE("Circle", ShapeCategory.CORE_PRIMITIVES, true, "Pure circular token"),
    SQUARE("Square (Squircle)", ShapeCategory.CORE_PRIMITIVES, true, "Continuous curvature superellipse"),
    SLANTED("Slanted", ShapeCategory.CORE_PRIMITIVES, true, "Slanted architectural parallelogram"),
    ARCH("Arch", ShapeCategory.CORE_PRIMITIVES, false, "Pure tangent rounded arch with vertical walls"),
    SEMICIRCLE("Semicircle", ShapeCategory.CORE_PRIMITIVES, false, "Pure tangent half-circle dome"),
    OVAL("Oval", ShapeCategory.CORE_PRIMITIVES, false, "M3 expressive rounded oblong oval"),
    PILL("Pill", ShapeCategory.CORE_PRIMITIVES, false, "Smooth elongated capsule with semi-circle caps"),
    TRIANGLE("Triangle", ShapeCategory.CORE_PRIMITIVES, true, "Equilateral triangle with filleted corners"),
    ARROW("Arrow", ShapeCategory.CORE_PRIMITIVES, true, "Directional chevron indicator"),
    FAN("Fan", ShapeCategory.CORE_PRIMITIVES, true, "90-degree fan wedge with rounded outer arc"),
    DIAMOND("Diamond", ShapeCategory.CORE_PRIMITIVES, true, "Filleted 4-point diamond"),
    CLAMSHELL("Clamshell", ShapeCategory.CORE_PRIMITIVES, true, "Multi-fluted scallop fan"),
    PENTAGON("Pentagon", ShapeCategory.CORE_PRIMITIVES, true, "Filleted 5-sided polygon"),
    GEM("Gem", ShapeCategory.CORE_PRIMITIVES, true, "Faceted polygonal faceted gem"),

    // 2. Cookies & Sunbursts
    VERY_SUNNY("Very Sunny", ShapeCategory.COOKIES_SUNBURSTS, true, "16-point expressive sunburst"),
    SUNNY("Sunny", ShapeCategory.COOKIES_SUNBURSTS, true, "8-point expressive sunburst badge"),
    COOKIE_4("4-Sided Cookie", ShapeCategory.COOKIES_SUNBURSTS, true, "4-sided rounded flower cookie"),
    COOKIE_6("6-Sided Cookie", ShapeCategory.COOKIES_SUNBURSTS, true, "6-sided soft scallop badge"),
    COOKIE_7("7-Sided Cookie", ShapeCategory.COOKIES_SUNBURSTS, true, "7-sided rounded scallop badge"),
    COOKIE_9("9-Sided Cookie", ShapeCategory.COOKIES_SUNBURSTS, true, "9-sided rounded flower cookie"),
    COOKIE_12("12-Sided Cookie", ShapeCategory.COOKIES_SUNBURSTS, true, "12-sided flower cookie badge"),

    // 3. Blooms & Bursts
    CLOVER_4("4-Leaf Clover", ShapeCategory.BLOOMS_BURSTS, true, "Expressive 4-lobe clover with filleted radii"),
    CLOVER_8("8-Leaf Clover", ShapeCategory.BLOOMS_BURSTS, true, "Expressive 8-lobe clover bloom"),
    BURST("Burst", ShapeCategory.BLOOMS_BURSTS, true, "Sharp 12-point energetic burst"),
    SOFT_BURST("Soft Burst", ShapeCategory.BLOOMS_BURSTS, true, "Softened 10-point radial burst"),
    BOOM("Boom", ShapeCategory.BLOOMS_BURSTS, true, "Dramatic 14-point comic boom badge"),
    SOFT_BOOM("Soft Boom", ShapeCategory.BLOOMS_BURSTS, true, "Softened 12-point expressive boom"),
    FLOWER("Flower", ShapeCategory.BLOOMS_BURSTS, true, "6-petal rounded bloom"),
    PUFFY("Puffy", ShapeCategory.BLOOMS_BURSTS, true, "Multi-puff organic cloud badge"),
    PUFFY_DIAMOND("Puffy Diamond", ShapeCategory.BLOOMS_BURSTS, true, "Curved puffy diamond with rounded corners"),

    // 4. Specialty Tokens
    GHOSTISH("Ghost-ish", ShapeCategory.SPECIALTY, true, "Playful curved dome with scalloped wavy foot"),
    PIXEL_CIRCLE("Pixel Circle", ShapeCategory.SPECIALTY, true, "Stepped quantized 8-bit circle token"),
    PIXEL_TRIANGLE("Pixel Triangle", ShapeCategory.SPECIALTY, true, "Stepped quantized 8-bit triangle token"),
    BUN("Bun", ShapeCategory.SPECIALTY, false, "Smooth rounded oblong bun shape"),
    HEART("Heart", ShapeCategory.SPECIALTY, true, "Filleted expressive heart token")
}

/**
 * Pre-computed, memoized shape engine.
 * Computes all normalized RoundedPolygon vector paths once to ensure zero allocations inside DrawScope.
 */
object Material3ShapeLibrary {

    // Cache of normalized paths bounded in [-1f, 1f]
    private val normalizedPathCache = mutableMapOf<M3ShapeToken, Path>()
    private val matrixPool = Matrix()
    private val transformedPathCache = object : android.util.LruCache<Long, Path>(256) {}

    init {
        val bounds = RectF()
        for (token in M3ShapeToken.values()) {
            val rawPath = buildNormalizedPath(token)
            rawPath.computeBounds(bounds, true)
            val bw = bounds.width().coerceAtLeast(0.001f)
            val bh = bounds.height().coerceAtLeast(0.001f)
            val maxDim = maxOf(bw, bh)
            // 0.88f scale provides 6% clearance around all outer lobes, completely preventing bottom lobe cutoff
            val scaleFactor = (2f * 0.88f) / maxDim
            val normMatrix = Matrix().apply {
                postTranslate(-bounds.centerX(), -bounds.centerY())
                postScale(scaleFactor, scaleFactor)
            }
            val safePath = Path()
            rawPath.transform(normMatrix, safePath)
            normalizedPathCache[token] = safePath
        }
    }

    fun getNormalizedPath(token: M3ShapeToken): Path {
        return normalizedPathCache[token] ?: normalizedPathCache.getValue(M3ShapeToken.CIRCLE)
    }

    /**
     * Obtains a transformed path scaled and translated to the destination rectangle.
     * Guaranteed zero path allocation when using an existing Path container or memoized cache.
     */
    fun transformPath(
        token: M3ShapeToken,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        dstPath: Path? = null
    ): Path {
        if (dstPath == null) {
            // Compute a 64-bit spatial hash key
            val key = ((token.ordinal.toLong() and 0xFFL) shl 48) or
                ((cx.toInt().toLong() and 0xFFFL) shl 36) or
                ((cy.toInt().toLong() and 0xFFFL) shl 24) or
                ((w.toInt().toLong() and 0xFFFL) shl 12) or
                (h.toInt().toLong() and 0xFFFL)
            val cached = transformedPathCache.get(key)
            if (cached != null) {
                return cached
            }
            val base = getNormalizedPath(token)
            val path = Path()
            synchronized(matrixPool) {
                matrixPool.reset()
                matrixPool.setScale(w / 2f, h / 2f)
                matrixPool.postTranslate(cx, cy)
                base.transform(matrixPool, path)
            }
            transformedPathCache.put(key, path)
            return path
        } else {
            val base = getNormalizedPath(token)
            synchronized(matrixPool) {
                matrixPool.reset()
                matrixPool.setScale(w / 2f, h / 2f)
                matrixPool.postTranslate(cx, cy)
                base.transform(matrixPool, dstPath)
            }
            return dstPath
        }
    }

    private fun buildNormalizedPath(token: M3ShapeToken): Path {
        return when (token) {
            // Core Primitives
            M3ShapeToken.CIRCLE -> {
                RoundedPolygon.circle(numVertices = 32).toPath()
            }
            M3ShapeToken.SQUARE -> {
                RoundedPolygon.rectangle(
                    width = 2f,
                    height = 2f,
                    rounding = CornerRounding(0.42f, 0.6f)
                ).toPath()
            }
            M3ShapeToken.SLANTED -> {
                val vertices = floatArrayOf(
                    -0.65f, -1.0f,
                    1.0f, -0.65f,
                    0.65f, 1.0f,
                    -1.0f, 0.65f
                )
                RoundedPolygon(vertices, rounding = CornerRounding(0.3f, 0.5f)).toPath()
            }
            M3ShapeToken.ARCH -> {
                // Symmetrical arch: flat bottom, vertical stems, semi-circle dome cap
                val path = Path()
                path.moveTo(-1f, 1f)
                path.lineTo(-1f, 0f)
                val domeRect = RectF(-1f, -1f, 1f, 1f)
                path.arcTo(domeRect, 180f, 180f, false)
                path.lineTo(1f, 1f)
                path.close()
                path
            }
            M3ShapeToken.SEMICIRCLE -> {
                val path = Path()
                val domeRect = RectF(-1f, -1f, 1f, 1f)
                path.arcTo(domeRect, 180f, 180f, true)
                path.close()
                path
            }
            M3ShapeToken.OVAL -> {
                RoundedPolygon.pill(width = 2f, height = 1.35f).toPath()
            }
            M3ShapeToken.PILL -> {
                RoundedPolygon.pill(width = 2f, height = 0.90f).toPath()
            }
            M3ShapeToken.TRIANGLE -> {
                RoundedPolygon(3, rounding = CornerRounding(0.28f, 0.5f)).toPath()
            }
            M3ShapeToken.ARROW -> {
                val path = Path()
                path.moveTo(-0.9f, 0.7f)
                path.lineTo(0f, -0.9f)
                path.lineTo(0.9f, 0.7f)
                path.lineTo(0.5f, 0.7f)
                path.lineTo(0f, -0.2f)
                path.lineTo(-0.5f, 0.7f)
                path.close()
                path
            }
            M3ShapeToken.FAN -> {
                val path = Path()
                path.moveTo(-1f, 1f)
                path.lineTo(1f, 1f)
                val fanRect = RectF(-1f, -1f, 3f, 3f)
                path.arcTo(fanRect, 270f, -90f, false)
                path.close()
                path
            }
            M3ShapeToken.DIAMOND -> {
                RoundedPolygon(4, rounding = CornerRounding(0.35f, 0.5f)).toPath()
            }
            M3ShapeToken.CLAMSHELL -> {
                buildScallopPath(lobes = 5, innerRadius = 0.85f, angleSpan = 180f)
            }
            M3ShapeToken.PENTAGON -> {
                RoundedPolygon(5, rounding = CornerRounding(0.25f, 0.5f)).toPath()
            }
            M3ShapeToken.GEM -> {
                val vertices = floatArrayOf(
                    -0.6f, -1.0f,
                    0.6f, -1.0f,
                    1.0f, -0.2f,
                    0.0f, 1.0f,
                    -1.0f, -0.2f
                )
                RoundedPolygon(vertices, rounding = CornerRounding(0.18f, 0.5f)).toPath()
            }

            // Cookies & Sunbursts
            M3ShapeToken.VERY_SUNNY -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 16,
                    innerRadius = 0.76f,
                    rounding = CornerRounding(0.14f),
                    innerRounding = CornerRounding(0.14f)
                ).toPath()
            }
            M3ShapeToken.SUNNY -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 8,
                    innerRadius = 0.68f,
                    rounding = CornerRounding(0.22f),
                    innerRounding = CornerRounding(0.22f)
                ).toPath()
            }
            M3ShapeToken.COOKIE_4 -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 4,
                    innerRadius = 0.76f,
                    rounding = CornerRounding(0.38f),
                    innerRounding = CornerRounding(0.38f)
                ).toPath()
            }
            M3ShapeToken.COOKIE_6 -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 6,
                    innerRadius = 0.80f,
                    rounding = CornerRounding(0.28f),
                    innerRounding = CornerRounding(0.28f)
                ).toPath()
            }
            M3ShapeToken.COOKIE_7 -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 7,
                    innerRadius = 0.82f,
                    rounding = CornerRounding(0.25f),
                    innerRounding = CornerRounding(0.25f)
                ).toPath()
            }
            M3ShapeToken.COOKIE_9 -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 9,
                    innerRadius = 0.84f,
                    rounding = CornerRounding(0.22f),
                    innerRounding = CornerRounding(0.22f)
                ).toPath()
            }
            M3ShapeToken.COOKIE_12 -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 12,
                    innerRadius = 0.86f,
                    rounding = CornerRounding(0.18f),
                    innerRounding = CornerRounding(0.18f)
                ).toPath()
            }

            // Blooms & Bursts
            M3ShapeToken.CLOVER_4 -> {
                StudioCloverShape.build4LeafCloverPath(0f, 0f, 2f, 2f)
            }
            M3ShapeToken.CLOVER_8 -> {
                StudioCloverShape.build8LeafCloverPath(0f, 0f, 2f, 2f)
            }
            M3ShapeToken.BURST -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 12,
                    innerRadius = 0.44f,
                    rounding = CornerRounding(0.08f),
                    innerRounding = CornerRounding(0.08f)
                ).toPath()
            }
            M3ShapeToken.SOFT_BURST -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 10,
                    innerRadius = 0.58f,
                    rounding = CornerRounding(0.26f),
                    innerRounding = CornerRounding(0.26f)
                ).toPath()
            }
            M3ShapeToken.BOOM -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 14,
                    innerRadius = 0.38f,
                    rounding = CornerRounding(0.06f),
                    innerRounding = CornerRounding(0.06f)
                ).toPath()
            }
            M3ShapeToken.SOFT_BOOM -> {
                RoundedPolygon.star(
                    numVerticesPerRadius = 12,
                    innerRadius = 0.52f,
                    rounding = CornerRounding(0.22f),
                    innerRounding = CornerRounding(0.22f)
                ).toPath()
            }
            M3ShapeToken.FLOWER -> {
                buildCloverPath(lobes = 6)
            }
            M3ShapeToken.PUFFY -> {
                buildScallopPath(lobes = 8, innerRadius = 0.72f, angleSpan = 360f)
            }
            M3ShapeToken.PUFFY_DIAMOND -> {
                val path = Path()
                path.moveTo(0f, -1f)
                path.cubicTo(0.28f, -0.28f, 0.72f, -0.28f, 1f, 0f)
                path.cubicTo(0.72f, 0.28f, 0.28f, 0.72f, 0f, 1f)
                path.cubicTo(-0.28f, 0.72f, -0.72f, 0.28f, -1f, 0f)
                path.cubicTo(-0.72f, -0.28f, -0.28f, -0.28f, 0f, -1f)
                path.close()
                path
            }

            // Specialty Tokens
            M3ShapeToken.GHOSTISH -> {
                val path = Path()
                // Rounded dome head
                path.moveTo(-0.9f, 0.5f)
                path.lineTo(-0.9f, -0.1f)
                val domeRect = RectF(-0.9f, -1.0f, 0.9f, 0.8f)
                path.arcTo(domeRect, 180f, 180f, false)
                path.lineTo(0.9f, 0.5f)
                // Wavy bottom skirt (3 wave troughs)
                path.quadTo(0.6f, 0.95f, 0.3f, 0.5f)
                path.quadTo(0.0f, 0.95f, -0.3f, 0.5f)
                path.quadTo(-0.6f, 0.95f, -0.9f, 0.5f)
                path.close()
                path
            }
            M3ShapeToken.PIXEL_CIRCLE -> {
                buildPixelCirclePath()
            }
            M3ShapeToken.PIXEL_TRIANGLE -> {
                buildPixelTrianglePath()
            }
            M3ShapeToken.BUN -> {
                RoundedPolygon.pill(width = 1.9f, height = 1.15f).toPath()
            }
            M3ShapeToken.HEART -> {
                val path = Path()
                path.moveTo(0f, 0.75f)
                path.cubicTo(-0.85f, 0.3f, -1.0f, -0.4f, -0.5f, -0.75f)
                path.cubicTo(-0.2f, -0.95f, 0f, -0.6f, 0f, -0.35f)
                path.cubicTo(0f, -0.6f, 0.2f, -0.95f, 0.5f, -0.75f)
                path.cubicTo(1.0f, -0.4f, 0.85f, 0.3f, 0f, 0.75f)
                path.close()
                path
            }
        }
    }

    private fun buildCloverPath(lobes: Int): Path {
        val path = Path()
        val total = lobes * 2
        val step = (2.0 * PI / total).toFloat()
        // Explicit 8% safety margin (0.92f) prevents lobe truncation against outer bounding boxes
        val maxR = 0.92f
        val innerR = if (lobes == 4) 0.46f else 0.50f
        for (i in 0 until total) {
            val angle = i * step
            val r = if (i % 2 == 0) maxR else innerR
            val x = r * cos(angle)
            val y = r * sin(angle)
            if (i == 0) path.moveTo(x, y) else {
                val prevAngle = (i - 1) * step
                val prevR = if ((i - 1) % 2 == 0) maxR else innerR
                val midAngle = (prevAngle + angle) / 2f
                val midR = (prevR + r) / 1.65f
                path.quadTo(midR * cos(midAngle), midR * sin(midAngle), x, y)
            }
        }
        path.close()
        return path
    }

    private fun buildScallopPath(lobes: Int, innerRadius: Float, angleSpan: Float): Path {
        val path = Path()
        val total = lobes * 2
        val stepRad = (angleSpan * (PI / 180f) / total).toFloat()
        for (i in 0 until total) {
            val angle = i * stepRad
            val r = if (i % 2 == 0) 1.0f else innerRadius
            val x = r * cos(angle)
            val y = r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private fun buildPixelCirclePath(): Path {
        // 8-bit quantized circle steps
        val path = Path()
        val steps = listOf(
            RectF(-0.35f, -0.95f, 0.35f, -0.65f),
            RectF(-0.65f, -0.65f, 0.65f, -0.35f),
            RectF(-0.95f, -0.35f, 0.95f, 0.35f),
            RectF(-0.65f, 0.35f, 0.65f, 0.65f),
            RectF(-0.35f, 0.65f, 0.35f, 0.95f)
        )
        for (rect in steps) {
            path.addRect(rect, Path.Direction.CW)
        }
        return path
    }

    private fun buildPixelTrianglePath(): Path {
        val path = Path()
        val steps = listOf(
            RectF(-0.2f, -0.9f, 0.2f, -0.5f),
            RectF(-0.55f, -0.5f, 0.55f, 0.1f),
            RectF(-0.9f, 0.1f, 0.9f, 0.8f)
        )
        for (rect in steps) {
            path.addRect(rect, Path.Direction.CW)
        }
        return path
    }
}
