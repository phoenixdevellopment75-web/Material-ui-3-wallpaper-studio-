package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.util.Random

/**
 * 4. Organic M3 Scallops & Badges Renderer
 *
 * Implements pure smooth organic curvature strictly free of sharp starbursts, sawtooth angles,
 * rigid bullseyes, and plain outline rings.
 *
 * Geometric elements:
 * - Smooth scalloped badges: Polar harmonic curves r(θ) = R0 + A * cos(kθ) where k ∈ [6, 12]
 *   with smooth continuous curvature and filleted transitions.
 * - Smooth organic pebbles with asymmetric soft radii.
 * - Elongated vertical stadium pills.
 * - Multi-pass elevation drop shadows via Paint.setShadowLayer().
 * - Rule-of-thirds asymmetric compositions with generous negative space.
 */
object ScallopBadgeRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val rng = Random(params.seed)
        val colors = params.palette.colors

        // 1. Clean M3 Canvas Surface Background
        val bgArgb = colors.first().toArgb()
        val bgEndArgb = colors[1.coerceAtMost(colors.size - 1)].toArgb()
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, height, bgArgb, bgEndArgb, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Dispatch by sub-type
        when (params.subTypeIndex % 5) {
            0 -> renderAsymmetricScallopFlower(canvas, width, height, params, rng)
            1 -> renderRuleOfThirdsTriad(canvas, width, height, params, rng)
            2 -> renderOrganicPebbleZen(canvas, width, height, params, rng)
            3 -> renderDescendingPillScallopDuet(canvas, width, height, params, rng)
            4 -> renderModernistLayeredBadges(canvas, width, height, params, rng)
        }
    }

    /**
     * Subtype 0: Lower-left floating hero scalloped flower badge paired with an upper-right descending pill
     * and soft background elevation drift with generous negative space.
     */
    private fun renderAsymmetricScallopFlower(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        // Soft background ambient pill drift at top-right
        val driftX = width * 0.76f
        val driftY = height * 0.24f
        val driftW = width * 0.28f * params.scale
        val driftH = height * 0.32f * params.scale
        drawStadiumPillWithShadow(
            canvas, driftX, driftY, driftW, driftH,
            color = params.palette.getColorAt(0.30f).toArgb(),
            params = params
        )

        // Lower-left floating hero 10-petal scalloped flower
        val flowerX = width * 0.38f
        val flowerY = height * 0.64f
        val heroRadius = width * 0.38f * params.scale
        drawScallopedBadgeWithShadow(
            canvas, flowerX, flowerY, heroRadius,
            lobes = 10,
            depth = 0.12f * params.distortion.coerceIn(0.6f, 1.4f),
            color = params.palette.getColorAt(0.55f).toArgb(),
            params = params
        )

        // Inner nested 6-petal smooth scallop core
        val innerRadius = heroRadius * 0.55f
        drawScallopedBadgeWithShadow(
            canvas, flowerX, flowerY, innerRadius,
            lobes = 6,
            depth = 0.10f * params.distortion.coerceIn(0.6f, 1.4f),
            color = params.palette.getColorAt(0.85f).toArgb(),
            params = params
        )

        // Soft organic floating pebble at bottom-right
        val pebX = width * 0.78f
        val pebY = height * 0.82f
        val pebSize = width * 0.22f * params.scale
        drawPebbleWithShadow(
            canvas, pebX, pebY, pebSize,
            rotationDeg = 25f,
            color = params.palette.getColorAt(0.95f).toArgb(),
            params = params
        )
    }

    /**
     * Subtype 1: Three overlapping asymmetric M3 shapes placed with intentional negative space
     * (Top-Left 8-Lobe Scallop, Center-Right Vertical Pill Capsule, Bottom-Center Pebble)
     */
    private fun renderRuleOfThirdsTriad(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        // Shape 1: Top-Left 8-Lobe Scallop
        val s1X = width * 0.34f
        val s1Y = height * 0.28f
        val s1R = width * 0.30f * params.scale
        drawScallopedBadgeWithShadow(
            canvas, s1X, s1Y, s1R,
            lobes = 8,
            depth = 0.11f * params.distortion.coerceIn(0.6f, 1.4f),
            color = params.palette.getColorAt(0.35f).toArgb(),
            params = params
        )

        // Shape 2: Center-Right Vertical Pill Capsule
        val s2X = width * 0.68f
        val s2Y = height * 0.48f
        val s2W = width * 0.28f * params.scale
        val s2H = height * 0.36f * params.scale
        drawStadiumPillWithShadow(
            canvas, s2X, s2Y, s2W, s2H,
            color = params.palette.getColorAt(0.65f).toArgb(),
            params = params
        )

        // Shape 3: Bottom-Left 12-lobe organic scallop rosette
        val s3X = width * 0.38f
        val s3Y = height * 0.74f
        val s3R = width * 0.28f * params.scale
        drawScallopedBadgeWithShadow(
            canvas, s3X, s3Y, s3R,
            lobes = 12,
            depth = 0.09f * params.distortion.coerceIn(0.6f, 1.4f),
            color = params.palette.getColorAt(0.90f).toArgb(),
            params = params
        )
    }

    /**
     * Subtype 2: Smooth organic pebbles arranged in a zen composition with elevation depth
     */
    private fun renderOrganicPebbleZen(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val pebbleCount = 4
        val positions = listOf(
            Triple(width * 0.36f, height * 0.28f, width * 0.32f),
            Triple(width * 0.68f, height * 0.44f, width * 0.34f),
            Triple(width * 0.34f, height * 0.66f, width * 0.30f),
            Triple(width * 0.70f, height * 0.82f, width * 0.24f)
        )

        for (i in 0 until pebbleCount) {
            val (px, py, baseSize) = positions[i]
            val size = baseSize * params.scale
            val rot = (i * 40f) + 15f
            val color = params.palette.getColorAt(0.20f + (i.toFloat() / pebbleCount) * 0.75f).toArgb()

            drawPebbleWithShadow(canvas, px, py, size, rot, color, params)
        }
    }

    /**
     * Subtype 3: Descending elongated stadium pills with intersecting smooth 8-lobe scallop badge
     */
    private fun renderDescendingPillScallopDuet(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        // Left descending tall pill
        val p1X = width * 0.32f
        val p1Y = height * 0.40f
        val p1W = width * 0.24f * params.scale
        val p1H = height * 0.44f * params.scale
        drawStadiumPillWithShadow(
            canvas, p1X, p1Y, p1W, p1H,
            color = params.palette.getColorAt(0.32f).toArgb(),
            params = params
        )

        // Right offset tall pill
        val p2X = width * 0.68f
        val p2Y = height * 0.60f
        val p2W = width * 0.26f * params.scale
        val p2H = height * 0.40f * params.scale
        drawStadiumPillWithShadow(
            canvas, p2X, p2Y, p2W, p2H,
            color = params.palette.getColorAt(0.60f).toArgb(),
            params = params
        )

        // Center intersecting 8-lobe scallop
        val sX = width * 0.50f
        val sY = height * 0.50f
        val sR = width * 0.26f * params.scale
        drawScallopedBadgeWithShadow(
            canvas, sX, sY, sR,
            lobes = 8,
            depth = 0.12f,
            color = params.palette.getColorAt(0.88f).toArgb(),
            params = params
        )
    }

    /**
     * Subtype 4: Modernist composition of 3 layered organic badges with elevation depth
     */
    private fun renderModernistLayeredBadges(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val cx = width * 0.5f
        val cy = height * 0.48f

        // Bottom backdrop stadium pill
        val pillW = width * 0.56f * params.scale
        val pillH = height * 0.42f * params.scale
        drawStadiumPillWithShadow(
            canvas, cx, cy, pillW, pillH,
            color = params.palette.getColorAt(0.28f).toArgb(),
            params = params
        )

        // Middle 10-point smooth scalloped badge
        val scallopR = width * 0.32f * params.scale
        drawScallopedBadgeWithShadow(
            canvas, cx, cy - height * 0.04f, scallopR,
            lobes = 10,
            depth = 0.10f,
            color = params.palette.getColorAt(0.65f).toArgb(),
            params = params
        )

        // Top inner 6-point smooth scalloped badge
        drawScallopedBadgeWithShadow(
            canvas, cx, cy - height * 0.04f, scallopR * 0.52f,
            lobes = 6,
            depth = 0.09f,
            color = params.palette.getColorAt(0.95f).toArgb(),
            params = params
        )
    }

    private fun drawScallopedBadgeWithShadow(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        lobes: Int,
        depth: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val clampedLobes = lobes.coerceIn(6, 12)
        val clampedDepth = depth.coerceIn(0.06f, 0.16f)
        val path = buildScallopedPath(cx, cy, radius, clampedLobes, clampedDepth)

        if (!params.isWireframe) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = 0x22000000
                setShadowLayer(24f * params.scale, 0f, 8f, 0x38000000)
            }
            canvas.drawPath(path, shadowPaint)
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }
        canvas.drawPath(path, fillPaint)

        if (!params.isWireframe) {
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth.coerceAtMost(1.5f)
                this.color = 0x24FFFFFF
            }
            canvas.drawPath(path, border)
        }
    }

    private fun drawStadiumPillWithShadow(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        width: Float,
        height: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val left = cx - width * 0.5f
        val top = cy - height * 0.5f
        val right = cx + width * 0.5f
        val bottom = cy + height * 0.5f
        val rect = RectF(left, top, right, bottom)
        val cornerRadius = width * 0.5f

        if (!params.isWireframe) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = 0x20000000
                setShadowLayer(22f * params.scale, 0f, 8f, 0x34000000)
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint)
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)

        if (!params.isWireframe) {
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth.coerceAtMost(1.5f)
                this.color = 0x20FFFFFF
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, border)
        }
    }

    private fun drawPebbleWithShadow(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        rotationDeg: Float,
        color: Int,
        params: WallpaperParams
    ) {
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(rotationDeg)

        val path = buildPebblePath(size)

        if (!params.isWireframe) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = 0x20000000
                setShadowLayer(20f * params.scale, 0f, 6f, 0x30000000)
            }
            canvas.drawPath(path, shadowPaint)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }
        canvas.drawPath(path, paint)

        if (!params.isWireframe) {
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth.coerceAtMost(1.5f)
                this.color = 0x22FFFFFF
            }
            canvas.drawPath(path, border)
        }

        canvas.restore()
    }

    /**
     * Constructs a smooth vector path with polar radius r(θ) = R0 + A * cos(kθ)
     * with clamped peak curvature and filleted transitions.
     */
    private fun buildScallopedPath(
        cx: Float,
        cy: Float,
        radius: Float,
        lobes: Int,
        depth: Float
    ): Path {
        val path = Path()
        val steps = lobes * 24

        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * (2f * PI.toFloat())
            val r = radius * (1f + cos(angle * lobes) * depth)
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private fun buildPebblePath(size: Float): Path {
        val path = Path()
        val rect = RectF(-size * 0.6f, -size * 0.4f, size * 0.6f, size * 0.4f)
        path.addRoundRect(
            rect,
            floatArrayOf(
                size * 0.45f, size * 0.45f,
                size * 0.58f, size * 0.58f,
                size * 0.38f, size * 0.38f,
                size * 0.50f, size * 0.50f
            ),
            Path.Direction.CW
        )
        return path
    }
}
