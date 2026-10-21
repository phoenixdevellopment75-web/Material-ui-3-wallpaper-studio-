package com.example.ui

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Standardized Material 3 Expressive Motion System for Wallpaper Studio v2.6.0.
 * Complies with official m3.material.io motion specifications:
 * - Spatial expansions, bottom sheets, dock entrances: dampingRatio = 0.62f, stiffness = 320f
 * - Crisp spatial dismissals: dampingRatio = 0.85f, stiffness = 450f
 * - Color, glow, and alpha settling: dampingRatio = 1.0f, stiffness = 400f
 * - Tactile haptic feedback triggers for selection and snapping
 */
object ExpressiveMotionSpec {
    // Spatial & Expansion Spring Constants (Fluid organic bounce)
    const val SPATIAL_DAMPING = 0.62f
    const val SPATIAL_STIFFNESS = 320f

    // Spatial Exit Spring Constants (Crisp, clean dismissal)
    const val EXIT_DAMPING = 0.85f
    const val EXIT_STIFFNESS = 450f

    // Color & Alpha Settling Constants (Strictly smooth, non-oscillating settling)
    const val SETTLING_DAMPING = 1.0f
    const val SETTLING_STIFFNESS = 400f

    // Standardized Spring Specifications
    val SpatialSpringFloat: SpringSpec<Float> = spring(
        dampingRatio = SPATIAL_DAMPING,
        stiffness = SPATIAL_STIFFNESS
    )

    val SpatialSpringIntOffset: SpringSpec<IntOffset> = spring(
        dampingRatio = SPATIAL_DAMPING,
        stiffness = SPATIAL_STIFFNESS
    )

    val SpatialSpringDp: SpringSpec<Dp> = spring(
        dampingRatio = SPATIAL_DAMPING,
        stiffness = SPATIAL_STIFFNESS
    )

    val SpatialExitSpringIntOffset: SpringSpec<IntOffset> = spring(
        dampingRatio = EXIT_DAMPING,
        stiffness = EXIT_STIFFNESS
    )

    val AlphaSpringFloat: SpringSpec<Float> = spring(
        dampingRatio = SETTLING_DAMPING,
        stiffness = SETTLING_STIFFNESS
    )

    /**
     * Triggers slider step tactile haptic feedback.
     */
    fun performSliderHaptic(haptics: HapticFeedback?, enabled: Boolean = true) {
        if (enabled && haptics != null) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Triggers center line / alignment snap tactile haptic feedback.
     */
    fun performSnapHaptic(haptics: HapticFeedback?, enabled: Boolean = true) {
        if (enabled && haptics != null) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
