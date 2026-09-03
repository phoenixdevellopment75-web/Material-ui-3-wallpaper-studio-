package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils

/**
 * Tonal Circle Matrix / Dot Grid Wallpaper Engine (Reference Image 1000053775)
 *
 * Renders:
 * - A minimalist 4x7 grid (4 columns, 7 rows = 28 dots) centered on the screen with balanced margins.
 * - Monotonic vertical lightness ramp: each horizontal row transitions smoothly from light sage/cream
 *   at the top row down to deep forest green at the bottom row.
 * - Subtle ambient elevation drop shadows for tactile depth.
 */
object DotGridRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette

        // 1. Clean backdrop from palette
        val bgTop = palette.getColorAt(0.96f).toArgb()
        val bgBottom = palette.getColorAt(0.90f).toArgb()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgTop, bgBottom,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Grid Geometry: 4 columns x 7 rows
        val cols = 4
        val rows = 7

        val baseDim = minOf(width, height)
        val dotRadius = (baseDim * 0.082f * params.scale).coerceIn(baseDim * 0.045f, baseDim * 0.11f)

        // Balanced margins: center the 4x7 matrix
        val gridWidth = width * 0.74f
        val gridHeight = height * 0.70f

        val spacingX = gridWidth / (cols - 1)
        val spacingY = gridHeight / (rows - 1)

        val startX = (width - gridWidth) / 2f
        val startY = (height - gridHeight) / 2f

        // 3. Render circular dots row by row
        for (row in 0 until rows) {
            val progressY = row.toFloat() / (rows - 1) // 0.0 (top) .. 1.0 (bottom)

            // Monotonic vertical lightness ramp:
            // Top row is light sage/cream, bottom row is deep forest green/rich tonal color
            val colorT = (0.85f - progressY * 0.75f).coerceIn(0.06f, 0.94f)
            val dotColor = palette.getColorAt(colorT).toArgb()

            for (col in 0 until cols) {
                val cx = startX + col * spacingX
                val cy = startY + row * spacingY

                // Drop shadow
                if (!params.isWireframe) {
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = AndroidColor.TRANSPARENT
                        setShadowLayer(
                            14f,
                            0f,
                            6f,
                            AndroidColor.argb(40, 10, 20, 15)
                        )
                    }
                    canvas.drawCircle(cx, cy, dotRadius, shadowPaint)
                }

                // Dot Surface Fill or Wireframe
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    if (params.isWireframe) {
                        style = Paint.Style.STROKE
                        strokeWidth = params.lineWidth * (baseDim / 500f)
                        color = dotColor
                    } else {
                        style = Paint.Style.FILL
                        color = dotColor
                    }
                }
                canvas.drawCircle(cx, cy, dotRadius, dotPaint)

                // Tactile rim highlight
                if (!params.isWireframe) {
                    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1.0f
                        color = AndroidColor.argb(35, 255, 255, 255)
                    }
                    canvas.drawCircle(cx, cy, dotRadius, rimPaint)
                }
            }
        }
    }
}
