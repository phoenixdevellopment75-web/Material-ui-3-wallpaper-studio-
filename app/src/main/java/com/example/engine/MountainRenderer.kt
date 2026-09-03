package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.sin

/**
 * Clean Geometric Atmospheric Mountain Renderer.
 *
 * Implements:
 * 1. Monotonic atmospheric perspective ($L^*: 20\% \to 95\%$) fading up into off-white sky.
 * 2. Multi-octave cubic Bézier splines smoothed with Catmull-Rom math to eliminate jagged noise.
 * 3. Seam-free continuous mountain paths extending down to the very bottom edge of viewport (`height`).
 * 4. Crisp minimalist vector sun disc placed at the golden ratio with zero radial blur / halo noise.
 */
object MountainRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val rng = MathUtils.FastRandom(params.seed)

        // 1. Off-white to misty sky background gradient
        val skyTop = palette.getColorAt(0.96f).toArgb()
        val skyBottom = palette.getColorAt(0.82f).toArgb()

        val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.65f,
                skyTop, skyBottom,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, skyPaint)

        // 2. Minimalist Vector Sun (Golden Ratio Placement, Solid Crisp Disc or 2-Stop Linear Gradient)
        val showSun = params.subTypeIndex % 2 == 0 || params.subTypeIndex == 4
        if (showSun) {
            val sunX = width * 0.618034f // Golden ratio horizontal anchor
            val sunY = height * (0.22f + (rng.nextFloat() - 0.5f) * 0.08f)
            val sunRadius = width * 0.095f * params.scale.coerceIn(0.75f, 1.35f)

            val sunTopColor = palette.getColorAt(0.94f).toArgb()
            val sunBottomColor = palette.getColorAt(0.75f).toArgb()

            val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    sunX, sunY - sunRadius,
                    sunX, sunY + sunRadius,
                    sunTopColor,
                    sunBottomColor,
                    Shader.TileMode.CLAMP
                )
            }
            // Crisp vector disc with no noisy halo
            canvas.drawCircle(sunX, sunY, sunRadius, sunPaint)

            // Crisp hairline edge accent
            val sunRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                color = 0x40FFFFFF.toInt()
            }
            canvas.drawCircle(sunX, sunY, sunRadius, sunRimPaint)
        }

        // 3. Continuous 6-Layer Mountain Ridges (Back to Front)
        val totalLayers = 6
        val baseYSteps = floatArrayOf(0.38f, 0.48f, 0.58f, 0.68f, 0.78f, 0.88f)

        for (layer in 0 until totalLayers) {
            val depthFraction = layer.toFloat() / (totalLayers - 1) // 0.0 (furthest back) .. 1.0 (foreground)

            // Monotonic lightness: Layer 0 is light misty horizon, Layer 5 is deep rich obsidian/pine
            val paletteT = (0.88f - depthFraction * 0.78f).coerceIn(0.06f, 0.94f)
            val layerSolidColor = palette.getColorAt(paletteT).toArgb()
            val layerBaseColor = palette.getColorAt((paletteT - 0.08f).coerceAtLeast(0.04f)).toArgb()

            val baseY = height * baseYSteps[layer]
            val layerPath = buildSmoothRidgePath(
                width = width,
                height = height,
                baseY = baseY,
                layerIndex = layer,
                params = params
            )

            // Fill mountain path with vertical gradient to give depth without horizontal seams
            val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f, baseY - height * 0.15f,
                    0f, height,
                    layerSolidColor,
                    layerBaseColor,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(layerPath, mountainPaint)

            // Optional wireframe outline or tactile ridge stroke
            if (params.isWireframe) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth * (1.2f - depthFraction * 0.3f)
                    color = 0xCCFFFFFF.toInt()
                }
                canvas.drawPath(layerPath, strokePaint)
            }
        }
    }

    /**
     * Constructs a smooth, continuous mountain ridge path from x=0 to x=width,
     * cleanly closed at the bottom (y=height) so no horizontal seam lines appear.
     */
    private fun buildSmoothRidgePath(
        width: Float,
        height: Float,
        baseY: Float,
        layerIndex: Int,
        params: WallpaperParams
    ): Path {
        val path = Path()

        val seed = params.seed + layerIndex * 1987L
        val phi1 = ((seed % 1000) * 0.012f)
        val phi2 = (((seed / 1000) % 1000) * 0.019f)
        val phi3 = (((seed / 1000000) % 1000) * 0.027f)

        // Generate smooth Catmull-Rom control points across the width
        val numSegments = 12 + layerIndex * 2
        val controlPoints = mutableListOf<FloatArray>()

        val omega1 = (1.8f + layerIndex * 0.35f) * params.scale / width * (2f * PI.toFloat())
        val omega2 = omega1 * 2.15f
        val omega3 = omega1 * 4.35f
        val amplitude = height * (0.075f - layerIndex * 0.005f) * params.distortion.coerceIn(0.3f, 2.0f)

        // Generate points with margin on left and right for smooth spline tangents
        for (i in -1..numSegments + 1) {
            val x = (i.toFloat() / numSegments) * width
            val w1 = sin(x * omega1 + phi1)
            val w2 = sin(x * omega2 + phi2) * 0.38f
            val w3 = sin(x * omega3 + phi3) * 0.16f
            val y = baseY + (w1 + w2 + w3) * amplitude
            controlPoints.add(floatArrayOf(x, y))
        }

        // Trace path using Catmull-Rom to cubic Bézier spline interpolation
        val startPt = controlPoints[1]
        path.moveTo(startPt[0].coerceAtLeast(0f), startPt[1])

        for (i in 1 until controlPoints.size - 2) {
            val p0 = controlPoints[i - 1]
            val p1 = controlPoints[i]
            val p2 = controlPoints[i + 1]
            val p3 = controlPoints[i + 2]

            // Catmull-Rom tangent vectors converted to cubic Bézier control handles
            val c1x = p1[0] + (p2[0] - p0[0]) / 6f
            val c1y = p1[1] + (p2[1] - p0[1]) / 6f
            val c2x = p2[0] - (p3[0] - p1[0]) / 6f
            val c2y = p2[1] - (p3[1] - p1[1]) / 6f

            path.cubicTo(c1x, c1y, c2x, c2y, p2[0], p2[1])
        }

        // Strictly close path to the bottom corners of the canvas
        // This guarantees NO horizontal seam lines can ever cut across background layers!
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        return path
    }
}
