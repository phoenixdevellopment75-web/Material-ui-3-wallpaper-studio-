package com.example.engine

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Modern Lockscreen Display Typography Styles.
 * Inspired by architectural lockscreen clocks in HyperOS 3, ColorOS 16, and OriginOS 6.
 */
enum class StudioTypographyStyle(
    val displayName: String,
    val description: String,
    val defaultLetterSpacing: Float,
    val verticalStemScale: Float
) {
    /**
     * 1. Nagasaki / HyperOS Tall: Ultra-condensed vertical architectural numerals.
     */
    NAGASAKI_HYPEROS_TALL(
        displayName = "Nagasaki / HyperOS Tall",
        description = "Ultra-condensed vertical architectural numerals",
        defaultLetterSpacing = -0.06f,
        verticalStemScale = 1.38f
    ),

    /**
     * 2. ColorOS Bold Slab: Rounded, ultra-heavy display geometry.
     */
    COLOROS_BOLD_SLAB(
        displayName = "ColorOS Bold Slab",
        description = "Rounded, ultra-heavy display geometry",
        defaultLetterSpacing = -0.02f,
        verticalStemScale = 1.0f
    ),

    /**
     * 3. OriginOS Editorial Serif: High-contrast modern luxury display typeface.
     */
    ORIGINOS_EDITORIAL_SERIF(
        displayName = "OriginOS Editorial Serif",
        description = "High-contrast modern luxury display typeface",
        defaultLetterSpacing = 0.02f,
        verticalStemScale = 1.08f
    ),

    /**
     * 4. Brutalist Tech Mono: Industrial block-segmented numerals.
     */
    BRUTALIST_TECH_MONO(
        displayName = "Brutalist Tech Mono",
        description = "Industrial block-segmented numerals",
        defaultLetterSpacing = -0.01f,
        verticalStemScale = 1.0f
    ),

    /**
     * 5. Expressive M3 Sans: Clean geometric humanist sans-serif.
     */
    EXPRESSIVE_M3_SANS(
        displayName = "Expressive M3 Sans",
        description = "Clean geometric humanist sans-serif",
        defaultLetterSpacing = -0.03f,
        verticalStemScale = 1.0f
    );

    companion object {
        // Compatibility aliases
        val ARCHITECTURAL_CONDENSED get() = NAGASAKI_HYPEROS_TALL
        val MODERN_SERIF_DISPLAY get() = ORIGINOS_EDITORIAL_SERIF
        val BRUTALIST_MONOSPACE get() = BRUTALIST_TECH_MONO
        val GROTESQUE_HEAVY get() = EXPRESSIVE_M3_SANS
        val NAGASAKI_CONDENSED get() = NAGASAKI_HYPEROS_TALL
        val MODERN_SERIF_BOLD get() = ORIGINOS_EDITORIAL_SERIF
        val SMOOTH_NEO_GROTESQUE get() = EXPRESSIVE_M3_SANS
    }
}

/**
 * Lockscreen Depth Text Engine.
 * Handles ultra-tall font metrics, stroke outline modes, atmospheric edge glow,
 * and zIndex interweaving depth planes.
 */
object StudioTypographyEngine {

    fun createTypeface(style: StudioTypographyStyle): Typeface {
        return when (style) {
            StudioTypographyStyle.NAGASAKI_HYPEROS_TALL -> {
                Typeface.create("sans-serif-condensed", Typeface.BOLD)
            }
            StudioTypographyStyle.COLOROS_BOLD_SLAB -> {
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            StudioTypographyStyle.ORIGINOS_EDITORIAL_SERIF -> {
                Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            StudioTypographyStyle.BRUTALIST_TECH_MONO -> {
                Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            StudioTypographyStyle.EXPRESSIVE_M3_SANS -> {
                Typeface.create("sans-serif-medium", Typeface.BOLD) ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }
    }

    fun createTypeface(textLayer: StudioTextLayer): Typeface {
        if (!textLayer.customFontPath.isNullOrBlank()) {
            try {
                val file = java.io.File(textLayer.customFontPath)
                if (file.exists() && file.canRead()) {
                    val tf = Typeface.createFromFile(file)
                    if (tf != null) return tf
                }
            } catch (_: Exception) {
                // fallback to style default
            }
        }
        return createTypeface(textLayer.typographyStyle)
    }

    /**
     * Renders depth text onto the native canvas with lockscreen clock aesthetics:
     * - Elongated vertical stems (verticalScale)
     * - Configurable opacity (0.0f..1.0f)
     * - Atmospheric edge glow (0dp..32dp)
     * - Stroke outline mode (hollow 1dp..12dp) vs solid fill
     * - Tactile drop shadow
     * - Artifact-free round cap & round join geometry
     */
    fun drawDepthText(
        canvas: Canvas,
        textLayer: StudioTextLayer,
        cx: Float,
        cy: Float,
        fontSizePx: Float,
        colorArgb: Int,
        rotationDeg: Float,
        densityMultiplier: Float = 1.0f
    ) {
        val style = textLayer.typographyStyle
        val tf = createTypeface(textLayer)
        val clampedOpacity = textLayer.opacity.coerceIn(0f, 1f)
        val alphaInt = (clampedOpacity * 255).toInt()
        val letterSpacingVal = if (textLayer.letterSpacingSp != 0.0f) {
            textLayer.letterSpacingSp / 24f
        } else if (textLayer.letterSpacing != -0.04f) {
            textLayer.letterSpacing
        } else {
            style.defaultLetterSpacing
        }

        val lines = textLayer.text.split("\n")
        val lineLead = fontSizePx * textLayer.lineHeightMultiplier.coerceIn(0.75f, 1.60f)

        // Measure bounds for centering vertically
        val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            textSize = fontSizePx
            letterSpacing = letterSpacingVal
            isFakeBoldText = true
        }
        val firstLineBounds = Rect()
        measurePaint.getTextBounds(lines[0], 0, lines[0].length, firstLineBounds)
        val firstLineCenterY = (firstLineBounds.bottom + firstLineBounds.top) / 2f

        canvas.save()
        if (rotationDeg != 0f) {
            canvas.rotate(rotationDeg, cx, cy)
        }

        // Apply vertical stem elongation and independent 2D stretching for architectural typography
        val stemScale = style.verticalStemScale * textLayer.scaleY
        val horizScale = textLayer.scaleX
        if (stemScale != 1.0f || horizScale != 1.0f) {
            canvas.scale(horizScale, stemScale, cx, cy)
        }

        val totalHeight = (lines.size - 1) * lineLead
        val baseDrawY = (cy - firstLineCenterY) - (totalHeight / 2f)

        // 1. Soft Atmospheric Edge Glow Pass (if glow radius > 0)
        if (textLayer.glowRadiusDp > 0f && clampedOpacity > 0.05f) {
            val glowPx = textLayer.glowRadiusDp * densityMultiplier
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = tf
                textSize = fontSizePx
                letterSpacing = letterSpacingVal
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                this.style = if (textLayer.isOutlineOnly) Paint.Style.STROKE else Paint.Style.FILL
                if (textLayer.isOutlineOnly) {
                    strokeWidth = textLayer.strokeWidthDp * densityMultiplier + (glowPx * 0.4f)
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                color = colorArgb
                alpha = (alphaInt * 0.65f).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(glowPx.coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
            }
            for (i in lines.indices) {
                canvas.drawText(lines[i], cx, baseDrawY + (i * lineLead), glowPaint)
            }
        }

        // 2. Drop Shadow Pass (if shadow radius > 0 and not outlined)
        if (textLayer.shadowRadius > 0f && !textLayer.isOutlineOnly && clampedOpacity > 0.05f) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = tf
                textSize = fontSizePx
                letterSpacing = letterSpacingVal
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                this.style = Paint.Style.FILL
                color = colorArgb
                alpha = alphaInt
                val shadowColor = android.graphics.Color.argb(
                    (120 * clampedOpacity).toInt(),
                    0,
                    0,
                    0
                )
                setShadowLayer(
                    textLayer.shadowRadius * densityMultiplier,
                    0f,
                    textLayer.shadowRadius * densityMultiplier * 0.45f,
                    shadowColor
                )
            }
            for (i in lines.indices) {
                canvas.drawText(lines[i], cx, baseDrawY + (i * lineLead), shadowPaint)
            }
        }

        // 3. Primary Text Pass (Fill or Stroke Outline with Artifact-Free Round Join and Round Cap)
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            textSize = fontSizePx
            letterSpacing = letterSpacingVal
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            color = colorArgb
            alpha = alphaInt

            if (textLayer.isOutlineOnly) {
                this.style = Paint.Style.STROKE
                strokeWidth = (textLayer.strokeWidthDp * densityMultiplier).coerceAtLeast(1f)
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            } else {
                this.style = Paint.Style.FILL
            }
        }

        for (i in lines.indices) {
            canvas.drawText(lines[i], cx, baseDrawY + (i * lineLead), mainPaint)
        }
        canvas.restore()
    }

    /**
     * Measures exact bounding box for touch hit testing, corner handles, and selection box.
     */
    fun measureBounds(
        textLayer: StudioTextLayer,
        baseDim: Float,
        scale: Float,
        densityMultiplier: Float = 1.0f
    ): RectF {
        val style = textLayer.typographyStyle
        val fontSizePx = textLayer.normalizedSize * baseDim * scale
        val letterSpacingVal = if (textLayer.letterSpacingSp != 0.0f) {
            textLayer.letterSpacingSp / 24f
        } else if (textLayer.letterSpacing != -0.04f) {
            textLayer.letterSpacing
        } else {
            style.defaultLetterSpacing
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = createTypeface(textLayer)
            textSize = fontSizePx
            letterSpacing = letterSpacingVal
            isFakeBoldText = true
            if (textLayer.isOutlineOnly) {
                this.style = Paint.Style.STROKE
                strokeWidth = textLayer.strokeWidthDp * densityMultiplier
            }
        }

        val lines = textLayer.text.split("\n")
        var maxLineWidth = 0f
        for (line in lines) {
            val b = Rect()
            paint.getTextBounds(line, 0, line.length, b)
            if (b.width() > maxLineWidth) {
                maxLineWidth = b.width().toFloat()
            }
        }

        val lineLead = fontSizePx * textLayer.lineHeightMultiplier.coerceIn(0.75f, 1.60f)
        val textHeight = if (lines.size > 1) {
            ((lines.size - 1) * lineLead) + (fontSizePx * 0.9f)
        } else {
            val b = Rect()
            paint.getTextBounds(textLayer.text, 0, textLayer.text.length, b)
            b.height().toFloat()
        }

        val stemScale = style.verticalStemScale * textLayer.scaleY
        val extraPadding = if (textLayer.isOutlineOnly) textLayer.strokeWidthDp * densityMultiplier * 2f else 16f
        val w = (maxLineWidth * textLayer.scaleX) + extraPadding
        val h = (textHeight * stemScale) + extraPadding

        return RectF(-w / 2f, -h / 2f, w / 2f, h / 2f)
    }
}
