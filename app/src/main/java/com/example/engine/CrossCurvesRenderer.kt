package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.max
import kotlin.math.min

/**
 * Minimalist Cross Curves Engine.
 * Renders intersecting bi-directional cubic Bezier ribbons, fluid woven bands,
 * and harmonious gradient curves crossing the viewport.
 */
object CrossCurvesRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val colors = params.palette.colors
        val baseDim = min(width, height)
        val strokeW = max(2f, params.lineWidth * (baseDim / 900f))

        val bgColor = colors.firstOrNull()?.let {
            Color.argb(255, (it.red * 255).toInt(), (it.green * 255).toInt(), (it.blue * 255).toInt())
        } ?: Color.rgb(25, 27, 32)
        canvas.drawColor(bgColor)

        val bandCount = (6f * params.scale * params.complexity).toInt().coerceIn(3, 16)
        val warp = params.distortion * 0.5f

        // Draw primary diagonal ribbons (Top-Left to Bottom-Right)
        for (i in 0 until bandCount) {
            val progress = i.toFloat() / bandCount.toFloat()
            val colorIdx = (i + 1) % colors.size
            val c = colors[colorIdx]
            val curveColor = Color.argb(
                if (params.isWireframe) 240 else 210,
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt()
            )

            val y1 = height * (progress - 0.2f)
            val y2 = height * (progress + 0.2f)

            val p = Path().apply {
                moveTo(0f, y1)
                cubicTo(
                    width * 0.35f, y1 + (height * warp),
                    width * 0.65f, y2 - (height * warp),
                    width, y2
                )
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = curveColor
                strokeWidth = strokeW * 2f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawPath(p, paint)
        }

        // Draw opposing cross-diagonal ribbons (Top-Right to Bottom-Left)
        for (i in 0 until bandCount) {
            val progress = i.toFloat() / bandCount.toFloat()
            val colorIdx = (i + 2) % colors.size
            val c = colors[colorIdx]
            val crossColor = Color.argb(
                if (params.isWireframe) 220 else 180,
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt()
            )

            val y1 = height * (progress - 0.15f)
            val y2 = height * (progress + 0.25f)

            val p = Path().apply {
                moveTo(width, y1)
                cubicTo(
                    width * 0.65f, y1 - (height * warp * 1.2f),
                    width * 0.35f, y2 + (height * warp * 1.2f),
                    0f, y2
                )
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = crossColor
                strokeWidth = strokeW * 2.2f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawPath(p, paint)
        }
    }
}
