package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dedicated Cold-Start App Launch Screen.
 *
 * Cycles strictly through the requested Material 3 shape-morphing sequence:
 * Circle -> 4-Leaf Clover -> 12-Point Scallop -> Circle
 *
 * Held for a minimum of 1.8 seconds (1800ms) before seamlessly fading out into the home screen.
 */
@Composable
fun AppLaunchScreen(
    onLaunchComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        // Hold for minimum duration of 1.8 seconds (1800ms)
        delay(1800L)
        onLaunchComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("app_launch_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Material 3 Expressive Intro Morph Loader
            M3IntroMorphLoader(size = 96.dp)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Wallpaper Studio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Procedural Vector Engine",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.25.sp
            )
        }
    }
}

/**
 * Intro Shape Morpher: Circle -> 4-Leaf Clover -> 12-Point Scallop -> Circle.
 */
@Composable
private fun M3IntroMorphLoader(
    size: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "intro_morph_transition")

    // Full 3-stage morph cycle over 1800ms
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "intro_morph_progress"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "intro_rotation"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val cx = canvasW / 2f
            val cy = canvasH / 2f
            val maxR = (minOf(canvasW, canvasH) / 2f) * 0.82f

            val stageIndex = morphProgress.toInt() % 3
            val localFraction = morphProgress - stageIndex

            // Cosine S-curve easing
            val easedT = (1f - cos(localFraction * PI.toFloat())) / 2f

            val numPoints = 120
            val path = Path()

            for (i in 0 until numPoints) {
                val theta = (i.toFloat() / numPoints) * (2f * PI.toFloat())

                // 1. Circle
                val rCircle = maxR
                // 2. 4-Leaf Clover
                val rClover = maxR * (0.64f + 0.36f * cos(2f * theta) * cos(2f * theta))
                // 3. 12-Point Scallop
                val rScallop = maxR * (0.84f + 0.16f * cos(12f * theta))

                val (rFrom, rTo) = when (stageIndex) {
                    0 -> Pair(rCircle, rClover)
                    1 -> Pair(rClover, rScallop)
                    else -> Pair(rScallop, rCircle)
                }

                val currentR = rFrom + (rTo - rFrom) * easedT
                val px = cx + currentR * cos(theta)
                val py = cy + currentR * sin(theta)

                if (i == 0) {
                    path.moveTo(px, py)
                } else {
                    path.lineTo(px, py)
                }
            }
            path.close()

            val activeColors = when (stageIndex) {
                0 -> listOf(primary, secondary)
                1 -> listOf(secondary, tertiary)
                else -> listOf(tertiary, primary)
            }

            rotate(rotationAngle, pivot = Offset(cx, cy)) {
                val gradientBrush = Brush.linearGradient(
                    colors = activeColors,
                    start = Offset(0f, 0f),
                    end = Offset(canvasW, canvasH)
                )
                drawPath(path = path, brush = gradientBrush, style = Fill)
            }
        }
    }
}
