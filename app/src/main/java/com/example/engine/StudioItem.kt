package com.example.engine

import androidx.compose.ui.geometry.Offset

/**
 * Modes for the Canvas Background Engine.
 */
enum class BackgroundMode(val displayName: String) {
    SOLID("Solid Color"),
    LINEAR_GRADIENT("Linear Gradient"),
    RADIAL_GRADIENT("Radial Glow")
}

/**
 * Dedicated background configuration for canvas and procedural wallpapers.
 * Positioned at the lowest layer (zIndex = 0) keeping shapes and text distinct.
 */
data class CanvasBackgroundConfig(
    val mode: BackgroundMode = BackgroundMode.LINEAR_GRADIENT,
    val solidColorHex: Long? = null,
    val gradientColorHexes: List<Long> = emptyList(),
    val angleDegrees: Float = 90f
)

/**
 * Unified polymorphic interface for all interactive visual entities on the Studio Canvas.
 * Unifies shapes and text layers to ensure seamless touch hit-testing, dragging,
 * scaling, and zIndex depth interweaving without pointer-interception freezing.
 */
sealed interface StudioItem {
    val id: String
    val normalizedX: Float
    val normalizedY: Float
    val scale: Float
    val scaleX: Float get() = scale
    val scaleY: Float get() = scale
    val rotation: Float
    val zIndex: Int
    val opacity: Float

    /**
     * Accurately tests if the given touchPoint in canvas pixels falls within this item's bounds,
     * accounting for item rotation, aspect ratios, and comfortable touch margins.
     */
    fun contains(
        touchPoint: Offset,
        canvasW: Float,
        canvasH: Float,
        baseDim: Float,
        globalScale: Float
    ): Boolean
}
