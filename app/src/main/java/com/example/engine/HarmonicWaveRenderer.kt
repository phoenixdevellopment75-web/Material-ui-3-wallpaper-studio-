package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2. Harmonic S-Curve Waves / Dune Contours Renderer
 *
 * Smooth, vertical flowing S-curves calculated using continuous cubic Bézier splines:
 * B(t) = (1-t)^3 P0 + 3(1-t)^2 t P1 + 3(1-t) t^2 P2 + t^3 P3
 * Flowing vertical gradient bands (Deep Forest Green, Jade, Sage, Pale Celadon, Muted Cream).
 * Tangent curvature guarantees zero sharp corners or pinch points.
 */
object HarmonicWaveRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val rng = MathUtils.FastRandom(params.seed)

        // 1. Clean Base Canvas Fill
        val baseBg = palette.getColorAt(0.05f).toArgb()
        canvas.drawColor(baseBg)

        // 2. Determine number of vertical S-curve bands
        val bandCount = when (params.subTypeIndex % 5) {
            3 -> 3 // Minimalist Duo/Trio Stream
            else -> (5 * params.complexity).toInt().coerceIn(4, 7)
        }

        val stepSamples = 120
        val boundarySplines = mutableListOf<List<PointF>>()

        // Calculate bounded continuous cubic Bézier boundaries
        val baseSpacing = width / (bandCount + 1)
        val maxWaveAmp = (baseSpacing * 0.65f * params.distortion).coerceAtMost(baseSpacing * 0.85f)

        val seedFloat = (params.seed % 10000) * 0.005f

        for (b in 0..bandCount + 1) {
            val anchorX = b * baseSpacing
            val phaseOffset = seedFloat + b * 0.85f

            val p0X = anchorX + sin(phaseOffset) * maxWaveAmp * 0.5f
            val p1X = anchorX + sin(phaseOffset + 1.2f) * maxWaveAmp
            val p2X = anchorX - cos(phaseOffset + 2.1f) * maxWaveAmp
            val p3X = anchorX + sin(phaseOffset + 3.4f) * maxWaveAmp * 0.6f

            val p0 = PointF(p0X, 0f)
            val p1 = PointF(p1X, height * 0.33f)
            val p2 = PointF(p2X, height * 0.67f)
            val p3 = PointF(p3X, height)

            // Sample cubic Bézier points
            val points = sampleCubicBezier(p0, p1, p2, p3, stepSamples)
            boundarySplines.add(points)
        }

        // 3. Render Each Continuous Flowing Band
        for (i in 0 until boundarySplines.size - 1) {
            val leftPoints = boundarySplines[i]
            val rightPoints = boundarySplines[i + 1]

            val bandFraction = i.toFloat() / (boundarySplines.size - 1)
            // Monotonic tonal ramp across the bands (Deep Forest -> Jade -> Sage -> Celadon -> Cream)
            val bandColorArgb = palette.getColorAt(bandFraction).toArgb()
            val nextColorArgb = palette.getColorAt((bandFraction + 0.12f).coerceAtMost(1f)).toArgb()

            val bandPath = Path()
            // Trace left boundary from top to bottom
            bandPath.moveTo(leftPoints.first().x, leftPoints.first().y)
            for (pt in leftPoints) {
                bandPath.lineTo(pt.x, pt.y)
            }
            // Trace right boundary from bottom to top
            for (j in rightPoints.indices.reversed()) {
                val pt = rightPoints[j]
                bandPath.lineTo(pt.x, pt.y)
            }
            bandPath.close()

            // Subtle vertical linear gradient fill for organic depth
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f, 0f, 0f, height,
                    bandColorArgb, nextColorArgb,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(bandPath, fillPaint)

            // Tactile Elevation Edge Shadow along the boundary spline
            if (!params.isWireframe && i > 0) {
                val shadowEdgePath = Path()
                shadowEdgePath.moveTo(leftPoints.first().x, leftPoints.first().y)
                for (pt in leftPoints) {
                    shadowEdgePath.lineTo(pt.x, pt.y)
                }

                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    color = AndroidColor.argb(45, 0, 0, 0)
                }
                canvas.drawPath(shadowEdgePath, shadowPaint)

                // Delicate highlight line
                val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                    color = AndroidColor.argb(35, 255, 255, 255)
                }
                canvas.drawPath(shadowEdgePath, highlightPaint)
            }

            // Wireframe stroke mode
            if (params.isWireframe) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth
                    color = bandColorArgb
                }
                canvas.drawPath(bandPath, strokePaint)
            }
        }
    }

    /**
     * Samples points along a cubic Bézier curve using the standard polynomial formula:
     * B(t) = (1-t)^3 P0 + 3(1-t)^2 t P1 + 3(1-t) t^2 P2 + t^3 P3
     */
    private fun sampleCubicBezier(
        p0: PointF,
        p1: PointF,
        p2: PointF,
        p3: PointF,
        steps: Int
    ): List<PointF> {
        val points = ArrayList<PointF>(steps + 1)
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val u = 1f - t
            val tt = t * t
            val uu = u * u
            val uuu = uu * u
            val ttt = tt * t

            val x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x
            val y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y

            points.add(PointF(x, y))
        }
        return points
    }
}
