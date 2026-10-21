package com.example.ui

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Expressive Tactile Motion System for Wallpaper Studio v2.6.0.
 *
 * Tuned spring parameters for Material 3 Expressive responsiveness:
 * Target: dampingRatio = 0.62f, stiffness = 320f (Spatial & Expansions)
 * Exit: dampingRatio = 0.85f, stiffness = 450f (Clean Dismissal)
 * Settling: dampingRatio = 1.0f, stiffness = 400f (Non-oscillating Alpha & Color)
 *
 * Applied universally to:
 * - Floating Studio dock expansion / collapse / entrance
 * - Bottom sheet dragging and sheet state transitions
 * - Interactive canvas shape scaling, rotation, and translation
 */
object TactileMotion {
    const val TACTILE_DAMPING = 0.62f
    const val TACTILE_STIFFNESS = 320f

    const val EXIT_DAMPING = 0.85f
    const val EXIT_STIFFNESS = 450f

    const val SETTLING_DAMPING = 1.0f
    const val SETTLING_STIFFNESS = 400f

    val floatTactileSpring: SpringSpec<Float> = spring(
        dampingRatio = TACTILE_DAMPING,
        stiffness = TACTILE_STIFFNESS
    )

    val intOffsetTactileSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = TACTILE_DAMPING,
        stiffness = TACTILE_STIFFNESS
    )

    val intOffsetExitSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = EXIT_DAMPING,
        stiffness = EXIT_STIFFNESS
    )

    val dpTactileSpring: SpringSpec<Dp> = spring(
        dampingRatio = TACTILE_DAMPING,
        stiffness = TACTILE_STIFFNESS
    )

    val alphaSettlingSpring: SpringSpec<Float> = spring(
        dampingRatio = SETTLING_DAMPING,
        stiffness = SETTLING_STIFFNESS
    )
}
