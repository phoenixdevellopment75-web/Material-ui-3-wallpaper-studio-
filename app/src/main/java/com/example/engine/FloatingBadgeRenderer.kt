package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3. Floating Pastel Badges & Nebulae Renderer
 *
 * Minimalist floating composition:
 * - 1 large scalloped 12-point flower badge
 * - 2 soft circular or squircle tokens placed asymmetrically across the canvas
 * - Radial gradient fills with soft alpha falls (1.0f -> 0.7f) over clean pastel background tones.
 * - M3 elevation drop shadows for floating tactile depth.
 */
object FloatingBadgeRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val rng = MathUtils.FastRandom(params.seed)

        // 1. Clean Pastel Background Tone (Lilac, Pastel Sky Blue, or Soft Lavender)
        val bgColor = palette.colors.lastOrNull()?.toArgb() ?: 0xFFF2EFF6.toInt()
        val bgSecondary = palette.getColorAt(0.85f).toArgb()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.45f, height * 0.45f, width * 0.85f,
                intArrayOf(bgColor, bgSecondary),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Shape 1: Large Scalloped 12-Point Flower Badge (Main Focal Element)
        val badgeLobes = when (params.subTypeIndex % 5) {
            1 -> 8
            2 -> 10
            else -> 12 // Default 12-point scallop matching reference
        }
        val badgeCenterX = width * (0.38f + (rng.nextFloat() - 0.5f) * 0.08f)
        val badgeCenterY = height * (0.58f + (rng.nextFloat() - 0.5f) * 0.08f)
        val badgeRadius = (width * 0.28f * params.scale).coerceIn(width * 0.18f, width * 0.42f)
        val badgeDepth = badgeRadius * 0.16f * params.distortion.coerceIn(0.5f, 1.5f)

        val badgeColor1 = palette.getColorAt(0.35f).toArgb()
        val badgeColor2 = palette.getColorAt(0.55f).toArgb()

        val badgePath = buildScallopPath(
            cx = badgeCenterX,
            cy = badgeCenterY,
            baseRadius = badgeRadius,
            lobeDepth = badgeDepth,
            lobes = badgeLobes
        )

        drawFloatingShape(
            canvas = canvas,
            path = badgePath,
            cx = badgeCenterX,
            cy = badgeCenterY,
            radius = badgeRadius,
            colorCenter = badgeColor1,
            colorEdge = badgeColor2,
            isWireframe = params.isWireframe,
            lineWidth = params.lineWidth
        )

        // 3. Shape 2: Asymmetric Soft Squircle / Pebble Token
        val token2X = width * (0.68f + (rng.nextFloat() - 0.5f) * 0.08f)
        val token2Y = height * (0.34f + (rng.nextFloat() - 0.5f) * 0.08f)
        val token2Radius = (width * 0.16f * params.scale).coerceIn(width * 0.10f, width * 0.24f)

        val token2Color1 = palette.getColorAt(0.18f).toArgb()
        val token2Color2 = palette.getColorAt(0.32f).toArgb()

        val token2Path = Path().apply {
            val rect = RectF(
                token2X - token2Radius,
                token2Y - token2Radius,
                token2X + token2Radius,
                token2Y + token2Radius
            )
            addRoundRect(rect, token2Radius * 0.45f, token2Radius * 0.45f, Path.Direction.CW)
        }

        drawFloatingShape(
            canvas = canvas,
            path = token2Path,
            cx = token2X,
            cy = token2Y,
            radius = token2Radius,
            colorCenter = token2Color1,
            colorEdge = token2Color2,
            isWireframe = params.isWireframe,
            lineWidth = params.lineWidth
        )

        // 4. Shape 3: Asymmetric Minimalist Soft Circular / Torus Token
        val token3X = width * (0.74f + (rng.nextFloat() - 0.5f) * 0.06f)
        val token3Y = height * (0.76f + (rng.nextFloat() - 0.5f) * 0.06f)
        val token3Radius = (width * 0.095f * params.scale).coerceIn(width * 0.06f, width * 0.16f)

        val token3Color1 = palette.getColorAt(0.68f).toArgb()
        val token3Color2 = palette.getColorAt(0.82f).toArgb()

        val token3Path = Path().apply {
            addCircle(token3X, token3Y, token3Radius, Path.Direction.CW)
        }

        drawFloatingShape(
            canvas = canvas,
            path = token3Path,
            cx = token3X,
            cy = token3Y,
            radius = token3Radius,
            colorCenter = token3Color1,
            colorEdge = token3Color2,
            isWireframe = params.isWireframe,
            lineWidth = params.lineWidth
        )
    }

    private fun drawFloatingShape(
        canvas: Canvas,
        path: Path,
        cx: Float,
        cy: Float,
        radius: Float,
        colorCenter: Int,
        colorEdge: Int,
        isWireframe: Boolean,
        lineWidth: Float
    ) {
        // Elevation Drop Shadow (dx=0, dy=18dp/36px, soft blur=32)
        if (!isWireframe) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = AndroidColor.TRANSPARENT
                setShadowLayer(32f, 0f, 18f, AndroidColor.argb(55, 0, 0, 0))
            }
            canvas.drawPath(path, shadowPaint)
        }

        // Radial Gradient Fill with Soft Alpha Fall (1.0f -> 0.72f)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            if (isWireframe) {
                style = Paint.Style.STROKE
                strokeWidth = lineWidth.coerceAtLeast(1.5f)
                color = colorCenter
            } else {
                style = Paint.Style.FILL
                shader = RadialGradient(
                    cx, cy, radius * 1.15f,
                    intArrayOf(colorCenter, colorEdge),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }
        canvas.drawPath(path, fillPaint)

        // Delicate M3 Inner Tactile Rim
        if (!isWireframe) {
            val innerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                color = AndroidColor.argb(40, 255, 255, 255)
            }
            canvas.drawPath(path, innerBorder)
        }
    }

    private fun buildScallopPath(
        cx: Float,
        cy: Float,
        baseRadius: Float,
        lobeDepth: Float,
        lobes: Int
    ): Path {
        val path = Path()
        val totalSteps = lobes * 24
        val dTheta = (2.0 * PI / totalSteps).toFloat()

        for (i in 0..totalSteps) {
            val theta = i * dTheta
            val r = baseRadius + lobeDepth * cos(lobes * theta)
            val x = cx + r * cos(theta)
            val y = cy + r * sin(theta)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return path
    }
}
