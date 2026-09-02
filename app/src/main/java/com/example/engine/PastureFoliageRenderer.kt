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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.util.Random

/**
 * 5. Minimalist Pasture & Foliage Renderer (Reference Image 4)
 *
 * Smooth rolling hills with layered foreground, midground, and background depths.
 * Geometric minimalist flora: circular and elliptical foliage crowns atop straight vector stems
 * with minimal 45-degree geometric branch strokes.
 */
object PastureFoliageRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val rng = Random(params.seed)
        val colors = params.palette.colors

        // 1. Serene Sky Background
        val skyTop = colors.first().toArgb()
        val skyBot = if (colors.size > 2) colors[2].toArgb() else colors.first().toArgb()
        val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, height * 0.65f, skyTop, skyBot, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width, height, skyPaint)

        // 2. Minimalist Sun Disc in Sky
        val sunRadius = width * 0.12f
        val sunX = width * 0.75f
        val sunY = height * 0.22f
        val sunColor = colors.last().toArgb()
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = sunColor
        }
        canvas.drawCircle(sunX, sunY, sunRadius, sunPaint)

        // 3. Dispatch by sub-type
        when (params.subTypeIndex % 5) {
            0 -> renderRollingMeadowPines(canvas, width, height, params, rng)
            1 -> renderNordicOrchard(canvas, width, height, params, rng)
            2 -> renderTuscanCypress(canvas, width, height, params, rng)
            3 -> renderMinimalBirchGrove(canvas, width, height, params, rng)
            4 -> renderSunsetSavanna(canvas, width, height, params, rng)
        }
    }

    /**
     * Layered rolling hills with geometric minimalist pines and deciduous trees
     */
    private fun renderRollingMeadowPines(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val layerCount = (4 * params.complexity).toInt().coerceIn(3, 6)
        val startY = height * 0.45f
        val stepY = (height - startY) / layerCount

        for (l in 0 until layerCount) {
            val progress = l.toFloat() / layerCount
            val hillY = startY + l * stepY
            val hillColor = params.palette.getColorAt(0.2f + progress * 0.7f).toArgb()

            // Draw smooth rolling hill
            val hillPath = buildRollingHillPath(width, height, hillY, l, params)
            val hillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = params.lineWidth
                this.color = hillColor
            }
            canvas.drawPath(hillPath, hillPaint)

            // Plant geometric trees along the hill crest
            val treeCount = (3 + l * 2)
            val treeStepX = width / (treeCount + 1)
            for (t in 1..treeCount) {
                val tx = treeStepX * t + (rng.nextFloat() * 20f - 10f)
                val ty = sampleHillY(tx, width, hillY, l, params)

                val treeHeight = (height * 0.06f + l * 12f) * params.scale
                val treeCrownColor = params.palette.getColorAt(0.35f + progress * 0.6f).toArgb()

                if (t % 2 == 0) {
                    // Geometric Pine (stacked triangles)
                    drawGeometricPine(canvas, tx, ty, treeHeight, treeCrownColor, params)
                } else {
                    // Circular Deciduous (lollipop style)
                    drawDeciduousTree(canvas, tx, ty, treeHeight, treeCrownColor, params)
                }
            }
        }
    }

    /**
     * Clean staggered tree stems with concentric foliage circles and grid rhythm
     */
    private fun renderNordicOrchard(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val hills = 3
        val startY = height * 0.48f
        val stepY = (height - startY) / hills

        for (h in 0 until hills) {
            val prog = h.toFloat() / hills
            val hillY = startY + h * stepY
            val color = params.palette.getColorAt(0.25f + prog * 0.65f).toArgb()

            val hillPath = buildRollingHillPath(width, height, hillY, h, params)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = params.lineWidth
                this.color = color
            }
            canvas.drawPath(hillPath, fill)

            val trees = 4 + h * 2
            val treeStep = width / (trees + 1)
            for (t in 1..trees) {
                val tx = treeStep * t
                val ty = sampleHillY(tx, width, hillY, h, params)
                val treeH = (height * 0.08f + h * 10f) * params.scale
                val crownColor = params.palette.getColorAt(0.4f + prog * 0.55f).toArgb()

                // Straight trunk stem
                val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth * 1.5f
                    this.color = 0x88000000.toInt()
                }
                canvas.drawLine(tx, ty, tx, ty - treeH, trunkPaint)

                // Concentric foliage rings
                val crownRad = treeH * 0.38f
                val crownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                    strokeWidth = params.lineWidth
                    this.color = crownColor
                }
                canvas.drawCircle(tx, ty - treeH, crownRad, crownPaint)

                val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth
                    this.color = 0x44FFFFFF
                }
                canvas.drawCircle(tx, ty - treeH, crownRad * 0.6f, ringPaint)
            }
        }
    }

    /**
     * Tall slender flame-shaped cypress silhouettes against layered hill crests (Tuscan style)
     */
    private fun renderTuscanCypress(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val layers = 4
        val startY = height * 0.40f
        val stepY = (height - startY) / layers

        for (l in 0 until layers) {
            val prog = l.toFloat() / layers
            val hillY = startY + l * stepY
            val color = params.palette.getColorAt(0.2f + prog * 0.7f).toArgb()

            val hillPath = buildRollingHillPath(width, height, hillY, l, params)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = params.lineWidth
                this.color = color
            }
            canvas.drawPath(hillPath, fill)

            // Clustered slender cypress trees
            val clusters = 2 + l
            for (c in 0 until clusters) {
                val groupX = width * (0.15f + (c.toFloat() / clusters) * 0.7f) + (rng.nextFloat() * 40f - 20f)
                val inCluster = 2 + rng.nextInt(3)
                for (i in 0 until inCluster) {
                    val tx = groupX + (i * 16f)
                    val ty = sampleHillY(tx, width, hillY, l, params)
                    val treeH = (height * 0.12f + l * 14f) * (0.8f + rng.nextFloat() * 0.4f) * params.scale
                    val cColor = params.palette.getColorAt(0.15f + prog * 0.5f).toArgb()
                    drawCypressTree(canvas, tx, ty, treeH, cColor, params)
                }
            }
        }
    }

    /**
     * Slender straight trunks with 45-degree branch strokes and leaf dots
     */
    private fun renderMinimalBirchGrove(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val hillY = height * 0.75f
        val hillPath = buildRollingHillPath(width, height, hillY, 0, params)
        val hillColor = params.palette.getColorAt(0.3f).toArgb()
        val hillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = hillColor
        }
        canvas.drawPath(hillPath, hillPaint)

        val trunkCount = (8 * params.scale).toInt().coerceIn(5, 14)
        val stepX = width / (trunkCount + 1)

        for (i in 1..trunkCount) {
            val tx = stepX * i + (rng.nextFloat() * 30f - 15f)
            val ty = sampleHillY(tx, width, hillY, 0, params)
            val treeH = height * (0.35f + rng.nextFloat() * 0.15f)
            val trunkColor = params.palette.getColorAt(0.95f).toArgb()

            // Birch Trunk
            val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth * 2f
                this.color = trunkColor
            }
            canvas.drawLine(tx, ty, tx, ty - treeH, trunkPaint)

            // 45-degree geometric branches
            val branchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = params.lineWidth * 1.2f
                this.color = trunkColor
            }
            val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = params.palette.getColorAt(0.6f + (i % 3) * 0.15f).toArgb()
            }

            val branchCount = 4
            for (b in 1..branchCount) {
                val by = ty - treeH * (0.3f + (b.toFloat() / branchCount) * 0.6f)
                val isRight = (b % 2 == 0)
                val len = 25f * params.scale
                val endX = if (isRight) tx + len else tx - len
                val endY = by - len * 0.7f

                canvas.drawLine(tx, by, endX, endY, branchPaint)
                // Minimal leaf cluster dot
                canvas.drawCircle(endX, endY, 6f * params.scale, leafPaint)
            }
        }
    }

    /**
     * Broad flat-topped umbrella acacia trees against a sunset horizon
     */
    private fun renderSunsetSavanna(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val hillY = height * 0.68f
        val hillPath = buildRollingHillPath(width, height, hillY, 0, params)
        val hillColor = params.palette.getColorAt(0.2f).toArgb()
        val hillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = hillColor
        }
        canvas.drawPath(hillPath, hillPaint)

        // Hero Acacia Tree
        val tx = width * 0.38f
        val ty = sampleHillY(tx, width, hillY, 0, params)
        val treeH = height * 0.22f * params.scale
        val treeColor = params.palette.getColorAt(0.12f).toArgb()

        drawAcaciaTree(canvas, tx, ty, treeH, treeColor, params)

        // Secondary Acacia in background
        val tx2 = width * 0.78f
        val ty2 = sampleHillY(tx2, width, hillY, 0, params)
        val treeH2 = height * 0.14f * params.scale
        val treeColor2 = params.palette.getColorAt(0.28f).toArgb()
        drawAcaciaTree(canvas, tx2, ty2, treeH2, treeColor2, params)
    }

    private fun buildRollingHillPath(
        width: Float,
        height: Float,
        baseY: Float,
        layer: Int,
        params: WallpaperParams
    ): Path {
        val path = Path()
        path.moveTo(0f, height)
        path.lineTo(0f, sampleHillY(0f, width, baseY, layer, params))

        var x = 0f
        while (x <= width) {
            val y = sampleHillY(x, width, baseY, layer, params)
            path.lineTo(x, y)
            x += 16f
        }

        path.lineTo(width, height)
        path.close()
        return path
    }

    private fun sampleHillY(
        x: Float,
        width: Float,
        baseY: Float,
        layer: Int,
        params: WallpaperParams
    ): Float {
        val freq = 0.0035f * params.scale
        val phase = layer * 1.8f + (params.seed % 100) * 0.1f
        val wave1 = sin(x * freq + phase) * 35f * params.distortion
        val wave2 = cos(x * freq * 1.8f + phase * 0.7f) * 18f * params.distortion
        return baseY + wave1 + wave2
    }

    private fun drawGeometricPine(
        canvas: Canvas,
        tx: Float,
        ty: Float,
        treeHeight: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val trunkH = treeHeight * 0.25f
        val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = params.lineWidth * 1.2f
            this.color = 0x88000000.toInt()
        }
        canvas.drawLine(tx, ty, tx, ty - trunkH, trunkPaint)

        // 3 stacked geometric triangles
        val foliageH = treeHeight * 0.75f
        val tiers = 3
        val tierH = foliageH / tiers
        val maxW = treeHeight * 0.45f

        val foliagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }

        for (t in 0 until tiers) {
            val top = ty - trunkH - foliageH + (t * tierH * 0.7f)
            val bot = top + tierH * 1.3f
            val w = maxW * (0.5f + (t.toFloat() / tiers) * 0.5f)

            val p = Path().apply {
                moveTo(tx, top)
                lineTo(tx - w * 0.5f, bot)
                lineTo(tx + w * 0.5f, bot)
                close()
            }
            canvas.drawPath(p, foliagePaint)
        }
    }

    private fun drawDeciduousTree(
        canvas: Canvas,
        tx: Float,
        ty: Float,
        treeHeight: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val trunkH = treeHeight * 0.6f
        val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = params.lineWidth * 1.5f
            this.color = 0x88000000.toInt()
        }
        canvas.drawLine(tx, ty, tx, ty - trunkH, trunkPaint)

        val crownRadius = treeHeight * 0.35f
        val crownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }
        canvas.drawCircle(tx, ty - trunkH, crownRadius, crownPaint)
    }

    private fun drawCypressTree(
        canvas: Canvas,
        tx: Float,
        ty: Float,
        treeHeight: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val w = treeHeight * 0.18f
        val path = Path().apply {
            moveTo(tx, ty - treeHeight)
            cubicTo(tx + w, ty - treeHeight * 0.6f, tx + w * 0.8f, ty - treeHeight * 0.2f, tx, ty)
            cubicTo(tx - w * 0.8f, ty - treeHeight * 0.2f, tx - w, ty - treeHeight * 0.6f, tx, ty - treeHeight)
            close()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }
        canvas.drawPath(path, paint)
    }

    private fun drawAcaciaTree(
        canvas: Canvas,
        tx: Float,
        ty: Float,
        treeHeight: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = params.lineWidth * 2f
            this.color = color
        }
        // Y-shaped trunk
        val forkY = ty - treeHeight * 0.5f
        val crownY = ty - treeHeight * 0.85f
        canvas.drawLine(tx, ty, tx, forkY, trunkPaint)
        canvas.drawLine(tx, forkY, tx - treeHeight * 0.35f, crownY, trunkPaint)
        canvas.drawLine(tx, forkY, tx + treeHeight * 0.35f, crownY, trunkPaint)

        // Flat umbrella crown canopy
        val canopyW = treeHeight * 1.1f
        val canopyH = treeHeight * 0.22f
        val ovalRect = RectF(tx - canopyW * 0.5f, crownY - canopyH * 0.5f, tx + canopyW * 0.5f, crownY + canopyH * 0.5f)
        val canopyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
            strokeWidth = params.lineWidth
            this.color = color
        }
        canvas.drawOval(ovalRect, canopyPaint)
    }
}
