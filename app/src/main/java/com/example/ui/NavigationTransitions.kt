package com.example.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * Production navigation transition specifications for Wallpaper Studio.
 * Tactile, fluid nested push/pop animation (depth scale + slide + fade) inspired by PixelPlayer.
 * High-performance transitions with zero blur ghosting.
 */
object NavigationTransitions {

    val expressiveSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )
    val expressiveIntOffsetSpring = spring<IntOffset>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    /**
     * Creates the animated drill-down or return ContentTransform for AnimatedContent.
     */
    fun createTransition(isDrillDown: Boolean): ContentTransform {
        return if (isDrillDown) {
            // Push / Drill-Down Transition
            (slideInHorizontally(
                animationSpec = expressiveIntOffsetSpring,
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(animationSpec = tween(220)))
                .togetherWith(
                    slideOutHorizontally(
                        animationSpec = expressiveIntOffsetSpring,
                        targetOffsetX = { fullWidth -> -fullWidth / 6 }
                    ) + scaleOut(
                        animationSpec = expressiveSpring,
                        targetScale = 0.93f
                    ) + fadeOut(animationSpec = tween(200))
                )
        } else {
            // Pop / Return Transition (Tactile Depth Return)
            (slideInHorizontally(
                animationSpec = expressiveIntOffsetSpring,
                initialOffsetX = { fullWidth -> -fullWidth / 6 }
            ) + scaleIn(
                animationSpec = expressiveSpring,
                initialScale = 0.93f
            ) + fadeIn(animationSpec = tween(220)))
                .togetherWith(
                    slideOutHorizontally(
                        animationSpec = expressiveIntOffsetSpring,
                        targetOffsetX = { fullWidth -> fullWidth }
                    ) + fadeOut(animationSpec = tween(180))
                )
        }
    }
}
