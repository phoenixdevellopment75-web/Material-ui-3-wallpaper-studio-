package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils

/**
 * Mid-Century Bauhaus Semicircle Tile Grid Procedural Wallpaper Engine.
 *
 * Strict 4 x 7 geometric tile matrix with uniform cell spacing:
 * - Subdivides canvas into an exact grid of 4 x 7 cells covering the entire screen.
 * - Each tile is centered within its grid cell with a uniform 3dp margin.
 * - Flat muted tones: Muted Sage (#7D8D78) over Off-White (#F7F5F0).
 * - Disciplined geometric primitives:
 *   1. Top-half semicircle.
 *   2. Bottom-half semicircle.
 *   3. Quarter-circle fan anchored at corner.
 *   4. Perfectly centered solid circle or opposing double fans.
 */
object ModularBauhausRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        // 1. Dual-tone flat matte palette matching exact specifications
        val (bgArgb, shapeArgb) = when (params.subTypeIndex % 5) {
            0 -> Pair(
                0xFFF7F5F0.toInt(), // Off-White (#F7F5F0)
                0xFF7D8D78.toInt()  // Muted Sage (#7D8D78)
            )
            1 -> Pair(
                0xFFF4F1EA.toInt(), // Pale chalk
                0xFF5A7260.toInt()  // Deep vintage spruce sage
            )
            2 -> Pair(
                0xFFF5EFEB.toInt(), // Cream paper
                0xFFC26E55.toInt()  // Mid-century warm terracotta
            )
            3 -> Pair(
                0xFFF2F4F3.toInt(), // Cool chalk
                0xFF4F6D7A.toInt()  // Bauhaus slate blue-grey
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

        // Draw clean flat matte background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgArgb
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Paint for geometric primitives
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = shapeArgb
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            if (params.isWireframe) {
                strokeWidth = (width * 0.005f).coerceAtLeast(2.5f)
            }
        }

        // 2. Strict 4 x 7 grid subdivision covering the entire screen
        val cols = 4
        val rows = 7
        val cellW = width / cols
        val cellH = height / rows

        // Uniform 3dp margin inside each grid cell
        val density = width / 380f
        val marginPx = (3.0f * density).coerceIn(3.0f, 6.0f)

        // 3. Render disciplined geometric primitives centered in each cell
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cellLeft = c * cellW + marginPx
                val cellTop = r * cellH + marginPx
                val cellRight = (c + 1) * cellW - marginPx
                val cellBottom = (r + 1) * cellH - marginPx

                val w = cellRight - cellLeft
                val h = cellBottom - cellTop
                val cx = (cellLeft + cellRight) / 2f
                val cy = (cellTop + cellBottom) / 2f

                val cellSeed = params.seed + (r * 19L + c * 37L) * 1039L
                val rng = MathUtils.FastRandom(cellSeed)
                val primitiveType = rng.nextInt(4)

                val path = Path()

                when (primitiveType) {
                    0 -> {
                        // 1. Top-half semicircle (curved dome up, flat bottom), centered at cy
                        val radius = minOf(w / 2f, h / 2f)
                        val baselineY = cy + radius * 0.5f
                        val arcRect = RectF(cx - radius, baselineY - radius * 2f, cx + radius, baselineY)
                        path.moveTo(cx - radius, baselineY)
                        path.arcTo(arcRect, 180f, 180f, false)
                        path.lineTo(cx - radius, baselineY)
                        path.close()
                    }
                    1 -> {
                        // 2. Bottom-half semicircle (flat top, curved dome down), centered at cy
                        val radius = minOf(w / 2f, h / 2f)
                        val baselineY = cy - radius * 0.5f
                        val arcRect = RectF(cx - radius, baselineY, cx + radius, baselineY + radius * 2f)
                        path.moveTo(cx + radius, baselineY)
                        path.arcTo(arcRect, 0f, 180f, false)
                        path.lineTo(cx + radius, baselineY)
                        path.close()
                    }
                    2 -> {
                        // 3. Quarter-circle fan anchored at one of the 4 cell corners
                        val corner = rng.nextInt(4)
                        val radius = minOf(w, h) * 0.95f
                        when (corner) {
                            0 -> {
                                // Top-Left
                                val arcRect = RectF(cellLeft - radius, cellTop - radius, cellLeft + radius, cellTop + radius)
                                path.moveTo(cellLeft, cellTop)
                                path.lineTo(cellLeft + radius, cellTop)
                                path.arcTo(arcRect, 0f, 90f, false)
                                path.close()
                            }
                            1 -> {
                                // Top-Right
                                val arcRect = RectF(cellRight - radius, cellTop - radius, cellRight + radius, cellTop + radius)
                                path.moveTo(cellRight, cellTop)
                                path.lineTo(cellRight, cellTop + radius)
                                path.arcTo(arcRect, 90f, 90f, false)
                                path.close()
                            }
                            2 -> {
                                // Bottom-Right
                                val arcRect = RectF(cellRight - radius, cellBottom - radius, cellRight + radius, cellBottom + radius)
                                path.moveTo(cellRight, cellBottom)
                                path.lineTo(cellRight - radius, cellBottom)
                                path.arcTo(arcRect, 180f, 90f, false)
                                path.close()
                            }
                            else -> {
                                // Bottom-Left
                                val arcRect = RectF(cellLeft - radius, cellBottom - radius, cellLeft + radius, cellBottom + radius)
                                path.moveTo(cellLeft, cellBottom)
                                path.lineTo(cellLeft, cellBottom - radius)
                                path.arcTo(arcRect, 270f, 90f, false)
                                path.close()
                            }
                        }
                    }
                    else -> {
                        // 4. Centered solid circle or opposing double fans
                        val subChoice = rng.nextInt(2)
                        if (subChoice == 0) {
                            val radius = minOf(w, h) * 0.44f
                            path.addCircle(cx, cy, radius, Path.Direction.CW)
                        } else {
                            val fanRadius = minOf(w, h) * 0.70f
                            val rectTL = RectF(cellLeft - fanRadius, cellTop - fanRadius, cellLeft + fanRadius, cellTop + fanRadius)
                            path.moveTo(cellLeft, cellTop)
                            path.lineTo(cellLeft + fanRadius, cellTop)
                            path.arcTo(rectTL, 0f, 90f, false)
                            path.close()

                            val rectBR = RectF(cellRight - fanRadius, cellBottom - fanRadius, cellRight + fanRadius, cellBottom + fanRadius)
                            path.moveTo(cellRight, cellBottom)
                            path.lineTo(cellRight - fanRadius, cellBottom)
                            path.arcTo(rectBR, 180f, 90f, false)
                            path.close()
                        }
                    }
                }

                canvas.drawPath(path, shapePaint)
            }
        }
    }
}
