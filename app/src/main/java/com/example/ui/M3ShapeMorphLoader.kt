package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic Material 3 Expressive Shape-Morphing Loading Animation.
 * Seamlessly morphs between:
 * Circle -> 4-Leaf Clover -> 12-Sided Cookie -> Puffy Diamond -> Circle
 * Replicates the modern Google Play Store and Material 3 Expressive loading physics.
 */
@Composable
fun M3ShapeMorphLoader(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    paletteColors: List<Color> = emptyList(),
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    showGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_shape_morph_transition")

    // Guaranteed 1.8s full morph cycle (1800ms)
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morph_stage_progress"
    )

    // Smooth rotational spin
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morph_rotation_angle"
    )

    // Gentle pulsing breath
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morph_pulse_scale"
    )

    val activeColors = if (paletteColors.size >= 2) paletteColors else listOf(primaryColor, secondaryColor)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val cx = canvasW / 2f
            val cy = canvasH / 2f
            val maxR = (minOf(canvasW, canvasH) / 2f) * 0.85f * pulseScale

            val stageIndex = morphProgress.toInt() % 4
            val localFraction = morphProgress - stageIndex

            // Smooth cosine S-curve easing for the morph step
            val easedT = (1f - cos(localFraction * PI.toFloat())) / 2f

            val numPoints = 120
            val path = Path()

            for (i in 0 until numPoints) {
                val theta = (i.toFloat() / numPoints) * (2f * PI.toFloat())

                // Radial distance calculations for the 4 requested M3 tokens:
                // 1. Circle
                val rCircle = maxR

                // 2. 4-Leaf Clover
                val rClover = maxR * (0.64f + 0.36f * cos(2f * theta) * cos(2f * theta))

                // 3. 12-Point Scallop
                val rScallop = maxR * (0.84f + 0.16f * cos(12f * theta))

                // 4. Sunny Cookie (16-point star cookie)
                val rSunnyCookie = maxR * (0.78f + 0.22f * cos(16f * theta))

                val (rFrom, rTo) = when (stageIndex) {
                    0 -> Pair(rCircle, rClover)
                    1 -> Pair(rClover, rScallop)
                    2 -> Pair(rScallop, rSunnyCookie)
                    else -> Pair(rSunnyCookie, rCircle)
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

            // Dynamic color progression through palette stops
            val colorCount = activeColors.size
            val cIndex1 = (stageIndex % colorCount)
            val cIndex2 = ((stageIndex + 1) % colorCount)
            val currentColor1 = activeColors[cIndex1]
            val currentColor2 = activeColors[cIndex2]

            // Optional ambient glow behind
            if (showGlow) {
                drawCircle(
                    color = currentColor1.copy(alpha = 0.20f),
                    radius = maxR * 1.18f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
            }

            // Draw rotating morphed shape with dynamic palette gradient fill
            rotate(rotationAngle, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
                val gradientBrush = Brush.linearGradient(
                    colors = listOf(currentColor1, currentColor2)
                )
                drawPath(
                    path = path,
                    brush = gradientBrush,
                    style = Fill
                )
                // Crisp highlight contour
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.32f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Full-screen or modal backdrop shape-morphing loading overlay with status message.
 */
@Composable
fun M3ShapeMorphLoadingOverlay(
    message: String,
    modifier: Modifier = Modifier,
    subMessage: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                M3ShapeMorphLoader(
                    size = 72.dp,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!subMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
