package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 3. Minimalist Desert Dunes & Horizon Shadows (Reference Image 3)
 *
 * Clean parametric cubic Bézier curves representing overlapping ridgelines.
 * Chiaroscuro light/shadow division: one side of the dune crest rendered in a sunlit tone,
 * the other falling into smooth tonal shade.
 */
object DuneRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val colors = palette.colors

        // 1. Warm Sky & Horizon Banding
        val skyTop = colors.first().toArgb()
        val skyHorizon = if (colors.size > 2) colors[2].toArgb() else colors.first().toArgb()
        val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f, 0f,
                0f, height * 0.65f,
                skyTop, skyHorizon,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, skyPaint)

        // 2. Minimalist Sun Disc & Glow
        renderDesertSun(canvas, width, height, params)

        // 3. Sweeping Dune Ribbons (Parametric Bézier Sweeps)
        val duneCount = when (params.subTypeIndex % 5) {
            0 -> (5 * params.complexity).toInt().coerceIn(4, 8) // Sahara Crests
            1 -> (6 * params.complexity).toInt().coerceIn(4, 9) // Erg Ribbons
            2 -> (4 * params.complexity).toInt().coerceIn(3, 7) // Golden Hour Dunes
            3 -> (5 * params.complexity).toInt().coerceIn(4, 8) // Wind Ripples & Crests
            4 -> (4 * params.complexity).toInt().coerceIn(3, 6) // Sunset Dune Horizon
            else -> 5
        }

        val startY = height * 0.36f
        val duneStepY = (height - startY) / duneCount

        for (i in 0 until duneCount) {
            val progress = i.toFloat() / duneCount
            val baseDuneY = startY + i * duneStepY
            val seedOffset = params.seed + i * 1337L
            val layerRand = MathUtils.FastRandom(seedOffset)

            // Dynamic color stops: light facet vs shadow facet
            val lightColor = palette.getColorAt(0.35f + progress * 0.60f).toArgb()
            val shadowColor = palette.getColorAt(0.10f + progress * 0.50f).toArgb()

            // Control points for sweeping cubic Bézier dune crest
            val p0x = 0f
            val p0y = baseDuneY + (layerRand.nextFloat(-25f, 25f) * params.scale)

            val p1x = width * 0.33f
            val p1y = baseDuneY + (layerRand.nextFloat(-70f, 70f) * params.scale)

            val p2x = width * 0.66f
            val p2y = baseDuneY + (layerRand.nextFloat(-70f, 70f) * params.scale)

            val p3x = width
            val p3y = baseDuneY + (layerRand.nextFloat(-35f, 35f) * params.scale)

            // 1. Overall Dune Body Path
            val duneBodyPath = Path().apply {
                moveTo(0f, height)
                lineTo(p0x, p0y)
                cubicTo(p1x, p1y, p2x, p2y, p3x, p3y)
                lineTo(width, height)
                close()
            }

            // Fill with Sunlit Tone
            val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f, baseDuneY - 40f,
                    width, baseDuneY + 140f,
                    lightColor,
                    shadowColor,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(duneBodyPath, lightPaint)

            // 2. Chiaroscuro Shadow Facet (Sharp ridge dividing sunlit and shadowed face)
            val ridgeApexX = (p1x + p2x) * 0.5f + layerRand.nextFloat(-width * 0.08f, width * 0.08f)
            val ridgeApexY = (p1y + p2y) * 0.5f

            val shadowPath = Path().apply {
                moveTo(ridgeApexX, ridgeApexY)
                // Curve following the ridge line down into the valley
                cubicTo(
                    ridgeApexX + width * 0.15f, ridgeApexY + 40f,
                    width * 0.85f, baseDuneY + 100f,
                    width, height
                )
                lineTo(ridgeApexX - width * 0.1f, height)
                close()
            }

            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = (shadowColor and 0x00FFFFFF) or (0x99 shl 24)
            }
            canvas.drawPath(shadowPath, shadowPaint)

            // 3. Wind Ripple Texturing along slope
            if (params.subTypeIndex % 5 == 3 || params.complexity > 1.3f) {
                renderWindRipples(canvas, width, baseDuneY, lightColor, params)
            }

            // 4. Sharp Crest Ridge Highlight Stroke
            if (params.lineWidth > 0.4f || params.isWireframe) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth * (1.1f - progress * 0.35f)
                    color = if (params.isWireframe) lightColor else 0x44FFFFFF.toInt()
                }
                canvas.drawPath(duneBodyPath, strokePaint)
            }
        }
    }

    private fun renderDesertSun(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams
    ) {
        val colors = params.palette.colors
        val sunColor = colors.last().toArgb()
        val cx = width * 0.72f
        val cy = height * 0.28f
        val radius = min(width, height) * 0.13f

        // Soft Atmospheric Sunset Glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx, cy, radius * 3.2f,
                intArrayOf(
                    (sunColor and 0x00FFFFFF) or (0x66 shl 24),
                    (sunColor and 0x00FFFFFF) or (0x18 shl 24),
                    0x00000000
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius * 3.2f, glowPaint)

        // Geometric Sun Disc
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = sunColor
        }
        canvas.drawCircle(cx, cy, radius, sunPaint)

        // Minimalist Orbit Ring Accent
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = params.lineWidth
            color = 0x2EFFFFFF
        }
        canvas.drawCircle(cx, cy, radius * 1.5f, ringPaint)
    }

    private fun renderWindRipples(
        canvas: Canvas,
        width: Float,
        baseY: Float,
        color: Int,
        params: WallpaperParams
    ) {
        val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.0f * params.lineWidth
            this.color = (color and 0x00FFFFFF) or (0x26 shl 24)
        }

        val step = 16f * (1.5f - params.complexity * 0.3f)
        var y = baseY + 12f
        while (y < baseY + 100f) {
            val ripplePath = Path().apply {
                moveTo(0f, y)
                var x = 0f
                while (x <= width) {
                    val wave = sin(x * 0.04f + y * 0.08f) * 2.5f
                    lineTo(x, y + wave)
                    x += 20f
                }
            }
            canvas.drawPath(ripplePath, ripplePaint)
            y += step
        }
    }
}
