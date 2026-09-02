package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import kotlin.math.min
import kotlin.math.sin
import java.util.Random

/**
 * 1. Material Nested Arches & Pills Renderer
 *
 * True concentric stadium/pill arches with precise geometric tangency (upper semi-circle seamlessly
 * tangent to parallel vertical legs extending off the bottom edge).
 * Simulated ambient elevation depth: each inner arch casts a soft, blurred linear and radial shadow onto
 * the layer directly behind it using android.graphics.Paint.setShadowLayer().
 * Strictly monotonic analogous tonal ramps eliminating muddy transitions.
 */
object NestedArchesRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val rng = Random(params.seed)
        val colors = params.palette.colors

        // 1. Background Fill (Softest / deepest base tone from monotonic palette)
        val bgArgb = colors.first().toArgb()
        val bgEndArgb = colors[min(1, colors.size - 1)].toArgb()
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgArgb, bgEndArgb, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Dispatch by sub-type
        when (params.subTypeIndex % 5) {
            0 -> renderConcentricPortal(canvas, width, height, params, rng)
            1 -> renderStaggeredColonnade(canvas, width, height, params, rng)
            2 -> renderAsymmetricBauhaus(canvas, width, height, params, rng)
            3 -> renderInvertedReflection(canvas, width, height, params, rng)
            4 -> renderModernistCascade(canvas, width, height, params, rng)
        }
    }

    /**
     * Classic centered concentric arch portal nesting with simulated ambient elevation shadows
     */
    private fun renderConcentricPortal(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val centerX = width * 0.5f
        val bottomY = height + 10f // extend cleanly past bottom edge
        val layerCount = (7 * params.complexity).toInt().coerceIn(4, 12)
        val maxRadius = (width * 0.44f * params.scale).coerceAtMost(width * 0.48f)

        // Background subtle ambient celestial sun disc
        val sunRadius = maxRadius * 0.40f
        val sunCenterY = height * 0.26f

        // Sun ambient shadow
        if (!params.isWireframe) {
            val sunShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = 0x2A000000
                setShadowLayer(24f * params.scale, 0f, 8f, 0x33000000)
            }
            canvas.drawCircle(centerX, sunCenterY, sunRadius + 2f, sunShadow)
        }

        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = params.palette.getColorAt(0.85f).toArgb()
        }
        canvas.drawCircle(centerX, sunCenterY, sunRadius, sunPaint)

        // Delicate inner sun disc
        val innerSunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = params.palette.getColorAt(0.96f).toArgb()
        }
        canvas.drawCircle(centerX, sunCenterY, sunRadius * 0.5f, innerSunPaint)

        val archTopBaseY = height * 0.40f

        for (i in 0 until layerCount) {
            val progress = i.toFloat() / (layerCount - 1).coerceAtLeast(1)
            // Precise geometric progression with linear radius decay
            val radius = maxRadius * (1f - progress * 0.78f)
            val archTopY = archTopBaseY + (i * 26f * params.scale)

            // Monotonic 8-12% lightness step progression
            val color = params.palette.getColorAt(0.12f + progress * 0.85f).toArgb()

            val path = buildArchPath(centerX, archTopY, radius, bottomY)

            if (params.isWireframe) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth * (1.2f - progress * 0.3f)
                    this.color = color
                }
                canvas.drawPath(path, strokePaint)
            } else {
                // Multi-pass soft shadow layer cast on the surface behind
                if (i > 0) {
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        this.color = 0x1E000000
                        setShadowLayer(18f * params.scale, 0f, 6f, 0x38000000)
                    }
                    val shadowPath = buildArchPath(centerX, archTopY - 3f, radius + 4f, bottomY)
                    canvas.drawPath(shadowPath, shadowPaint)
                }

                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = color
                }
                canvas.drawPath(path, fillPaint)

                // Elegant high-precision hairline border stroke
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth.coerceAtMost(1.5f)
                    this.color = 0x22FFFFFF
                }
                canvas.drawPath(path, borderPaint)
            }
        }
    }

    /**
     * Multi-column colonnade of nested arch pills with soft elevation
     */
    private fun renderStaggeredColonnade(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val columnCount = 3
        val colWidth = width / columnCount
        val bottomY = height + 10f

        for (col in 0 until columnCount) {
            val cx = colWidth * (col + 0.5f)
            val heightOffset = (sin(col * 1.5f) * height * 0.08f)
            val baseTopY = height * (0.32f + col * 0.08f) + heightOffset
            val maxColRadius = colWidth * 0.44f * params.scale
            val layers = (4 * params.complexity).toInt().coerceIn(3, 7)

            for (l in 0 until layers) {
                val t = l.toFloat() / (layers - 1).coerceAtLeast(1)
                val rad = maxColRadius * (1f - t * 0.72f)
                val topY = baseTopY + (l * 28f)
                val color = params.palette.getColorAt((col * 0.20f + t * 0.70f) % 1.0f).toArgb()

                val path = buildArchPath(cx, topY, rad, bottomY)

                if (params.isWireframe) {
                    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = params.lineWidth
                        this.color = color
                    }
                    canvas.drawPath(path, stroke)
                } else {
                    if (l > 0) {
                        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                            this.color = 0x1A000000
                            setShadowLayer(14f * params.scale, 0f, 4f, 0x30000000)
                        }
                        val sp = buildArchPath(cx, topY - 2f, rad + 3f, bottomY)
                        canvas.drawPath(sp, shadow)
                    }

                    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        this.color = color
                    }
                    canvas.drawPath(path, fill)

                    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = params.lineWidth.coerceAtMost(1.2f)
                        this.color = 0x1AFFFFFF
                    }
                    canvas.drawPath(path, border)
                }
            }
        }
    }

    /**
     * Asymmetric Bauhaus geometric composition with offset arch and celestial elements
     */
    private fun renderAsymmetricBauhaus(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val offsetX = width * (0.40f + (params.seed % 100) * 0.001f)
        val archTopY = height * 0.28f
        val maxRad = width * 0.52f * params.scale
        val bottomY = height + 10f
        val steps = (7 * params.complexity).toInt().coerceIn(4, 10)

        // Large solid floating accent moon with soft shadow
        val moonX = width * 0.76f
        val moonY = height * 0.22f
        val moonRad = width * 0.16f

        if (!params.isWireframe) {
            val moonShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = 0x22000000
                setShadowLayer(20f * params.scale, 0f, 6f, 0x2E000000)
            }
            canvas.drawCircle(moonX, moonY, moonRad, moonShadow)
        }

        val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = params.palette.getColorAt(0.85f).toArgb()
        }
        canvas.drawCircle(moonX, moonY, moonRad, moonPaint)

        // Horizontal geometric datum line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = params.lineWidth * 1.5f
            this.color = params.palette.getColorAt(0.95f).toArgb()
        }
        canvas.drawLine(0f, height * 0.55f, width, height * 0.55f, linePaint)

        for (i in 0 until steps) {
            val frac = i.toFloat() / (steps - 1).coerceAtLeast(1)
            val rad = maxRad * (1f - frac * 0.82f)
            val topY = archTopY + (i * 32f * params.scale)
            val color = params.palette.getColorAt(0.12f + frac * 0.85f).toArgb()

            val archPath = buildArchPath(offsetX, topY, rad, bottomY)

            if (params.isWireframe) {
                val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth
                    this.color = color
                }
                canvas.drawPath(archPath, stroke)
            } else {
                if (i > 0) {
                    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        this.color = 0x20000000
                        setShadowLayer(16f * params.scale, 0f, 5f, 0x33000000)
                    }
                    val sp = buildArchPath(offsetX, topY - 3f, rad + 4f, bottomY)
                    canvas.drawPath(sp, shadow)
                }

                val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = color
                }
                canvas.drawPath(archPath, fill)

                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth.coerceAtMost(1.2f)
                    this.color = 0x1AFFFFFF
                }
                canvas.drawPath(archPath, border)
            }
        }
    }

    /**
     * Top and bottom reflecting nested arch portals
     */
    private fun renderInvertedReflection(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val cx = width * 0.5f
        val centerY = height * 0.5f
        val radMax = width * 0.38f * params.scale
        val steps = (5 * params.complexity).toInt().coerceIn(3, 8)

        // Center connecting diamond or ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = params.lineWidth * 1.5f
            color = params.palette.getColorAt(0.9f).toArgb()
        }
        canvas.drawCircle(cx, centerY, radMax * 0.4f, ringPaint)

        // Bottom arch expanding up
        for (i in 0 until steps) {
            val f = i.toFloat() / (steps - 1).coerceAtLeast(1)
            val r = radMax * (1f - f * 0.75f)
            val topY = centerY + (i * 20f)
            val color = params.palette.getColorAt(0.18f + f * 0.75f).toArgb()

            val path = buildArchPath(cx, topY, r, height + 10f)

            if (!params.isWireframe && i > 0) {
                val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = 0x1A000000
                    setShadowLayer(12f * params.scale, 0f, 4f, 0x2A000000)
                }
                val sp = buildArchPath(cx, topY - 2f, r + 3f, height + 10f)
                canvas.drawPath(sp, shadow)
            }

            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = params.lineWidth
                this.color = color
            }
            canvas.drawPath(path, fill)
        }

        // Top arch expanding down (inverted)
        for (i in 0 until steps) {
            val f = i.toFloat() / (steps - 1).coerceAtLeast(1)
            val r = radMax * (1f - f * 0.75f)
            val botY = centerY - (i * 20f)
            val color = params.palette.getColorAt(0.92f - f * 0.75f).toArgb()

            val path = buildInvertedArchPath(cx, botY, r, -10f)

            if (!params.isWireframe && i > 0) {
                val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = 0x1A000000
                    setShadowLayer(12f * params.scale, 0f, -4f, 0x2A000000)
                }
                val sp = buildInvertedArchPath(cx, botY + 2f, r + 3f, -10f)
                canvas.drawPath(sp, shadow)
            }

            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = params.lineWidth
                this.color = color
            }
            canvas.drawPath(path, fill)
        }
    }

    /**
     * Modernist cascading staircase of nested arches with step offsets
     */
    private fun renderModernistCascade(
        canvas: Canvas,
        width: Float,
        height: Float,
        params: WallpaperParams,
        rng: Random
    ) {
        val steps = (6 * params.complexity).toInt().coerceIn(4, 9)
        val maxRad = width * 0.36f * params.scale
        val bottomY = height + 10f

        for (i in 0 until steps) {
            val f = i.toFloat() / (steps - 1).coerceAtLeast(1)
            val cx = width * (0.28f + f * 0.44f)
            val topY = height * (0.24f + f * 0.28f)
            val rad = maxRad * (1f - f * 0.45f)
            val color = params.palette.getColorAt(0.15f + f * 0.82f).toArgb()

            val path = buildArchPath(cx, topY, rad, bottomY)

            if (!params.isWireframe && i > 0) {
                val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = 0x1E000000
                    setShadowLayer(16f * params.scale, 0f, 5f, 0x30000000)
                }
                val sp = buildArchPath(cx, topY - 3f, rad + 4f, bottomY)
                canvas.drawPath(sp, shadow)
            }

            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = params.lineWidth
                this.color = color
            }
            canvas.drawPath(path, p)

            if (!params.isWireframe) {
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth.coerceAtMost(1.2f)
                    this.color = 0x1AFFFFFF
                }
                canvas.drawPath(path, border)
            }
        }
    }

    /**
     * Builds a geometrically precise upward stadium arch: semi-circle at top, vertical straight legs extending down to [bottomY]
     */
    private fun buildArchPath(centerX: Float, topY: Float, radius: Float, bottomY: Float): Path {
        val path = Path()
        val left = centerX - radius
        val right = centerX + radius
        val arcBottom = topY + radius * 2f

        path.moveTo(left, bottomY)
        path.lineTo(left, topY + radius)
        val arcRect = RectF(left, topY, right, arcBottom)
        path.arcTo(arcRect, 180f, 180f, false)
        path.lineTo(right, bottomY)
        path.close()
        return path
    }

    /**
     * Builds an inverted downward arch: semi-circle at bottom, vertical legs extending up to [topY]
     */
    private fun buildInvertedArchPath(centerX: Float, bottomY: Float, radius: Float, topY: Float): Path {
        val path = Path()
        val left = centerX - radius
        val right = centerX + radius
        val arcTop = bottomY - radius * 2f

        path.moveTo(left, topY)
        path.lineTo(left, bottomY - radius)
        val arcRect = RectF(left, arcTop, right, bottomY)
        path.arcTo(arcRect, 180f, -180f, false)
        path.lineTo(right, topY)
        path.close()
        return path
    }
}
