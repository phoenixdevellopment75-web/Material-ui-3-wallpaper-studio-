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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Polar Concentric Labyrinth & Segmented Arc Renderer.
 *
 * Implements concentric circular labyrinth tracks, segmented geometric arcs,
 * rhythmic maze gaps, and tactile elevation shadows.
 */
object PolarLabyrinthRenderer {

    fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)
        val rng = MathUtils.FastRandom(params.seed)

        val palette = params.palette
        val bgArgb = palette.colors.firstOrNull()?.toArgb() ?: 0xFF141218.toInt()
        val bgSecond = palette.colors.getOrNull(1)?.toArgb() ?: bgArgb

        // Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgArgb, bgSecond,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Center origin based on subTypeIndex
        val (originX, originY) = when (params.subTypeIndex % 5) {
            0 -> Pair(width * 0.85f, height * 0.25f) // Top-Right Corner
            1 -> Pair(width * 0.15f, height * 0.80f) // Bottom-Left Corner
            2 -> Pair(width * 0.50f, height * 0.45f) // Center Vortex
            3 -> Pair(width * 0.90f, height * 0.70f) // Bottom-Right Nexus
            else -> Pair(width * 0.30f, height * 0.20f) // Top-Left Orbit
        }

        val colors = palette.colors.map { it.toArgb() }
        val ringCount = (6 + (params.complexity * 5).toInt()).coerceIn(4, 14)
        val maxRadius = maxOf(width, height) * 1.1f * params.scale
        val ringThickness = (maxRadius / (ringCount * 2.2f)).coerceIn(12f, 75f)
        val ringGap = ringThickness * 0.45f

        var currentRadius = ringThickness * 1.2f

        for (i in 0 until ringCount) {
            val color = colors[(i + 1) % colors.size]
            val ringOuterR = currentRadius + ringThickness
            val ringInnerR = currentRadius
            val midR = (ringOuterR + ringInnerR) / 2f

            // Generate 2-4 segmented arcs per ring
            val segmentCount = (2 + (rng.nextFloat() * 3).toInt()).coerceIn(1, 4)
            val angleStep = 360f / segmentCount
            val gapAngle = 18f + (rng.nextFloat() * 24f * params.distortion)

            for (s in 0 until segmentCount) {
                val startAngle = s * angleStep + (i * 22f) + (rng.nextFloat() * 10f) + params.rotationDegrees
                val sweepAngle = (angleStep - gapAngle).coerceAtLeast(15f)

                val arcPath = createArcSegmentPath(
                    originX, originY,
                    ringInnerR, ringOuterR,
                    startAngle, sweepAngle
                )

                // Tactile shadow
                if (!params.isWireframe) {
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        this.color = AndroidColor.TRANSPARENT
                        setShadowLayer(
                            26f,
                            0f,
                            12f,
                            AndroidColor.argb(80, 0, 0, 0)
                        )
                    }
                    canvas.drawPath(arcPath, shadowPaint)
                }

                // Arc Fill / Stroke
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    if (params.isWireframe) {
                        style = Paint.Style.STROKE
                        strokeWidth = (params.lineWidth * (width / 500f)).coerceAtLeast(2f)
                        strokeCap = Paint.Cap.ROUND
                    } else {
                        style = Paint.Style.FILL
                    }
                }
                canvas.drawPath(arcPath, paint)

                // Highlight border
                if (!params.isWireframe) {
                    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1.2f
                        this.color = AndroidColor.argb(35, 255, 255, 255)
                    }
                    canvas.drawPath(arcPath, rimPaint)
                }
            }

            currentRadius += ringThickness + ringGap
        }
    }

    private fun createArcSegmentPath(
        cx: Float,
        cy: Float,
        innerR: Float,
        outerR: Float,
        startDeg: Float,
        sweepDeg: Float
    ): Path {
        val path = Path()
        val outerRect = RectF(cx - outerR, cy - outerR, cx + outerR, cy + outerR)
        val innerRect = RectF(cx - innerR, cy - innerR, cx + innerR, cy + innerR)

        path.arcTo(outerRect, startDeg, sweepDeg)
        path.arcTo(innerRect, startDeg + sweepDeg, -sweepDeg)
        path.close()
        return path
    }
}
