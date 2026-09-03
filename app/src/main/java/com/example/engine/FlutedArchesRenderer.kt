package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.ceil

/**
 * Fluted Labyrinth Line Arches Procedural Wallpaper Engine.
 *
 * Clean, disciplined non-intersecting concentric arch modules:
 * - Eliminates all diagonal criss-crosses and spider-web moiré artifacts.
 * - Divides canvas into a clean grid of strictly square modules (W_cell = H_cell = S).
 * - In each module, draws either horizontal parallel lines OR vertical concentric quarter-arches
 *   (or vertical parallel lines).
 * - All lines terminate precisely at cell boundary edges at uniform track fractions
 *   so adjacent cells link together seamlessly without overlapping line collisions.
 * - Single crisp stroke color on textured bone white paper (#F7F4EE).
 */
object FlutedArchesRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        // 1. Dual-tone palette matching exact specifications
        val (bgArgb, strokeArgb) = when (params.subTypeIndex % 5) {
            0 -> Pair(
                0xFFF7F4EE.toInt(), // Textured bone white paper (#F7F4EE)
                0xFF546A55.toInt()  // Vintage sage green (#546A55)
            )
            1 -> Pair(
                0xFFF6F3EC.toInt(), // Warm chalk
                0xFF2D2E2E.toInt()  // Architectural warm charcoal
            )
            2 -> Pair(
                0xFFF8F2E8.toInt(), // Cream linen
                0xFFB85D43.toInt()  // Terracotta rust
            )
            3 -> Pair(
                0xFFF4F5F2.toInt(), // Bone paper
                0xFF3B5249.toInt()  // Deep spruce green
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

        // Draw flat clean paper background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgArgb
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Setup clean grid of strictly square modules (W_cell = H_cell)
        val cols = 4
        val cellSize = width / cols.toFloat()
        val rows = ceil(height / cellSize).toInt()

        // 5 parallel tracks per module
        val tracks = 5

        // Stroke width scaled to display density, using BUTT cap to terminate precisely at cell boundaries
        val density = width / 380f
        val strokeWidthPx = (2.0f * density).coerceIn(3.0f, 6.5f)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeArgb
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.BUTT
            strokeJoin = Paint.Join.MITER
        }

        val masterPath = Path()

        // 3. Render clean, non-intersecting modules in each square cell
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = c * cellSize
                val top = r * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                val cellSeed = params.seed + (r * 31L + c * 59L) * 1063L
                val rng = MathUtils.FastRandom(cellSeed)
                val moduleType = rng.nextInt(4)

                when (moduleType) {
                    0 -> {
                        // Module 0: Horizontal parallel lines terminating squarely at x = left and x = right
                        for (k in 0 until tracks) {
                            val frac = (k + 0.5f) / tracks
                            val y = top + frac * cellSize
                            masterPath.moveTo(left, y)
                            masterPath.lineTo(right, y)
                        }
                    }
                    1 -> {
                        // Module 1: Vertical concentric quarter-arches (Top-Left & Bottom-Right)
                        // All arches have identical radius in x and y because W_cell = H_cell
                        for (k in 0 until tracks) {
                            val frac = (k + 0.5f) / tracks
                            val radTL = frac * cellSize
                            val rectTL = RectF(left - radTL, top - radTL, left + radTL, top + radTL)
                            masterPath.arcTo(rectTL, 0f, 90f, false)

                            val radBR = (1f - frac) * cellSize
                            val rectBR = RectF(right - radBR, bottom - radBR, right + radBR, bottom + radBR)
                            masterPath.arcTo(rectBR, 180f, 90f, false)
                        }
                    }
                    2 -> {
                        // Module 2: Vertical concentric quarter-arches (Top-Right & Bottom-Left)
                        for (k in 0 until tracks) {
                            val frac = (k + 0.5f) / tracks
                            val radTR = frac * cellSize
                            val rectTR = RectF(right - radTR, top - radTR, right + radTR, top + radTR)
                            masterPath.arcTo(rectTR, 90f, 90f, false)

                            val radBL = (1f - frac) * cellSize
                            val rectBL = RectF(left - radBL, bottom - radBL, left + radBL, bottom + radBL)
                            masterPath.arcTo(rectBL, 270f, 90f, false)
                        }
                    }
                    else -> {
                        // Module 3: Vertical parallel lines terminating squarely at y = top and y = bottom
                        for (k in 0 until tracks) {
                            val frac = (k + 0.5f) / tracks
                            val x = left + frac * cellSize
                            masterPath.moveTo(x, top)
                            masterPath.lineTo(x, bottom)
                        }
                    }
                }
            }
        }

        // Draw master non-intersecting line labyrinth
        canvas.drawPath(masterPath, linePaint)
    }
}
