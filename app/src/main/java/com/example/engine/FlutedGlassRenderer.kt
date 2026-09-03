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
import kotlin.math.sin

/**
 * Fluted Reed Glass & Striated Pills Renderer.
 *
 * Implements staggered vertical stadium pillars with fine parallel vector
 * pinstripes (1dp stroke weight), soft layer opacity blending (0.5f -> 0.95f),
 * elevation drop shadows, and frosted refraction highlights.
 */
object FlutedGlassRenderer {

    fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)
        val rng = MathUtils.FastRandom(params.seed)

        val palette = params.palette
        val bgArgb = palette.colors.firstOrNull()?.toArgb() ?: 0xFF1E1B18.toInt()
        val bgSecond = palette.colors.getOrNull(1)?.toArgb() ?: bgArgb

        // Background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgArgb, bgSecond,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        val colors = palette.colors.map { it.toArgb() }
        val pillarCount = (4 + (params.complexity * 3).toInt()).coerceIn(3, 8)

        val pillarWidth = (width / (pillarCount + 1f)) * (1.2f * params.scale)
        val pinstripeSpacing = (8f * (width / 500f) * params.scale).coerceIn(4f, 20f)
        val strokeW = (params.lineWidth * (width / 600f)).coerceIn(1f, 3.5f)

        for (i in 0 until pillarCount) {
            val progress = i / (pillarCount - 1f).coerceAtLeast(1f)
            val cx = (0.15f + progress * 0.7f + (rng.nextFloat() - 0.5f) * 0.08f * params.distortion) * width
            
            // Staggered vertical heights and positions
            val staggerY = when (params.subTypeIndex % 5) {
                0 -> (sin(progress * Math.PI) * 0.2f - 0.1f).toFloat() // Wave cascade
                1 -> (progress * 0.25f - 0.12f)                      // Ascending
                2 -> if (i % 2 == 0) -0.1f else 0.1f                 // Alternating
                3 -> ((1f - progress) * 0.25f - 0.12f)               // Descending
                else -> (rng.nextFloat() - 0.5f) * 0.15f             // Organic drift
            }

            val cy = (0.5f + staggerY) * height
            val pillarHeight = height * (0.55f + (rng.nextFloat() * 0.35f))

            val halfW = pillarWidth / 2f
            val halfH = pillarHeight / 2f
            val left = cx - halfW
            val top = cy - halfH
            val right = cx + halfW
            val bottom = cy + halfH

            val color = colors[(i + 1) % colors.size]
            val opacity = 0.55f + (progress * 0.4f)

            val pillPath = Path().apply {
                val cornerR = halfW
                addRoundRect(RectF(left, top, right, bottom), cornerR, cornerR, Path.Direction.CW)
            }

            canvas.save()
            if (params.rotationDegrees != 0f) {
                canvas.rotate(params.rotationDegrees, cx, cy)
            }

            // Tactile drop shadow
            if (!params.isWireframe) {
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.TRANSPARENT
                    setShadowLayer(
                        28f,
                        0f,
                        14f,
                        AndroidColor.argb((0.32f * opacity * 255).toInt(), 0, 0, 0)
                    )
                }
                canvas.drawPath(pillPath, shadowPaint)
            }

            // Glass translucent fill
            val fillAlpha = (opacity * 210).toInt().coerceIn(0, 255)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = (color and 0x00FFFFFF) or (fillAlpha shl 24)
            }
            canvas.drawPath(pillPath, fillPaint)

            // Fine vertical pinstripe reed striations clipped to pill path
            canvas.save()
            canvas.clipPath(pillPath)

            val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeW
                val stripeAlpha = (opacity * 160).toInt().coerceIn(30, 240)
                this.color = AndroidColor.argb(stripeAlpha, 255, 255, 255)
            }

            var sx = left + pinstripeSpacing / 2f
            while (sx <= right) {
                canvas.drawLine(sx, top - 10f, sx, bottom + 10f, stripePaint)
                sx += pinstripeSpacing
            }

            // Glass specular rim outline
            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = 1.5f
                this.color = AndroidColor.argb((opacity * 100).toInt(), 255, 255, 255)
            }
            canvas.drawPath(pillPath, rimPaint)

            canvas.restore() // unclip
            canvas.restore() // unrotate
        }
    }
}
