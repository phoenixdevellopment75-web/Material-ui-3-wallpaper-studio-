package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.math.MathUtils
import com.example.palette.ColorPalette
import kotlin.math.ceil

/**
 * Fluted Labyrinth Line Arches Procedural Wallpaper Engine.
 *
 * Clean, disciplined non-intersecting concentric arch modules:
 * - Eliminates all diagonal criss-crosses and spider-web moiré artifacts.
 * - Divides canvas into a clean grid of strictly square modules (W_cell = H_cell = S).
 * - In each module, draws either horizontal parallel lines, vertical parallel lines,
 *   or concentric quarter-arches anchored exclusively at one of the 4 cell corners.
 * - Parallel line tracks cleanly meet and merge with adjacent concentric quarter-arcs
 *   at cell boundary lines.
 * - Stroke color binds dynamically to palette.stops[1] or onSurface, with background binding to palette.stops[0].
 */
object FlutedArchesRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        // 1. Dynamic palette bindings: stroke binds to stops[1], background to stops[0]
        val background = params.palette.stops.firstOrNull() ?: Color(0xFFF7F4EE)
        val strokeColor = params.palette.stops.getOrElse(1) { Color(0xFF546A55) }

        // Draw flat clean paper background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = background.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Setup clean grid of strictly square modules (W_cell = H_cell = S)
        val cols = 4
        val cellSize = width / cols.toFloat()
        val rows = ceil(height / cellSize).toInt()

        // Stroke width uniform 1.5dp scaled to display density, using BUTT cap
        val density = width / 380f
        val strokeWidthPx = 1.5f * density

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.BUTT
            strokeJoin = Paint.Join.MITER
        }

        // Reusable RectF for zero-allocation rendering pass
        val arcRect = RectF()
        val numTracks = 4

        // 3. Render disciplined, non-intersecting modules in each cell with strict clipping
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = c * cellSize
                val top = r * cellSize
                val right = if (c == cols - 1) width else (left + cellSize)
                val bottom = if (r == rows - 1 && (top + cellSize) >= height) height else (top + cellSize)

                val cellSeed = params.seed + (r * 31L + c * 59L) * 1063L
                val rng = MathUtils.FastRandom(cellSeed)

                canvas.save()
                canvas.clipRect(left, top, right, bottom)

                val option = rng.nextInt(6)

                when (option) {
                    0 -> {
                        // Parallel horizontal lines at track offsets (k - 0.5f) / numTracks * cellSize
                        for (k in 1..numTracks) {
                            val trackOffset = ((k - 0.5f) / numTracks.toFloat()) * cellSize
                            val y = top + trackOffset
                            canvas.drawLine(left, y, right, y, linePaint)
                        }
                    }
                    1 -> {
                        // Parallel vertical lines at track offsets (k - 0.5f) / numTracks * cellSize
                        for (k in 1..numTracks) {
                            val trackOffset = ((k - 0.5f) / numTracks.toFloat()) * cellSize
                            val x = left + trackOffset
                            canvas.drawLine(x, top, x, bottom, linePaint)
                        }
                    }
                    2 -> {
                        // Concentric quarter-arcs anchored at Top-Left
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks.toFloat()) * cellSize
                            arcRect.set(left - rad, top - rad, left + rad, top + rad)
                            canvas.drawArc(arcRect, 0f, 90f, false, linePaint)
                        }
                    }
                    3 -> {
                        // Concentric quarter-arcs anchored at Top-Right
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks.toFloat()) * cellSize
                            arcRect.set(right - rad, top - rad, right + rad, top + rad)
                            canvas.drawArc(arcRect, 90f, 90f, false, linePaint)
                        }
                    }
                    4 -> {
                        // Concentric quarter-arcs anchored at Bottom-Right
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks.toFloat()) * cellSize
                            arcRect.set(right - rad, bottom - rad, right + rad, bottom + rad)
                            canvas.drawArc(arcRect, 180f, 90f, false, linePaint)
                        }
                    }
                    else -> {
                        // Concentric quarter-arcs anchored at Bottom-Left
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks.toFloat()) * cellSize
                            arcRect.set(left - rad, bottom - rad, left + rad, bottom + rad)
                            canvas.drawArc(arcRect, 270f, 90f, false, linePaint)
                        }
                    }
                }

                canvas.restore()
            }
        }
    }
}

/**
 * Jetpack Compose DrawScope extension for rendering Fluted Arches.
 */
fun DrawScope.drawFlutedArches(
    palette: ColorPalette,
    seed: Long
) {
    val background = palette.stops.firstOrNull() ?: Color(0xFFF7F4EE)
    val strokeColor = palette.stops.getOrElse(1) { Color(0xFF546A55) }

    drawRect(color = background)

    val cols = 4
    val cellSize = size.width / cols
    val rows = ceil(size.height / cellSize).toInt()
    val numTracks = 4
    val strokeStyle = Stroke(width = 1.5.dp.toPx())

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val left = c * cellSize
            val top = r * cellSize
            val right = left + cellSize
            val bottom = top + cellSize

            val cellSeed = seed + (r * 31L + c * 59L) * 1063L
            val rng = MathUtils.FastRandom(cellSeed)

            clipRect(left, top, right, bottom) {
                val option = rng.nextInt(6)
                when (option) {
                    0 -> {
                        for (k in 1..numTracks) {
                            val trackOffset = ((k - 0.5f) / numTracks) * cellSize
                            val y = top + trackOffset
                            drawLine(
                                color = strokeColor,
                                start = Offset(left, y),
                                end = Offset(right, y),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                    1 -> {
                        for (k in 1..numTracks) {
                            val trackOffset = ((k - 0.5f) / numTracks) * cellSize
                            val x = left + trackOffset
                            drawLine(
                                color = strokeColor,
                                start = Offset(x, top),
                                end = Offset(x, bottom),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                    2 -> {
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks) * cellSize
                            drawArc(
                                color = strokeColor,
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(left - rad, top - rad),
                                size = Size(rad * 2, rad * 2),
                                style = strokeStyle
                            )
                        }
                    }
                    3 -> {
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks) * cellSize
                            drawArc(
                                color = strokeColor,
                                startAngle = 90f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(right - rad, top - rad),
                                size = Size(rad * 2, rad * 2),
                                style = strokeStyle
                            )
                        }
                    }
                    4 -> {
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks) * cellSize
                            drawArc(
                                color = strokeColor,
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(right - rad, bottom - rad),
                                size = Size(rad * 2, rad * 2),
                                style = strokeStyle
                            )
                        }
                    }
                    else -> {
                        for (k in 1..numTracks) {
                            val rad = ((k - 0.5f) / numTracks) * cellSize
                            drawArc(
                                color = strokeColor,
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(left - rad, bottom - rad),
                                size = Size(rad * 2, rad * 2),
                                style = strokeStyle
                            )
                        }
                    }
                }
            }
        }
    }
}
