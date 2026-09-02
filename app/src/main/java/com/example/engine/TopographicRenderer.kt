package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 2. Continuous Topographic Contour Maps Renderer
 *
 * Screen-filling, continuous 2D scalar fields with smooth edge-to-edge marching squares isolines.
 * No isolated or disconnected stamp clusters.
 * Crisp 1.5dp strokes over clean, minimal bone/slate surfaces.
 */
object TopographicRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val colors = params.palette.colors
        val bgArgb = colors.first().toArgb()
        val bgEndArgb = colors[1.coerceAtMost(colors.size - 1)].toArgb()

        // 1. Clean Paper Background Fill
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, height, bgArgb, bgEndArgb, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Dispatch by sub-type
        when (params.subTypeIndex % 5) {
            0 -> renderFilledElevationBands(canvas, width, height, params)
            1 -> renderMinimalistRidgeLines(canvas, width, height, params)
            2 -> renderOceanicTrench(canvas, width, height, params)
            3 -> renderSubtleBasin(canvas, width, height, params)
            4 -> renderDualPeakTopography(canvas, width, height, params)
        }
    }

    /**
     * Layered filled continuous elevation bands with crisp contour boundary strokes
     */
    private fun renderFilledElevationBands(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams
    ) {
        val bands = (8 * params.complexity).toInt().coerceIn(5, 14)
        val cols = 80
        val rows = (80 * (height / width)).toInt().coerceIn(100, 180)
        val grid = generateHeightfield(cols, rows, params, mode = 0)

        val cellW = width / (cols - 1)
        val cellH = height / (rows - 1)

        for (b in 0 until bands) {
            val threshold = (b + 1).toFloat() / (bands + 1)
            val color = params.palette.getColorAt(0.12f + (b.toFloat() / bands) * 0.82f).toArgb()

            val contourPath = traceIsolinePath(grid, cols, rows, threshold, cellW, cellH)

            if (!params.isWireframe) {
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = color
                }
                canvas.drawPath(contourPath, fillPaint)
            }

            // Crisp stroke contour line
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth * (if (b % 3 == 0) 1.6f else 1.0f)
                this.color = if (params.isWireframe) color else 0x44FFFFFF
            }
            canvas.drawPath(contourPath, strokePaint)
        }
    }

    /**
     * Minimalist high-precision continuous single-stroke contour curves with elevation index accents
     */
    private fun renderMinimalistRidgeLines(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams
    ) {
        val isoLevels = (16 * params.complexity).toInt().coerceIn(10, 26)
        val cols = 90
        val rows = (90 * (height / width)).toInt().coerceIn(120, 200)
        val grid = generateHeightfield(cols, rows, params, mode = 1)

        val cellW = width / (cols - 1)
        val cellH = height / (rows - 1)

        val strokeColor = params.palette.getColorAt(0.85f).toArgb()
        val indexColor = params.palette.getColorAt(0.98f).toArgb()

        for (i in 1..isoLevels) {
            val threshold = i.toFloat() / (isoLevels + 1)
            val isIndexContour = (i % 4 == 0)
            val path = traceIsolinePath(grid, cols, rows, threshold, cellW, cellH)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = if (isIndexContour) params.lineWidth * 1.8f else params.lineWidth * 1.0f
                color = if (isIndexContour) indexColor else strokeColor
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * Deep canyon/trench continuous isolines spanning full viewport
     */
    private fun renderOceanicTrench(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams
    ) {
        val levels = (18 * params.complexity).toInt().coerceIn(10, 28)
        val cols = 80
        val rows = (80 * (height / width)).toInt().coerceIn(100, 180)
        val grid = generateHeightfield(cols, rows, params, mode = 2)

        val cellW = width / (cols - 1)
        val cellH = height / (rows - 1)

        for (i in 1..levels) {
            val t = i.toFloat() / levels
            val threshold = t * t // non-linear exponential spacing
            val color = params.palette.getColorAt(0.2f + t * 0.75f).toArgb()

            val path = traceIsolinePath(grid, cols, rows, threshold, cellW, cellH)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth * (1.5f - t * 0.5f)
                this.color = color
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * Expansive negative space with organic continuous contour waves
     */
    private fun renderSubtleBasin(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams
    ) {
        val levels = (12 * params.complexity).toInt().coerceIn(6, 18)
        val cols = 80
        val rows = (80 * (height / width)).toInt().coerceIn(100, 180)
        val grid = generateHeightfield(cols, rows, params, mode = 3)

        val cellW = width / (cols - 1)
        val cellH = height / (rows - 1)

        for (i in 1..levels) {
            val threshold = 0.20f + (i.toFloat() / levels) * 0.75f
            val color = params.palette.getColorAt(0.25f + (i.toFloat() / levels) * 0.70f).toArgb()

            val path = traceIsolinePath(grid, cols, rows, threshold, cellW, cellH)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth * 1.2f
                this.color = color
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * Dual geological peaks with continuous saddle contours
     */
    private fun renderDualPeakTopography(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams
    ) {
        val levels = (14 * params.complexity).toInt().coerceIn(8, 22)
        val cols = 80
        val rows = (80 * (height / width)).toInt().coerceIn(100, 180)
        val grid = generateHeightfield(cols, rows, params, mode = 4)

        val cellW = width / (cols - 1)
        val cellH = height / (rows - 1)

        for (i in 1..levels) {
            val threshold = i.toFloat() / (levels + 1)
            val color = params.palette.getColorAt(0.15f + threshold * 0.8f).toArgb()

            val path = traceIsolinePath(grid, cols, rows, threshold, cellW, cellH)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth * (if (i % 3 == 0) 1.6f else 1.0f)
                this.color = color
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * Generates a continuous, screen-filling 2D scalar field normalized to [0.0f .. 1.0f]
     */
    private fun generateHeightfield(
        cols: Int,
        rows: Int,
        params: WallpaperParams,
        mode: Int
    ): FloatArray {
        val grid = FloatArray(cols * rows)
        val seedOffset = (params.seed % 1000) * 0.15f
        val freq = 0.032f * params.scale
        val warp = params.distortion * 0.85f

        for (r in 0 until rows) {
            val y = r.toFloat()
            for (c in 0 until cols) {
                val x = c.toFloat()

                val nx = x * freq + seedOffset
                val ny = y * freq + seedOffset

                val h = when (mode) {
                    0 -> { // Multi-octave continuous continuous noise
                        val wX = x + sin(ny * 2.2f) * 14f * warp
                        val wY = y + cos(nx * 2.2f) * 14f * warp
                        val o1 = (sin(wX * freq) * cos(wY * freq) + 1f) * 0.5f
                        val o2 = (sin(wX * freq * 2.0f + 1.2f) * cos(wY * freq * 2.0f + 0.8f) + 1f) * 0.25f
                        val o3 = (sin(wX * freq * 4.0f) + 1f) * 0.125f
                        (o1 + o2 + o3) / 0.875f
                    }
                    1 -> { // Full continuous ridge lines
                        val wave1 = (sin(nx * 1.6f + ny * 0.8f) * cos(ny * 1.6f - nx * 0.5f) + 1f) * 0.5f
                        val wave2 = (sin(nx * 3.2f + 1.4f) * cos(ny * 3.2f + 0.9f) + 1f) * 0.25f
                        (wave1 * 0.75f + wave2).coerceIn(0f, 1f)
                    }
                    2 -> { // Oceanic diagonal trench
                        val diag = (x * 0.6f + y * 0.8f) * freq
                        val trench = (sin(diag) * cos(diag * 0.5f + nx * 0.4f) + 1f) * 0.5f
                        val sub = (sin(nx * 2.5f) * 0.15f * warp)
                        (trench + sub).coerceIn(0f, 1f)
                    }
                    3 -> { // Subtle continuous wave flow
                        val flow = (sin(nx * 1.4f + sin(ny * 1.8f) * warp) * cos(ny * 1.4f) + 1f) * 0.5f
                        val detail = (sin(nx * 3.5f + ny * 2.5f) + 1f) * 0.15f
                        (flow * 0.85f + detail).coerceIn(0f, 1f)
                    }
                    else -> { // Dual mountain peaks with continuous connecting saddle
                        val p1X = cols * 0.35f
                        val p1Y = rows * 0.38f
                        val p2X = cols * 0.68f
                        val p2Y = rows * 0.62f

                        val dx1 = (c - p1X) / (cols * 0.4f)
                        val dy1 = (r - p1Y) / (cols * 0.4f)
                        val d1 = sqrt(dx1 * dx1 + dy1 * dy1)

                        val dx2 = (c - p2X) / (cols * 0.4f)
                        val dy2 = (r - p2Y) / (cols * 0.4f)
                        val d2 = sqrt(dx2 * dx2 + dy2 * dy2)

                        val peak1 = (1f - d1).coerceAtLeast(0f)
                        val peak2 = (1f - d2).coerceAtLeast(0f)
                        val saddle = sin(nx * 1.8f + ny * 1.8f) * 0.18f * warp
                        (peak1 * 0.55f + peak2 * 0.55f + saddle + 0.1f).coerceIn(0f, 1f)
                    }
                }

                grid[r * cols + c] = h.coerceIn(0f, 1f)
            }
        }
        return grid
    }

    /**
     * Marching Squares segment collector constructing continuous vector paths across the viewport
     */
    private fun traceIsolinePath(
        grid: FloatArray,
        cols: Int,
        rows: Int,
        threshold: Float,
        cellW: Float,
        cellH: Float
    ): Path {
        val path = Path()

        for (r in 0 until rows - 1) {
            val rOffset = r * cols
            val rNextOffset = (r + 1) * cols
            val yTop = r * cellH
            val yBottom = (r + 1) * cellH

            for (c in 0 until cols - 1) {
                val xLeft = c * cellW
                val xRight = (c + 1) * cellW

                val tl = grid[rOffset + c]
                val tr = grid[rOffset + c + 1]
                val br = grid[rNextOffset + c + 1]
                val bl = grid[rNextOffset + c]

                var cellIndex = 0
                if (tl >= threshold) cellIndex = cellIndex or 8
                if (tr >= threshold) cellIndex = cellIndex or 4
                if (br >= threshold) cellIndex = cellIndex or 2
                if (bl >= threshold) cellIndex = cellIndex or 1

                if (cellIndex == 0 || cellIndex == 15) continue

                val topX = xLeft + cellW * interpolate(tl, tr, threshold)
                val rightY = yTop + cellH * interpolate(tr, br, threshold)
                val bottomX = xLeft + cellW * interpolate(bl, br, threshold)
                val leftY = yTop + cellH * interpolate(tl, bl, threshold)

                when (cellIndex) {
                    1, 14 -> { // bl
                        path.moveTo(xLeft, leftY)
                        path.lineTo(bottomX, yBottom)
                    }
                    2, 13 -> { // br
                        path.moveTo(bottomX, yBottom)
                        path.lineTo(xRight, rightY)
                    }
                    3, 12 -> { // bottom half
                        path.moveTo(xLeft, leftY)
                        path.lineTo(xRight, rightY)
                    }
                    4, 11 -> { // tr
                        path.moveTo(topX, yTop)
                        path.lineTo(xRight, rightY)
                    }
                    5 -> { // saddle
                        path.moveTo(xLeft, leftY)
                        path.lineTo(topX, yTop)
                        path.moveTo(bottomX, yBottom)
                        path.lineTo(xRight, rightY)
                    }
                    6, 9 -> { // right half or left half
                        path.moveTo(topX, yTop)
                        path.lineTo(bottomX, yBottom)
                    }
                    7, 8 -> { // tl
                        path.moveTo(xLeft, leftY)
                        path.lineTo(topX, yTop)
                    }
                    10 -> { // saddle opposite
                        path.moveTo(topX, yTop)
                        path.lineTo(xRight, rightY)
                        path.moveTo(xLeft, leftY)
                        path.lineTo(bottomX, yBottom)
                    }
                }
            }
        }
        return path
    }

    private fun interpolate(v1: Float, v2: Float, target: Float): Float {
        val diff = v2 - v1
        if (diff == 0f) return 0.5f
        return ((target - v1) / diff).coerceIn(0f, 1f)
    }
}
