package com.example.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

enum class MotionStyle(val label: String, val description: String) {
    SNAPPY("Snappy", "Crisp, instantaneous spring responsiveness (800f stiffness)"),
    EXPRESSIVE("Expressive", "Fluid organic spring physics with natural bounce (Default)"),
    GENTLE("Gentle", "Soft, tranquil deceleration for deep flowing transitions")
}

data class MotionPhysicsConfig(
    val floatSpring: SpringSpec<Float>,
    val intOffsetSpring: SpringSpec<IntOffset>,
    val dpSpring: SpringSpec<Dp>,
    val fadeSpec: FiniteAnimationSpec<Float>
)

fun getMotionPhysics(style: MotionStyle): MotionPhysicsConfig = when (style) {
    MotionStyle.SNAPPY -> MotionPhysicsConfig(
        floatSpring = spring(dampingRatio = 0.75f, stiffness = 650f),
        intOffsetSpring = spring(dampingRatio = 0.75f, stiffness = 650f),
        dpSpring = spring(dampingRatio = 0.75f, stiffness = 650f),
        fadeSpec = tween(durationMillis = 140)
    )
    MotionStyle.EXPRESSIVE -> MotionPhysicsConfig(
        floatSpring = spring(dampingRatio = TactileMotion.TACTILE_DAMPING, stiffness = TactileMotion.TACTILE_STIFFNESS),
        intOffsetSpring = spring(dampingRatio = TactileMotion.TACTILE_DAMPING, stiffness = TactileMotion.TACTILE_STIFFNESS),
        dpSpring = spring(dampingRatio = TactileMotion.TACTILE_DAMPING, stiffness = TactileMotion.TACTILE_STIFFNESS),
        fadeSpec = tween(durationMillis = 200)
    )
    MotionStyle.GENTLE -> MotionPhysicsConfig(
        floatSpring = spring(dampingRatio = 0.90f, stiffness = 200f),
        intOffsetSpring = spring(dampingRatio = 0.90f, stiffness = 200f),
        dpSpring = spring(dampingRatio = 0.90f, stiffness = 200f),
        fadeSpec = tween(durationMillis = 320)
    )
}

val LocalMotionPhysics = compositionLocalOf<MotionPhysicsConfig> {
    getMotionPhysics(MotionStyle.EXPRESSIVE)
}
