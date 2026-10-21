package com.example.engine

import androidx.compose.ui.text.style.TextGeometricTransform

/**
 * Typealias ensuring StudioTextItem is fully interchangeable with StudioTextLayer.
 */
typealias StudioTextItem = StudioTextLayer

/**
 * Extension providing Compose TextGeometricTransform for 2D horizontal text stretching.
 */
val StudioTextLayer.geometricTransform: TextGeometricTransform
    get() = TextGeometricTransform(
        scaleX = scaleX.coerceIn(0.2f, 5.0f),
        skewX = 0f
    )

/**
 * Helper to produce updated 2D stretched text layer.
 */
fun StudioTextLayer.with2DStretch(
    newScaleX: Float = scaleX,
    newScaleY: Float = scaleY
): StudioTextLayer {
    return copy(
        scaleX = newScaleX.coerceIn(0.2f, 5.0f),
        scaleY = newScaleY.coerceIn(0.2f, 5.0f)
    )
}

/**
 * Helper to update outline stroke width with bounds checking (1.0dp to 12.0dp).
 */
fun StudioTextLayer.withStrokeWidth(
    newStrokeWidthDp: Float
): StudioTextLayer {
    return copy(
        strokeWidthDp = newStrokeWidthDp.coerceIn(1.0f, 12.0f)
    )
}
