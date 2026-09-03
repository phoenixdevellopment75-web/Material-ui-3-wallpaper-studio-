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

/**
 * Stacked Horizon Pills Wallpaper Engine (Reference Image 1000053776)
 *
 * Renders:
 * - A centered, vertical stack of 8 to 10 horizontal stadium pills with smooth rounded end caps.
 * - Monotonic tonal gradient ramp stepping cleanly down the screen over a warm cream/sand canvas.
 * - Subtle 4dp ambient elevation shadows cast between overlapping horizontal pill rungs.
 */
object StackedPillsRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val rng = MathUtils.FastRandom(params.seed)

        // 1. Warm cream / neutral backdrop from palette
        val bgTop = palette.getColorAt(0.96f).toArgb()
        val bgBottom = palette.getColorAt(0.88f).toArgb()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgTop, bgBottom,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Determine number of pills: 8 to 10 rungs
        val pillCount = (8 + (params.complexity * 1.5f).toInt()).coerceIn(8, 10)

        // Sizing & Spacing from user-configurable tune parameters
        val pillWidth = width * params.pillWidth.coerceIn(0.4f, 1.0f) * params.scale.coerceIn(0.6f, 1.4f)
        val pillHeight = height * params.pillHeight.coerceIn(0.02f, 0.15f)
        val maxCap = minOf(pillWidth / 2f, pillHeight / 2f)
        val cornerRadius = maxCap * params.pillCurvature.coerceIn(0.1f, 1.0f)

        // Spacing stride between rungs
        val verticalSpacing = height * params.pillSpacing.coerceIn(0.0f, 0.08f)
        val stepY = pillHeight + verticalSpacing

        val totalStackHeight = pillHeight + (pillCount - 1) * stepY
        val startY = (height - totalStackHeight) / 2f
        val centerX = width / 2f

        // 3. Render pills from top to bottom (so upper pills cast shadows on lower ones)
        for (i in 0 until pillCount) {
            val progress = i.toFloat() / (pillCount - 1) // 0.0 (top) .. 1.0 (bottom)

            // Monotonic tonal ramp stepping down the screen (e.g. Warm Ochre -> Sunset Orange -> Crimson -> Deep Plum)
            val colorT = (0.15f + progress * 0.75f).coerceIn(0.05f, 0.95f)
            val pillColor = palette.getColorAt(colorT).toArgb()
            val pillColorEnd = palette.getColorAt((colorT + 0.08f).coerceAtMost(0.98f)).toArgb()

            val currentY = startY + i * stepY
            val left = centerX - pillWidth / 2f
            val top = currentY
            val right = centerX + pillWidth / 2f
            val bottom = currentY + pillHeight

            val pillRect = RectF(left, top, right, bottom)
            val pillPath = Path().apply {
                addRoundRect(pillRect, cornerRadius, cornerRadius, Path.Direction.CW)
            }

            // Elevation drop shadow (4dp ambient elevation)
            if (!params.isWireframe) {
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = AndroidColor.TRANSPARENT
                    setShadowLayer(
                        16f,
                        0f,
                        8f,
                        AndroidColor.argb(55, 10, 10, 20)
                    )
                }
                canvas.drawPath(pillPath, shadowPaint)
            }

            // Fill with smooth horizontal or vertical subtle tonal gradient
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                if (params.isWireframe) {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth * (width / 500f)
                    color = pillColor
                } else {
                    style = Paint.Style.FILL
                    shader = LinearGradient(
                        left, top, right, bottom,
                        pillColor, pillColorEnd,
                        Shader.TileMode.CLAMP
                    )
                }
            }
            canvas.drawPath(pillPath, fillPaint)

            // Subtle highlight top rim on each pill for tactile M3 feel
            if (!params.isWireframe) {
                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                    color = AndroidColor.argb(50, 255, 255, 255)
                }
                canvas.drawPath(pillPath, rimPaint)
            }
        }
    }
}
