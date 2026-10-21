package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils

/**
 * Mid-Century Modern Bauhaus Semicircle Grid Procedural Wallpaper Engine.
 *
 * Strict 4 x 7 Geometric Primitive Tile Matrix:
 * - BAN all skewed triangles and sheared wedges.
 * - Uniform grid of 4 columns x 7 rows (w_cell = W / 4, h_cell = H / 7).
 * - Closed set of geometric primitives per cell:
 *   1. Solid filled semicircle: Top half, Bottom half, Left half, or Right half.
 *   2. Solid full circle centered in cell (r = min(w_cell, h_cell) / 2).
 *   3. Flat solid color fill (background tone or accent tone).
 *   4. Two-tone quarter-circle corner arcs (r = min(w_cell, h_cell)).
 * - Color palette constraint: 3 or 4 high-contrast matte colors (Ochre #DDA15E, Deep Navy #1D3557,
 *   Terra Cotta #BC6C25, Ivory #FEFAE0).
 * - Zero allocations in draw loop: Reusable Path, RectF, and Paint objects.
 */
object BauhausGridRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        // 1. Dynamically bind active ColorPalette stops (background, primaryAccent, secondaryAccent)
        val background = params.palette.stops.firstOrNull() ?: Color(0xFFF7F5F0)
        val primaryAccent = params.palette.stops.getOrElse(1) { Color(0xFF1E2C3D) }
        val secondaryAccent = params.palette.stops.getOrElse(2) { Color(0xFFD99B56) }

        // Clear canvas with dynamic base background color
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = background.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Pre-allocate single reusable Paint, Path, and RectF for zero-allocation rendering
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            if (params.isWireframe) {
                strokeWidth = (width * 0.005f).coerceAtLeast(3f)
            }
        }
        val tempPath = Path()
        val tempRect = RectF()

        // 2. Strict 4 columns x 7 rows modular grid
        val cols = 4
        val rows = 7
        val cellW = width / cols.toFloat()
        val cellH = height / rows.toFloat()
        val density = width / 380f
        val insetPx = 4f * density

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = c * cellW
                val top = r * cellH
                val right = left + cellW
                val bottom = top + cellH
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val minDim = minOf(cellW, cellH)

                val cellSeed = params.seed + (r * 43L + c * 71L) * 1097L
                val rng = MathUtils.FastRandom(cellSeed)

                // Alternating cells alternate colors between primaryAccent and secondaryAccent
                val isPrimary = ((r + c) % 2 == 0)
                val tokenColor = if (isPrimary) primaryAccent.toArgb() else secondaryAccent.toArgb()
                shapePaint.color = tokenColor

                // Strictly ONE centered geometric token with 4dp inset
                val tokenRadius = ((minDim / 2f) - insetPx).coerceAtLeast(minDim * 0.25f)
                val primitiveType = rng.nextInt(4)

                tempRect.set(cx - tokenRadius, cy - tokenRadius, cx + tokenRadius, cy + tokenRadius)
                tempPath.reset()

                when (primitiveType) {
                    0 -> {
                        // 1. Horizontal semicircle (180° sweep): Top or Bottom half
                        val isTopHalf = rng.nextFloat() > 0.5f
                        if (isTopHalf) {
                            tempPath.moveTo(cx - tokenRadius, cy)
                            tempPath.arcTo(tempRect, 180f, 180f, false)
                            tempPath.close()
                        } else {
                            tempPath.moveTo(cx + tokenRadius, cy)
                            tempPath.arcTo(tempRect, 0f, 180f, false)
                            tempPath.close()
                        }
                        canvas.drawPath(tempPath, shapePaint)
                    }

                    1 -> {
                        // 2. Vertical semicircle (180° sweep): Left or Right half
                        val isLeftHalf = rng.nextFloat() > 0.5f
                        if (isLeftHalf) {
                            tempPath.moveTo(cx, cy + tokenRadius)
                            tempPath.arcTo(tempRect, 90f, 180f, false)
                            tempPath.close()
                        } else {
                            tempPath.moveTo(cx, cy - tokenRadius)
                            tempPath.arcTo(tempRect, 270f, 180f, false)
                            tempPath.close()
                        }
                        canvas.drawPath(tempPath, shapePaint)
                    }

                    2 -> {
                        // 3. Quadrant fan (90° sweep arc): Centered in cell with 90° sweep
                        val quadrant = rng.nextInt(4)
                        val startAngle = quadrant * 90f
                        tempPath.moveTo(cx, cy)
                        tempPath.arcTo(tempRect, startAngle, 90f, false)
                        tempPath.close()
                        canvas.drawPath(tempPath, shapePaint)
                    }

                    else -> {
                        // 4. Centered full circle
                        canvas.drawCircle(cx, cy, tokenRadius, shapePaint)
                    }
                }
            }
        }
    }
}
