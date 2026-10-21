package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Shape Vector Engine & Procedural Assembler.
 *
 * Implements the official Material 3 Expressive shape token library:
 * 1. 4-Leaf & 8-Leaf Clover (tangent filleted lobes)
 * 2. M3 Scallop (6-sided, 8-sided, and 12-sided cookie badges via clamped polar harmonics)
 * 3. M3 Arch & Semicircle (pure tangent rounded caps with vertical drop walls)
 * 4. Puffy Diamond & Gem (filleted convex/concave polygonal edges)
 * 5. Bun & Stadium Pill (smooth, unwarped elongated curves)
 * 6. Slanted Squircle & Pebble (organic asymmetrical continuous curvature)
 * 7. Circle & Torus Ring
 *
 * Enforces uniform 1:1 aspect ratio scaling to prevent shape warping/squeezing.
 */
object ProceduralM3Assembler {

    private const val PHI_1 = 0.381966f
    private const val PHI_2 = 0.618034f
    private const val PHI_3 = 0.236068f
    private const val PHI_4 = 0.763932f

    // Reusable Paint objects to achieve zero-allocation rendering per frame
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = AndroidColor.TRANSPARENT
    }
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        color = AndroidColor.argb(35, 255, 255, 255)
    }

    private sealed class RenderItem(val z: Int) {
        class ShapeItem(val shape: CustomCanvasShape) : RenderItem(shape.zIndex)
        class TextItem(val text: StudioTextLayer) : RenderItem(text.zIndex)
        class ImageItem(val image: StudioImageLayer) : RenderItem(image.zIndex)
    }

    /**
     * Renders user-composed shapes on the Studio canvas with 1:1 proportional scaling.
     */
    fun renderCustomShapes(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val bgArgb = palette.colors.firstOrNull()?.toArgb() ?: 0xFF1C1B1F.toInt()
        val bgSecond = palette.colors.getOrNull(1)?.toArgb() ?: bgArgb

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height,
                bgArgb, bgSecond,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        val colors = palette.colors.map { it.toArgb() }
        // Base reference dimension for 1:1 scaling
        val baseDim = minOf(width, height)

        // Unified rendering queue sorted by zIndex
        val renderQueue = (params.customShapes.map { RenderItem.ShapeItem(it) } +
                params.customTexts.map { RenderItem.TextItem(it) } +
                params.customImages.map { RenderItem.ImageItem(it) }).sortedBy { it.z }

        for (item in renderQueue) {
            when (item) {
                is RenderItem.ShapeItem -> {
                    val shape = item.shape
                    val cx = shape.normalizedX * width
                    val cy = shape.normalizedY * height

                    val scaleFactor = params.scale * baseDim
                    val w = shape.normalizedWidth * scaleFactor
                    val h = if (shape.lockProportions || shape.type == CustomShapeType.CIRCLE) {
                        w
                    } else {
                        shape.normalizedHeight * scaleFactor
                    }

                    val color = if (shape.customColorHex != null) {
                        shape.customColorHex.toInt()
                    } else {
                        colors[shape.colorIndex % colors.size]
                    }

                    drawSingleShape(
                        canvas = canvas,
                        type = shape.type,
                        cx = cx,
                        cy = cy,
                        w = w,
                        h = h,
                        rotationDeg = shape.rotationDeg + params.rotationDegrees,
                        color = color,
                        opacity = shape.opacity,
                        isWireframe = shape.isWireframe || params.isWireframe,
                        strokeWidth = shape.strokeWidth * (baseDim / 500f),
                        scallopLobes = shape.scallopLobes,
                        castShadow = true,
                        shadowRadius = shape.shadowRadius,
                        blurRadius = shape.blurRadius
                    )
                }
                is RenderItem.TextItem -> {
                    val textLayer = item.text
                    val cx = textLayer.normalizedX * width
                    val cy = textLayer.normalizedY * height
                    val fontSizePx = textLayer.normalizedSize * baseDim * params.scale
                    val color = if (textLayer.customColorHex != null) {
                        textLayer.customColorHex.toInt()
                    } else {
                        colors[textLayer.colorIndex % colors.size]
                    }

                    drawSingleText(
                        canvas = canvas,
                        textLayer = textLayer,
                        cx = cx,
                        cy = cy,
                        fontSizePx = fontSizePx,
                        color = color,
                        rotationDeg = textLayer.rotationDeg + params.rotationDegrees
                    )
                }
                is RenderItem.ImageItem -> {
                    val imgLayer = item.image
                    val cx = imgLayer.normalizedX * width
                    val cy = imgLayer.normalizedY * height
                    val scaleFactor = params.scale * baseDim
                    val w = imgLayer.normalizedWidth * scaleFactor
                    val h = if (imgLayer.lockProportions) {
                        w
                    } else {
                        imgLayer.normalizedHeight * scaleFactor
                    }

                    val bmp = imgLayer.cachedBitmap ?: runCatching {
                        if (java.io.File(imgLayer.imagePath).exists()) {
                            android.graphics.BitmapFactory.decodeFile(imgLayer.imagePath)
                        } else null
                    }.getOrNull()

                    if (bmp != null) {
                        StudioImagePipeline.drawPictureInWidget(
                            canvas = canvas,
                            imageLayer = imgLayer,
                            bitmap = bmp,
                            cx = cx,
                            cy = cy,
                            w = w,
                            h = h,
                            rotationDeg = imgLayer.rotationDeg + params.rotationDegrees,
                            densityMultiplier = baseDim / 500f
                        )
                    }
                }
            }
        }
    }

    /**
     * Lockscreen Depth Text renderer powered by StudioTypographyEngine.
     * Ultra-tall display typography, stroke outline mode, atmospheric edge glow,
     * and zIndex interleaving depth planes.
     */
    fun drawSingleText(
        canvas: Canvas,
        textLayer: StudioTextLayer,
        cx: Float,
        cy: Float,
        fontSizePx: Float,
        color: Int,
        rotationDeg: Float
    ) {
        StudioTypographyEngine.drawDepthText(
            canvas = canvas,
            textLayer = textLayer,
            cx = cx,
            cy = cy,
            fontSizePx = fontSizePx,
            colorArgb = color,
            rotationDeg = rotationDeg
        )
    }

    fun measureTextBounds(
        textLayer: StudioTextLayer,
        baseDim: Float,
        scale: Float
    ): RectF {
        return StudioTypographyEngine.measureBounds(
            textLayer = textLayer,
            baseDim = baseDim,
            scale = scale
        )
    }

    /**
     * Renders a single tactile M3 shape with shadow, fill, outline, or Liquid Glass refraction.
     */
    fun drawSingleShape(
        canvas: Canvas,
        type: CustomShapeType,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        rotationDeg: Float,
        color: Int,
        opacity: Float,
        isWireframe: Boolean,
        strokeWidth: Float,
        scallopLobes: Int = 8,
        castShadow: Boolean = true,
        shadowRadius: Float = 16f,
        blurRadius: Float = 0f
    ) {
        val path = createShapePath(type, cx, cy, w, h, scallopLobes)

        canvas.save()
        if (rotationDeg != 0f) {
            canvas.rotate(rotationDeg, cx, cy)
        }

        // Elevation Drop Shadow (0dp to 32dp):
        // Native canvas shadow layer rendered prior to drawing the fill
        if (castShadow && !isWireframe && shadowRadius > 0.5f && opacity > 0.05f) {
            val dy = shadowRadius * 0.5f
            shadowPaint.color = AndroidColor.TRANSPARENT
            shadowPaint.setShadowLayer(
                shadowRadius.coerceIn(1f, 32f),
                0f,
                dy,
                AndroidColor.argb((70 * opacity).toInt().coerceIn(0, 255), 0, 0, 0)
            )
            canvas.drawPath(path, shadowPaint)
        }

        // Standard Surface Fill / Outline with optional BlurMaskFilter soft edge glow
        val alpha = (opacity * 255).toInt().coerceIn(0, 255)
        shapePaint.color = (color and 0x00FFFFFF) or (alpha shl 24)
        if (isWireframe) {
            shapePaint.style = Paint.Style.STROKE
            shapePaint.strokeWidth = strokeWidth.coerceAtLeast(1.5f)
            shapePaint.strokeCap = Paint.Cap.ROUND
            shapePaint.strokeJoin = Paint.Join.ROUND
        } else {
            shapePaint.style = Paint.Style.FILL
        }

        // Soft Edge / Atmospheric Glow via BlurMaskFilter (Off to 24dp/32dp)
        if (blurRadius > 0.5f) {
            shapePaint.maskFilter = android.graphics.BlurMaskFilter(
                blurRadius.coerceIn(0.5f, 32f),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        } else {
            shapePaint.maskFilter = null
        }

        canvas.drawPath(path, shapePaint)

        // Highlight inner border for crisp tactile feel (only when not heavily blurred)
        if (!isWireframe && opacity > 0.3f && blurRadius <= 2f) {
            canvas.drawPath(path, innerBorderPaint)
        }

        canvas.restore()
    }

    /**
     * Constructs the parametric vector path for the official Material 3 Expressive shapes.
     * Powered by Google's androidx.graphics.shapes RoundedPolygon engine in Material3ShapeLibrary.
     */
    fun createShapePath(
        type: CustomShapeType,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        scallopLobes: Int = 8
    ): Path {
        return Material3ShapeLibrary.transformPath(type.m3Token, cx, cy, w, h)
    }
}
