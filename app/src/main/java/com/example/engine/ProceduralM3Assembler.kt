package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Shape Vector Engine & Procedural Assembler.
 *
 * Implements the official Material 3 Expressive shape token library:
 * 1. 4-Leaf & 8-Leaf Clover (tangent filleted lobes)
 * 2. M3 Scallop (6-sided, 8-sided, and 12-sided cookie badges via clamped polar harmonics)
 * 3. M3 Arch & Semicircle (pure tangent rounded caps with vertical drop walls)
 * 4. Puffy Diamond & Gem (filleted convex/concave polygonal edges)
 * 5. Bun & Stadium Pill (smooth, unwarped elongated curves)
 * 6. Slanted Squircle & Pebble (organic asymmetrical continuous curvature)
 * 7. Circle & Torus Ring
 *
 * Enforces uniform 1:1 aspect ratio scaling to prevent shape warping/squeezing.
 */
object ProceduralM3Assembler {

    private const val PHI_1 = 0.381966f
    private const val PHI_2 = 0.618034f
    private const val PHI_3 = 0.236068f
    private const val PHI_4 = 0.763932f

    /**
     * Renders user-composed shapes on the Studio canvas with 1:1 proportional scaling.
     */
    fun renderCustomShapes(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val bgArgb = palette.colors.firstOrNull()?.toArgb() ?: 0xFF1C1B1F.toInt()
        val bgSecond = palette.colors.getOrNull(1)?.toArgb() ?: bgArgb

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgArgb, bgSecond,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        val colors = palette.colors.map { it.toArgb() }
        val sortedShapes = params.customShapes.sortedBy { it.zIndex }

        // Uniform 1:1 base reference dimension so shapes never stretch on tall screens
        val baseDim = minOf(width, height)

        for (shape in sortedShapes) {
            val cx = shape.normalizedX * width
            val cy = shape.normalizedY * height

            val scaleFactor = params.scale * baseDim
            val w = shape.normalizedWidth * scaleFactor
            val h = if (shape.type.isProportional1to1) {
                shape.normalizedWidth * scaleFactor // Force strict 1:1 aspect ratio!
            } else {
                shape.normalizedHeight * scaleFactor
            }

            val color = if (shape.customColorHex != null) {
                shape.customColorHex.toInt()
            } else {
                colors[shape.colorIndex % colors.size]
            }

            drawSingleShape(
                canvas = canvas,
                type = shape.type,
                cx = cx,
                cy = cy,
                w = w,
                h = h,
                rotationDeg = shape.rotationDeg + params.rotationDegrees,
                color = color,
                opacity = shape.opacity,
                isWireframe = shape.isWireframe || params.isWireframe,
                strokeWidth = shape.strokeWidth * (baseDim / 500f),
                scallopLobes = shape.scallopLobes,
                castShadow = true
            )
        }
    }

    /**
     * Renders a single tactile M3 shape with shadow, fill, or outline.
     */
    fun drawSingleShape(
        canvas: Canvas,
        type: CustomShapeType,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        rotationDeg: Float,
        color: Int,
        opacity: Float,
        isWireframe: Boolean,
        strokeWidth: Float,
        scallopLobes: Int = 8,
        castShadow: Boolean = true
    ) {
        val path = createShapePath(type, cx, cy, w, h, scallopLobes)

        canvas.save()
        if (rotationDeg != 0f) {
            canvas.rotate(rotationDeg, cx, cy)
        }

        // Elevation Drop Shadow
        if (castShadow && !isWireframe) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = AndroidColor.TRANSPARENT
                setShadowLayer(
                    28f,
                    0f,
                    14f,
                    AndroidColor.argb((0.35f * opacity * 255).toInt(), 0, 0, 0)
                )
            }
            canvas.drawPath(path, shadowPaint)
        }

        // Primary Surface Fill / Outline
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val alpha = (opacity * 255).toInt().coerceIn(0, 255)
            this.color = (color and 0x00FFFFFF) or (alpha shl 24)
            if (isWireframe) {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth.coerceAtLeast(1.5f)
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            } else {
                style = Paint.Style.FILL
            }
        }
        canvas.drawPath(path, paint)

        // Highlight inner border for crisp tactile feel
        if (!isWireframe && opacity > 0.3f) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = 1.2f
                this.color = AndroidColor.argb(35, 255, 255, 255)
            }
            canvas.drawPath(path, borderPaint)
        }

        canvas.restore()
    }

    /**
     * Constructs the parametric vector path for the official Material 3 Expressive shapes.
     */
    fun createShapePath(
        type: CustomShapeType,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        scallopLobes: Int = 8
    ): Path {
        val path = Path()
        val halfW = w / 2f
        val halfH = h / 2f
        val left = cx - halfW
        val top = cy - halfH
        val right = cx + halfW
        val bottom = cy + halfH

        when (type) {
            CustomShapeType.CLOVER_4 -> {
                // Official 4-Leaf Clover: Unified single compound vector path with zero internal wireframes
                return StudioCloverShape.build4LeafCloverPath(cx, cy, w, h)
            }

            CustomShapeType.CLOVER_8 -> {
                // Official 8-Leaf Clover Bloom: Unified single compound vector path with zero internal wireframes
                return StudioCloverShape.build8LeafCloverPath(cx, cy, w, h)
            }

            CustomShapeType.SUNNY_BADGE -> {
                // Sunny Badge: 16-point sunburst star cookie badge
                val radius = minOf(halfW, halfH)
                val innerR = radius * 0.78f
                val points = 32
                for (i in 0 until points) {
                    val angle = (i * (360f / points)) * (PI.toFloat() / 180f)
                    val r = if (i % 2 == 0) radius else innerR
                    val px = cx + r * cos(angle)
                    val py = cy + r * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }

            CustomShapeType.COOKIE -> {
                // Material 3 Flower Cookie: 10-scallop cookie badge
                buildScallopPath(path, cx, cy, minOf(halfW, halfH), lobes = 10)
            }

            CustomShapeType.SCALLOP_6 -> {
                // 6-sided cookie badge: r(θ) = R * (1 + 0.12 * cos(6θ))
                buildScallopPath(path, cx, cy, minOf(halfW, halfH), lobes = 6)
            }

            CustomShapeType.SCALLOP_8 -> {
                // 8-sided cookie badge: r(θ) = R * (1 + 0.12 * cos(8θ))
                buildScallopPath(path, cx, cy, minOf(halfW, halfH), lobes = 8)
            }

            CustomShapeType.SCALLOP_12 -> {
                // 12-sided cookie badge: r(θ) = R * (1 + 0.12 * cos(12θ))
                buildScallopPath(path, cx, cy, minOf(halfW, halfH), lobes = 12)
            }

            CustomShapeType.M3_ARCH -> {
                // Pure tangent rounded arch with vertical drop walls
                val archRadius = halfW
                val archTopRect = RectF(left, top, right, top + archRadius * 2f)
                path.moveTo(left, bottom)
                path.lineTo(left, top + archRadius)
                path.arcTo(archTopRect, 180f, 180f, false)
                path.lineTo(right, bottom)
                path.close()
            }

            CustomShapeType.SEMICIRCLE -> {
                // Pure tangent half-circle dome with flat base
                val domeRect = RectF(left, top, right, bottom + halfH)
                path.moveTo(left, bottom)
                path.arcTo(domeRect, 180f, 180f, false)
                path.lineTo(right, bottom)
                path.close()
            }

            CustomShapeType.PUFFY_DIAMOND -> {
                // Puffy curved diamond with rounded vertices
                val bulge = 0.22f
                path.moveTo(cx, top)
                path.cubicTo(cx + halfW * bulge, top, right, cy - halfH * bulge, right, cy)
                path.cubicTo(right, cy + halfH * bulge, cx + halfW * bulge, bottom, cx, bottom)
                path.cubicTo(cx - halfW * bulge, bottom, left, cy + halfH * bulge, left, cy)
                path.cubicTo(left, cy - halfH * bulge, cx - halfW * bulge, top, cx, top)
                path.close()
            }

            CustomShapeType.GEM -> {
                // Filleted faceted gem (octagonal gem token with filleted corners)
                val chamfer = minOf(halfW, halfH) * 0.32f
                path.moveTo(left + chamfer, top)
                path.lineTo(right - chamfer, top)
                path.lineTo(right, top + chamfer)
                path.lineTo(right, bottom - chamfer)
                path.lineTo(right - chamfer, bottom)
                path.lineTo(left + chamfer, bottom)
                path.lineTo(left, bottom - chamfer)
                path.lineTo(left, top + chamfer)
                path.close()
            }

            CustomShapeType.BUN -> {
                // Material 3 Bun: smooth oblong rounded bun
                val r = minOf(halfW, halfH) * 0.82f
                val rect = RectF(left, top, right, bottom)
                path.addRoundRect(rect, r, r * 0.9f, Path.Direction.CW)
            }

            CustomShapeType.STADIUM_PILL -> {
                // Classic unwarped stadium pill
                val radius = minOf(halfW, halfH)
                val rect = RectF(left, top, right, bottom)
                path.addRoundRect(rect, radius, radius, Path.Direction.CW)
            }

            CustomShapeType.SLANTED_SQUIRCLE -> {
                // Slanted 45-degree squircle with soft continuous curvature
                val r = minOf(halfW, halfH)
                val k = r * 0.55f
                path.moveTo(cx, top)
                path.cubicTo(cx + k, top, right, cy - k, right, cy)
                path.cubicTo(right, cy + k, cx + k, bottom, cx, bottom)
                path.cubicTo(cx - k, bottom, left, cy + k, left, cy)
                path.cubicTo(left, cy - k, cx - k, top, cx, top)
                path.close()
            }

            CustomShapeType.PEBBLE -> {
                // Organic asymmetrical continuous curvature
                val r1 = halfW * 0.95f
                val r2 = halfH * 0.70f
                val r3 = halfW * 0.75f
                val r4 = halfH * 0.92f

                path.moveTo(cx, top)
                path.cubicTo(right - (r1 * 0.2f), top, right, cy - (r2 * 0.2f), right, cy)
                path.cubicTo(right, bottom - (r3 * 0.3f), cx + (r3 * 0.3f), bottom, cx, bottom)
                path.cubicTo(left + (r4 * 0.2f), bottom, left, cy + (r4 * 0.3f), left, cy)
                path.cubicTo(left, top + (r1 * 0.2f), cx - (r1 * 0.3f), top, cx, top)
                path.close()
            }

            CustomShapeType.TORUS_RING -> {
                // Concentric circular ring token
                val outerRadius = minOf(halfW, halfH)
                val innerRadius = outerRadius * 0.58f
                path.addCircle(cx, cy, outerRadius, Path.Direction.CW)
                path.addCircle(cx, cy, innerRadius, Path.Direction.CCW)
            }
        }
        return path
    }

    private fun buildScallopPath(path: Path, cx: Float, cy: Float, radius: Float, lobes: Int) {
        val amplitude = radius * 0.12f
        val points = 360
        var first = true

        for (deg in 0 until points) {
            val theta = Math.toRadians(deg.toDouble()).toFloat()
            val r = radius + amplitude * cos(lobes * theta)
            val px = cx + r * cos(theta)
            val py = cy + r * sin(theta)

            if (first) {
                path.moveTo(px, py)
                first = false
            } else {
                path.lineTo(px, py)
            }
        }
        path.close()
    }
}
