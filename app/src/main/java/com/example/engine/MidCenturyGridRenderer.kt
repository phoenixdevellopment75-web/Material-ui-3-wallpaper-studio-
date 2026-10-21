package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.math.MathUtils
import com.example.palette.ColorPalette

/**
 * Mid-Century Modern Bauhaus Semicircle Tile Grid Procedural Wallpaper Engine.
 *
 * Enforces strict 4x7 cell alignment with uniform 4dp spacing:
 * - Subdivides canvas into an exact grid of 4 columns x 7 rows covering the entire viewport.
 * - Each cell contains exactly one clean geometric primitive:
 *   1. Horizontal semicircle (top or bottom half)
 *   2. Vertical semicircle (left or right half)
 *   3. Quadrant fan arc (90° corner sweep)
 *   4. Perfectly centered solid circle
 * - Alternating cells alternate colors between palette.stops[1] and palette.stops[2].
 * - Dynamically binds to active ColorPalette stops (background, primaryAccent, secondaryAccent).
 */
object MidCenturyGridRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        // Dynamically inject active ColorPalette stops
        val background = params.palette.stops.firstOrNull() ?: Color(0xFFF7F5F0)
        val primaryAccent = params.palette.stops.getOrElse(1) { Color(0xFF1E2C3D) }
        val secondaryAccent = params.palette.stops.getOrElse(2) { Color(0xFFD99B56) }

        // Clear canvas with dynamic background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = background.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Strict 4 columns x 7 rows modular grid
        val cols = 4
        val rows = 7
        val cellW = width / cols.toFloat()
        val cellH = height / rows.toFloat()
        val density = width / 380f
        val insetPx = 4f * density

        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            if (params.isWireframe) {
                strokeWidth = (width * 0.005f).coerceAtLeast(3f)
            }
        }
        val tempPath = Path()
        val tempRect = RectF()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = c * cellW
                val top = r * cellH
                val right = left + cellW
                val bottom = top + cellH
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val minDim = minOf(cellW, cellH)

                val cellSeed = params.seed + (r * 31L + c * 73L) * 1097L
                val rng = MathUtils.FastRandom(cellSeed)

                // Alternating cells alternate colors between palette.stops[1] and palette.stops[2]
                val isPrimary = ((r + c) % 2 == 0)
                val tokenColor = if (isPrimary) primaryAccent else secondaryAccent
                shapePaint.color = tokenColor.toArgb()

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
                        // 3. Quadrant fan arc (90° sweep)
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

/**
 * Jetpack Compose DrawScope extension for rendering Bauhaus Semicircles.
 * Uses dynamic ColorPalette stops and strict 4x7 grid with 4dp spacing.
 */
fun DrawScope.drawBauhausSemicircles(
    palette: ColorPalette,
    seed: Long
) {
    val background = palette.stops.firstOrNull() ?: Color(0xFFF7F5F0)
    val primaryAccent = palette.stops.getOrElse(1) { Color(0xFF1E2C3D) }
    val secondaryAccent = palette.stops.getOrElse(2) { Color(0xFFD99B56) }

    // Draw solid dynamic background
    drawRect(color = background)

    val cols = 4
    val rows = 7
    val cellW = size.width / cols
    val cellH = size.height / rows
    val insetPx = 4.dp.toPx()

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val left = c * cellW
            val top = r * cellH
            val cx = left + (cellW / 2f)
            val cy = top + (cellH / 2f)
            val minDim = minOf(cellW, cellH)

            val cellSeed = seed + (r * 31L + c * 73L) * 1097L
            val rng = MathUtils.FastRandom(cellSeed)

            // Alternating cells alternate colors between palette.stops[1] and palette.stops[2]
            val isPrimary = ((r + c) % 2 == 0)
            val tokenColor = if (isPrimary) primaryAccent else secondaryAccent
            val tokenRadius = ((minDim / 2f) - insetPx).coerceAtLeast(minDim * 0.25f)
            val primitiveType = rng.nextInt(4)

            when (primitiveType) {
                0 -> {
                    // Horizontal semicircle
                    val isTopHalf = rng.nextFloat() > 0.5f
                    val startAngle = if (isTopHalf) 180f else 0f
                    drawArc(
                        color = tokenColor,
                        startAngle = startAngle,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx - tokenRadius, cy - tokenRadius),
                        size = Size(tokenRadius * 2, tokenRadius * 2),
                        style = Fill
                    )
                }

                1 -> {
                    // Vertical semicircle
                    val isLeftHalf = rng.nextFloat() > 0.5f
                    val startAngle = if (isLeftHalf) 90f else 270f
                    drawArc(
                        color = tokenColor,
                        startAngle = startAngle,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx - tokenRadius, cy - tokenRadius),
                        size = Size(tokenRadius * 2, tokenRadius * 2),
                        style = Fill
                    )
                }

                2 -> {
                    // Quadrant fan
                    val quadrant = rng.nextInt(4)
                    val startAngle = quadrant * 90f
                    drawArc(
                        color = tokenColor,
                        startAngle = startAngle,
                        sweepAngle = 90f,
                        useCenter = true,
                        topLeft = Offset(cx - tokenRadius, cy - tokenRadius),
                        size = Size(tokenRadius * 2, tokenRadius * 2),
                        style = Fill
                    )
                }

                else -> {
                    // Full circle
                    drawCircle(
                        color = tokenColor,
                        radius = tokenRadius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
