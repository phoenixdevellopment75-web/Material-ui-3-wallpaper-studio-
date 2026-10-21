package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Mathematically Correct Generative Wallpaper Engines:
 * 1. MondrianBspGridsRenderer: 2D Binary Space Partition (BSP) tree generating 6-12 non-overlapping
 *    architectural rectangles with dark divider tracks and tonal palette fills.
 * 2. LayeredPaperRidgesRenderer: 4-6 smooth sine-harmonic horizontal wave curves spanning the lower 60%
 *    with atmospheric tonal layering and soft tactile ridge bevels.
 * 3. GeometricSlicesRenderer: 3-5 overlapping planar diagonal cuts (15°-45°) with 16dp filleted
 *    corners and 4dp cast elevation drop shadows.
 */
object NewGenerativeEngines {

    /**
     * Minimalist Mondrian Architectural Grids Engine (BSP Partition).
     * Splits canvas recursively into 6-12 disciplined non-overlapping rectangles.
     */
    object MondrianBspGridsRenderer : WallpaperRenderer {
        private data class BspNode(
            val rect: RectF,
            val depth: Int
        )

        override fun render(bitmap: Bitmap, params: WallpaperParams) {
            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()
            val canvas = Canvas(bitmap)

            val rng = Random(params.seed)
            val colors = params.palette.colors

            // Divider track color: Dark architectural charcoal or dark stop from palette
            val dividerColor = colors.firstOrNull()?.let {
                val darkR = (it.red * 255 * 0.18f).toInt()
                val darkG = (it.green * 255 * 0.18f).toInt()
                val darkB = (it.blue * 255 * 0.18f).toInt()
                Color.rgb(darkR, darkG, darkB)
            } ?: Color.rgb(0x1A, 0x1A, 0x1A)

            val baseDim = min(width, height)
            val dividerStrokeWidth = (params.lineWidth * 0.8f * (baseDim / 200f)).coerceIn(
                baseDim * 0.004f,
                baseDim * 0.016f
            )

            // Target 6 to 12 non-overlapping partitioned rectangles
            val targetPartitions = (6 + (params.complexity * 6).toInt()).coerceIn(6, 12)
            val partitions = generateBspRectangles(width, height, targetPartitions, rng)

            // Canvas background
            canvas.drawColor(dividerColor)

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = dividerColor
                strokeWidth = dividerStrokeWidth
                strokeCap = Paint.Cap.SQUARE
            }

            // Render filled rectangular subdivisions
            partitions.forEachIndexed { index, rect ->
                val colorStop = colors[(index + 1) % colors.size]
                // Modulate tonal lightness across cells
                val lightnessFactor = 0.65f + (rng.nextFloat() * 0.55f)
                val r = (colorStop.red * 255 * lightnessFactor).toInt().coerceIn(0, 255)
                val g = (colorStop.green * 255 * lightnessFactor).toInt().coerceIn(0, 255)
                val b = (colorStop.blue * 255 * lightnessFactor).toInt().coerceIn(0, 255)

                val cellColor = Color.rgb(r, g, b)
                fillPaint.color = cellColor

                // Inset by half the divider width so dark tracks frame each cell
                val halfTrack = dividerStrokeWidth / 2f
                val insetRect = RectF(
                    rect.left + halfTrack,
                    rect.top + halfTrack,
                    rect.right - halfTrack,
                    rect.bottom - halfTrack
                )

                if (!params.isWireframe) {
                    canvas.drawRect(insetRect, fillPaint)
                }

                // Architectural border framing
                canvas.drawRect(insetRect, strokePaint)
            }

            // Draw clean architectural cross divider tracks over all partition seams
            partitions.forEach { rect ->
                canvas.drawLine(rect.left, rect.top, rect.right, rect.top, strokePaint)
                canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, strokePaint)
                canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, strokePaint)
                canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, strokePaint)
            }
        }

        private fun generateBspRectangles(
            w: Float,
            h: Float,
            targetCount: Int,
            rng: Random
        ): List<RectF> {
            val leaves = mutableListOf<RectF>()
            val queue = ArrayDeque<RectF>()
            queue.add(RectF(0f, 0f, w, h))

            while (queue.isNotEmpty() && (leaves.size + queue.size) < targetCount) {
                val current = queue.removeFirst()
                val curW = current.width()
                val curH = current.height()

                val canSplitV = curW > w * 0.18f
                val canSplitH = curH > h * 0.18f

                if (!canSplitV && !canSplitH) {
                    leaves.add(current)
                    continue
                }

                val splitVertical = when {
                    canSplitV && !canSplitH -> true
                    !canSplitV && canSplitH -> false
                    curW / curH > 1.35f -> true
                    curH / curW > 1.35f -> false
                    else -> rng.nextBoolean()
                }

                val splitRatio = 0.35f + (rng.nextFloat() * 0.30f)

                if (splitVertical) {
                    val splitX = current.left + curW * splitRatio
                    val leftRect = RectF(current.left, current.top, splitX, current.bottom)
                    val rightRect = RectF(splitX, current.top, current.right, current.bottom)
                    queue.add(leftRect)
                    queue.add(rightRect)
                } else {
                    val splitY = current.top + curH * splitRatio
                    val topRect = RectF(current.left, current.top, current.right, splitY)
                    val botRect = RectF(current.left, splitY, current.right, current.bottom)
                    queue.add(topRect)
                    queue.add(botRect)
                }
            }

            leaves.addAll(queue)
            return leaves
        }
    }

    /**
     * Layered Paper Ridges Engine (Harmonic Wave Horizons).
     * Renders 4-6 smooth sinusoidal layered curves across the lower 60% of the screen
     * with atmospheric tonal gradation and soft paper edge bevels.
     */
    object LayeredPaperRidgesRenderer : WallpaperRenderer {

        override fun render(bitmap: Bitmap, params: WallpaperParams) {
            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()
            val canvas = Canvas(bitmap)

            val rng = Random(params.seed)
            val colors = params.palette.colors

            // Base sky/backdrop color: Lightest or atmospheric primary
            val skyColor = colors.firstOrNull()?.let {
                Color.argb(255, (it.red * 255).toInt(), (it.green * 255).toInt(), (it.blue * 255).toInt())
            } ?: Color.rgb(240, 237, 230)

            canvas.drawColor(skyColor)

            // 4 to 6 gentle wavy horizon ridges across lower 60% of the canvas
            val layerCount = (4 + (params.complexity * 2).toInt()).coerceIn(4, 6)
            val startYFraction = 0.36f // Top ridge begins at 36-40% height
            val verticalSpan = height * 0.60f
            val layerSpacing = verticalSpan / (layerCount + 0.5f)

            val baseDim = min(width, height)
            val strokeW = max(1.5f, params.lineWidth * (baseDim / 1000f))

            val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = if (params.isWireframe) Paint.Style.STROKE else Paint.Style.FILL
                strokeWidth = strokeW
            }

            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = strokeW * 2f
            }

            // Draw ridges from back (highest y0, lightest tonal) to front (lowest y0, deepest tonal)
            for (i in 0 until layerCount) {
                val t = i.toFloat() / (layerCount - 1).coerceAtLeast(1)
                val y0 = (height * startYFraction) + (i * layerSpacing)

                // Harmonics parameters: y(x) = y0 + A1*sin(k1*x + phi1) + A2*sin(k2*x + phi2)
                val a1 = height * (0.025f + 0.020f * (1f - t)) * (0.8f + params.scale * 0.4f)
                val a2 = height * 0.012f * (0.8f + params.distortion * 0.4f)
                val k1 = (2.0 * PI / (width * (0.8f + 0.3f * rng.nextFloat()))).toFloat()
                val k2 = (4.0 * PI / (width * (0.9f + 0.4f * rng.nextFloat()))).toFloat()
                val phi1 = rng.nextFloat() * 2f * PI.toFloat()
                val phi2 = rng.nextFloat() * 2f * PI.toFloat()

                val ridgePath = Path()
                val sampleStep = 12f

                var x = 0f
                val firstY = y0 + a1 * sin(k1 * x + phi1) + a2 * sin(k2 * x + phi2)
                ridgePath.moveTo(x, firstY)

                while (x <= width + sampleStep) {
                    val y = y0 + a1 * sin(k1 * x + phi1) + a2 * sin(k2 * x + phi2)
                    ridgePath.lineTo(x, y)
                    x += sampleStep
                }

                // Close path downwards across canvas bottom
                ridgePath.lineTo(width, height)
                ridgePath.lineTo(0f, height)
                ridgePath.close()

                // Atmospheric tonal depth: progress through palette colors with subtle darkening
                val colorIdx = ((i + 1) % colors.size)
                val c = colors[colorIdx]
                val toneMultiplier = 0.95f - (t * 0.28f) // Tonal depth ramp
                val layerColor = Color.rgb(
                    (c.red * 255 * toneMultiplier).toInt().coerceIn(0, 255),
                    (c.green * 255 * toneMultiplier).toInt().coerceIn(0, 255),
                    (c.blue * 255 * toneMultiplier).toInt().coerceIn(0, 255)
                )

                if (!params.isWireframe) {
                    // Soft paper elevation shadow underneath ridge edge
                    shadowPaint.setShadowLayer(
                        10f + (t * 6f),
                        0f,
                        4f + (t * 3f),
                        Color.argb((45 + t * 40).toInt(), 15, 15, 20)
                    )
                    canvas.drawPath(ridgePath, shadowPaint)

                    layerPaint.color = layerColor
                    canvas.drawPath(ridgePath, layerPaint)

                    // Subtle highlight along ridge crest
                    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = max(1f, strokeW * 0.75f)
                        color = Color.argb(40, 255, 255, 255)
                    }
                    canvas.drawPath(ridgePath, highlightPaint)
                } else {
                    layerPaint.color = layerColor
                    canvas.drawPath(ridgePath, layerPaint)
                }
            }
        }
    }

    /**
     * Planar Geometric Slices Engine.
     * Renders 3-5 overlapping diagonal polygons across the canvas at angles between 15° and 45°,
     * with 16dp filleted intersection vertices and 4dp cast elevation drop shadows.
     */
    object GeometricSlicesRenderer : WallpaperRenderer {

        override fun render(bitmap: Bitmap, params: WallpaperParams) {
            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()
            val canvas = Canvas(bitmap)

            val rng = Random(params.seed)
            val colors = params.palette.colors
            val baseDim = min(width, height)

            // Base background
            val baseColor = colors.firstOrNull()?.let {
                Color.rgb((it.red * 255).toInt(), (it.green * 255).toInt(), (it.blue * 255).toInt())
            } ?: Color.rgb(28, 30, 36)
            canvas.drawColor(baseColor)

            // 3 to 5 overlapping diagonal polygons
            val sliceCount = (3 + (params.complexity * 2).toInt()).coerceIn(3, 5)
            // Angle between 15° and 45°
            val angleDeg = 15f + (params.distortion * 30f).coerceIn(0f, 30f)
            val angleRad = (angleDeg * PI / 180f).toFloat()
            val slope = kotlin.math.tan(angleRad)

            val strokeW = max(1.5f, params.lineWidth * (baseDim / 1000f))
            // 16dp filleted corner radius
            val filletedRadiusPx = (16f * (baseDim / 360f)).coerceAtLeast(8f)

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                pathEffect = CornerPathEffect(filletedRadiusPx)
            }

            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeW * 3f
                pathEffect = CornerPathEffect(filletedRadiusPx)
                setShadowLayer(
                    (8f * (baseDim / 360f)).coerceAtLeast(4f),
                    0f,
                    (4f * (baseDim / 360f)).coerceAtLeast(3f),
                    Color.argb(90, 0, 0, 0)
                )
            }

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeW
                pathEffect = CornerPathEffect(filletedRadiusPx)
            }

            val stepY = (height + (width * slope)) / (sliceCount + 1f)

            // Draw overlapping diagonal slices from top to bottom
            for (i in 1..sliceCount) {
                val yStart = (i * stepY) - (width * slope * 0.4f)
                val yEnd = yStart + (width * slope)

                val slicePath = Path().apply {
                    moveTo(-10f, yStart)
                    lineTo(width + 10f, yEnd)
                    lineTo(width + 10f, height + 10f)
                    lineTo(-10f, height + 10f)
                    close()
                }

                val colorIdx = i % colors.size
                val c = colors[colorIdx]
                val sliceColor = Color.rgb(
                    (c.red * 255).toInt(),
                    (c.green * 255).toInt(),
                    (c.blue * 255).toInt()
                )

                if (!params.isWireframe) {
                    // 4dp cast elevation drop shadow
                    canvas.drawPath(slicePath, shadowPaint)
                    fillPaint.color = sliceColor
                    canvas.drawPath(slicePath, fillPaint)
                }

                strokePaint.color = Color.argb(40, 255, 255, 255)
                canvas.drawPath(slicePath, strokePaint)
            }
        }
    }
}
