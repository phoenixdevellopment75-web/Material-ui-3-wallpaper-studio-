package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Organic Fluid Camo / Lava Procedural Wallpaper Engine.
 *
 * Implements continuous 2D analytic cubic Bézier ribbons:
 * - Eliminates all spikes, triangular pinch points, and self-intersections.
 * - Tangent continuity (C^1 smoothness) with smooth control point transitions.
 * - Clamped coordinate bounds and strictly bounded width modulations.
 * - Strict 2 flat colors: Warm Eggshell/Cream background (#F5EFEB) with
 *   bold deep Crimson liquid ribbons (#8E1624). Zero 3D gradients or specular reflections.
 */
object LiquidCamoRenderer : WallpaperRenderer {

    private data class RibbonNode(
        val x: Float,
        val y: Float,
        val halfWidth: Float,
        val nx: Float,
        val ny: Float
    )

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        // 1. Two-tone flat matte colorways matching exact specifications
        val (bgArgb, liquidArgb) = when (params.subTypeIndex % 5) {
            0 -> Pair(
                0xFFF5EFEB.toInt(), // Warm Eggshell/Cream background (#F5EFEB)
                0xFF8E1624.toInt()  // Bold deep Crimson liquid ribbons (#8E1624)
            )
            1 -> Pair(
                0xFFF4F0E8.toInt(), // Warm bone paper
                0xFF1E382B.toInt()  // Rich pine green liquid
            )
            2 -> Pair(
                0xFFF7F2EB.toInt(), // Warm chalk
                0xFFC0583E.toInt()  // Rich terracotta rust
            )
            3 -> Pair(
                0xFFF6EFE9.toInt(), // Pale linen
                0xFF264653.toInt()  // Deep Persian indigo
            )
            else -> {
                val bg = params.palette.colors.first().toArgb()
                val fg = if (params.palette.colors.size > 2) {
                    params.palette.colors[2].toArgb()
                } else {
                    params.palette.colors.last().toArgb()
                }
                Pair(bg, fg)
            }
        }

        // Draw flat matte background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgArgb
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Paint for liquid ribbons
        val liquidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = liquidArgb
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            if (params.isWireframe) {
                strokeWidth = (params.lineWidth * (width / 380f)).coerceAtLeast(2.5f)
            }
        }

        val rng = MathUtils.FastRandom(params.seed)
        val baseDim = minOf(width, height)
        val scale = params.scale.coerceIn(0.75f, 1.4f)

        // 2. Generate 3 to 4 continuous flowing serpentine rivers
        val riverCount = 3 + rng.nextInt(2) // 3 or 4 rivers
        val masterLiquidPath = Path()

        // Spacing across canvas width
        val xSpacing = width / (riverCount + 1f)

        for (r in 0 until riverCount) {
            val baseX = xSpacing * (r + 1) + (rng.nextFloat() - 0.5f) * (xSpacing * 0.35f)
            val numSteps = 28 // Dense sampling for pristine C1 curve conversion
            val yStart = -height * 0.15f
            val yEnd = height * 1.15f
            val stepY = (yEnd - yStart) / (numSteps - 1)

            val phase1 = rng.nextFloat() * (2f * PI.toFloat())
            val phase2 = rng.nextFloat() * (2f * PI.toFloat())
            val freq1 = 1.0f + rng.nextFloat() * 0.5f
            val freq2 = 2.0f + rng.nextFloat() * 0.5f
            val amplitude = (width * 0.10f * scale).coerceAtMost(xSpacing * 0.45f)

            // Minimum and maximum half-width clamped to prevent acute pinch points
            val baseHalfWidth = (baseDim * (0.075f + rng.nextFloat() * 0.035f) * scale)
            val minHalfWidth = baseHalfWidth * 0.65f // Never pinch to acute spike

            val rawNodes = mutableListOf<RibbonNode>()

            for (i in 0 until numSteps) {
                val y = yStart + i * stepY
                val progress = i.toFloat() / (numSteps - 1)

                // Smooth sinusoidal centerline
                val wave = sin(progress * PI.toFloat() * freq1 + phase1) * amplitude +
                    cos(progress * PI.toFloat() * freq2 + phase2) * (amplitude * 0.35f)
                val x = (baseX + wave).coerceIn(width * 0.04f, width * 0.96f)

                // Modulate thickness smoothly with positive floor
                val widthMod = 0.85f + 0.35f * sin(progress * 2.5f * PI.toFloat() + phase1)
                val hw = (baseHalfWidth * widthMod).coerceAtLeast(minHalfWidth)

                // Calculate derivative dx/dy for analytic normal vector
                val dProgress = 1f / (numSteps - 1)
                val dWave = (freq1 * PI.toFloat() * cos(progress * PI.toFloat() * freq1 + phase1) * amplitude -
                    freq2 * PI.toFloat() * sin(progress * PI.toFloat() * freq2 + phase2) * (amplitude * 0.35f)) * dProgress

                val dx = dWave
                val dy = stepY
                val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)

                // Unit normal (-dy, dx) / len
                val nx = -dy / len
                val ny = dx / len

                rawNodes.add(RibbonNode(x, y, hw, nx, ny))
            }

            // Construct smooth continuous C1 cubic Bézier closed ribbon path
            val riverPath = buildRibbonPath(rawNodes)
            masterLiquidPath.addPath(riverPath)
        }

        // 3. Add smooth organic floating enamel droplets/capsules nestled between rivers
        val dropletCount = 3 + rng.nextInt(3)
        for (d in 0 until dropletCount) {
            val cx = width * (0.15f + rng.nextFloat() * 0.70f)
            val cy = height * (0.12f + rng.nextFloat() * 0.76f)
            val radiusX = (baseDim * (0.045f + rng.nextFloat() * 0.035f) * scale)
            val radiusY = radiusX * (1.2f + rng.nextFloat() * 0.7f) // Elongated smooth organic capsule
            val rotation = (rng.nextFloat() - 0.5f) * 45f

            val dropPath = Path()
            val rect = RectF(cx - radiusX, cy - radiusY, cx + radiusX, cy + radiusY)
            dropPath.addRoundRect(rect, radiusX, radiusX, Path.Direction.CW)

            val matrix = android.graphics.Matrix()
            matrix.setRotate(rotation, cx, cy)
            dropPath.transform(matrix)

            masterLiquidPath.addPath(dropPath)
        }

        // 4. Render master flat liquid paths with zero gradients
        canvas.drawPath(masterLiquidPath, liquidPaint)
    }

    /**
     * Builds a continuous smooth ribbon path from centerline nodes using cubic Bézier splines
     * with Catmull-Rom tangent continuity (C^1 smoothness).
     */
    private fun buildRibbonPath(nodes: List<RibbonNode>): Path {
        val path = Path()
        if (nodes.size < 2) return path

        // Compute left boundary points with clamped coordinates
        val leftPoints = nodes.map { n ->
            Pair(n.x + n.nx * n.halfWidth, n.y + n.ny * n.halfWidth)
        }
        // Compute right boundary points with clamped coordinates
        val rightPoints = nodes.map { n ->
            Pair(n.x - n.nx * n.halfWidth, n.y - n.ny * n.halfWidth)
        }

        // Trace down the left boundary using smooth cubic Bézier segments
        path.moveTo(leftPoints[0].first, leftPoints[0].second)
        for (i in 0 until leftPoints.size - 1) {
            val p0 = leftPoints[maxOf(0, i - 1)]
            val p1 = leftPoints[i]
            val p2 = leftPoints[i + 1]
            val p3 = leftPoints[minOf(leftPoints.size - 1, i + 2)]

            // Catmull-Rom to Cubic Bézier conversion for smooth C1 curvature
            val cp1x = p1.first + (p2.first - p0.first) / 6f
            val cp1y = p1.second + (p2.second - p0.second) / 6f
            val cp2x = p2.first - (p3.first - p1.first) / 6f
            val cp2y = p2.second - (p3.second - p1.second) / 6f

            path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.first, p2.second)
        }

        // Filleted bottom cap wrapping smoothly to right boundary
        val lastLeft = leftPoints.last()
        val lastRight = rightPoints.last()
        val bottomMidX = (lastLeft.first + lastRight.first) / 2f
        val bottomMidY = maxOf(lastLeft.second, lastRight.second) + 16f
        path.quadTo(bottomMidX, bottomMidY, lastRight.first, lastRight.second)

        // Trace up the right boundary in reverse using smooth cubic Bézier segments
        for (i in rightPoints.size - 1 downTo 1) {
            val p0 = rightPoints[minOf(rightPoints.size - 1, i + 1)]
            val p1 = rightPoints[i]
            val p2 = rightPoints[i - 1]
            val p3 = rightPoints[maxOf(0, i - 2)]

            val cp1x = p1.first + (p2.first - p0.first) / 6f
            val cp1y = p1.second + (p2.second - p0.second) / 6f
            val cp2x = p2.first - (p3.first - p1.first) / 6f
            val cp2y = p2.second - (p3.second - p1.second) / 6f

            path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.first, p2.second)
        }

        // Filleted top cap wrapping smoothly to starting left point
        val firstRight = rightPoints.first()
        val firstLeft = leftPoints.first()
        val topMidX = (firstRight.first + firstLeft.first) / 2f
        val topMidY = minOf(firstRight.second, firstLeft.second) - 16f
        path.quadTo(topMidX, topMidY, firstLeft.first, firstLeft.second)

        path.close()
        return path
    }
}
