package com.example.engine

import java.util.UUID

/**
 * Text alignment options for Studio Text Layers.
 */
enum class StudioTextAlign {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * Depth Text Layer entity in Studio Mode.
 * Implements Nagasaki-style architectural display typography:
 * ultra-bold, condensed, zero inter-letter gap, exaggerated vertical ascenders.
 * Supports zIndex (bring to front/send to back), opacity, rotation,
 * and elevation drop shadow.
 */
data class StudioTextLayer(
    override val id: String = UUID.randomUUID().toString(),
    val text: String = "STUDIO",
    override val normalizedX: Float = 0.5f,
    override val normalizedY: Float = 0.5f,
    val normalizedSize: Float = 0.16f, // Relative to base dimension (minOf width, height)
    val rotationDeg: Float = 0.0f,
    val colorIndex: Int = 1,
    val customColorHex: Long? = null,
    override val opacity: Float = 1.0f,
    val shadowRadius: Float = 16.0f, // Drop shadow elevation radius
    override val zIndex: Int = 0,
    val isCondensed: Boolean = true,
    val letterSpacing: Float = -0.04f, // Zero to negative inter-letter gap for compact architectural look
    val letterSpacingSp: Float = 0.0f, // Granular letter spacing / tracking (-4.0sp .. +12.0sp)
    val lineHeightMultiplier: Float = 1.0f, // Granular vertical lead / line height (0.75x .. 1.60x)
    val textAlign: StudioTextAlign = StudioTextAlign.CENTER,
    val typographyStyle: StudioTypographyStyle = StudioTypographyStyle.ARCHITECTURAL_CONDENSED,
    val isOutlineOnly: Boolean = false,
    val strokeWidthDp: Float = 3.0f,
    val glowRadiusDp: Float = 0.0f,
    override val scaleX: Float = 1.0f,
    override val scaleY: Float = 1.0f,
    val customFontPath: String? = null,
    val customFontName: String? = null
) : StudioItem {
    override val scale: Float get() = normalizedSize
    override val rotation: Float get() = rotationDeg

    override fun contains(
        touchPoint: androidx.compose.ui.geometry.Offset,
        canvasW: Float,
        canvasH: Float,
        baseDim: Float,
        globalScale: Float
    ): Boolean {
        val cx = normalizedX * canvasW
        val cy = normalizedY * canvasH
        val bounds = StudioTypographyEngine.measureBounds(this, baseDim, globalScale)

        val rad = -Math.toRadians(rotationDeg.toDouble())
        val cos = kotlin.math.cos(rad).toFloat()
        val sin = kotlin.math.sin(rad).toFloat()
        val dx = touchPoint.x - cx
        val dy = touchPoint.y - cy
        val localX = dx * cos - dy * sin
        val localY = dx * sin + dy * cos

        val padding = 20f
        return localX in (bounds.left - padding)..(bounds.right + padding) &&
               localY in (bounds.top - padding)..(bounds.bottom + padding)
    }
}
