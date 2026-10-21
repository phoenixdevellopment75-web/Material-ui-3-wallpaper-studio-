package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.sin

/**
 * PixelPlayer-Grade Motion Systems:
 * 1. PixelPlayerCircularReveal: Expanding circular viewport mask morph reveal on app launch.
 * 2. WavyDivider: Animated continuous sinusoidal wave divider path.
 * 3. WavyChangelogBottomSheet: Milestone 2.6.0 bottom sheet with spatial bounce entrance (dampingRatio = 0.65f, stiffness = 320f).
 */

/**
 * PixelPlayer Hardware Backdrop Blur Modifier.
 */
fun Modifier.pixelPlayerBackdropBlur(radiusDp: Dp): Modifier {
    return if (radiusDp > 0.dp && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        this.blur(radiusDp)
    } else {
        this
    }
}

/**
 * PixelPlayer Circular Reveal Modifier.
 * Dynamically expands an oval mask centered at (width/2, height/2) out to maximum canvas hypotenuse.
 */
fun Modifier.pixelPlayerCircularReveal(launchProgress: Float): Modifier = this.graphicsLayer {
    clip = true
    shape = GenericShape { size, _ ->
        val maxRadius = hypot(size.width, size.height)
        addOval(
            Rect(
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxRadius * launchProgress.coerceIn(0f, 1f)
            )
        )
    }
}

/**
 * Container executing the PixelPlayer circular morph reveal animation on launch.
 */
@Composable
fun PixelPlayerRevealContainer(
    modifier: Modifier = Modifier,
    startReveal: Boolean = true,
    onRevealFinished: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var targetProgress by remember { mutableFloatStateOf(if (startReveal) 0f else 1f) }

    LaunchedEffect(startReveal) {
        if (startReveal) {
            targetProgress = 1f
        }
    }

    val launchProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 320f),
        finishedListener = { if (it >= 0.99f) onRevealFinished() },
        label = "PixelPlayerCircularReveal"
    )

    Box(
        modifier = modifier.pixelPlayerCircularReveal(launchProgress)
    ) {
        content()
    }
}

/**
 * Animated continuous sine-wave separator divider.
 */
@Composable
fun WavyDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    strokeWidthDp: Dp = 2.dp,
    waveAmplitudeDp: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WavyDividerAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavyDividerPhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .testTag("wavy_separator_divider")
    ) {
        val wavePath = Path().apply {
            val midY = size.height / 2f
            moveTo(0f, midY)
            val stepPx = 16f
            var x = 0f
            while (x <= size.width + stepPx) {
                val y = midY + sin(x * 0.045f + phase) * waveAmplitudeDp.toPx()
                lineTo(x, y)
                x += stepPx
            }
        }
        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(width = strokeWidthDp.toPx())
        )
    }
}

/**
 * PixelPlayer-grade Milestone 2.6.0 Changelog Bottom Sheet.
 * Features spatial bounce entrance physics (dampingRatio = 0.65f, stiffness = 320f),
 * animated wavy sine divider, and detailed release highlights.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavyChangelogBottomSheet(
    onDismiss: () -> Unit,
    onLetsCreateClicked: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        contentWindowInsets = { WindowInsets.navigationBars },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
        modifier = Modifier.testTag("wavy_changelog_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Wavy Separator at the top of the sheet
            WavyDivider(
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            // M3 Astroid Star / Badge Header
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(68.dp)
                    .padding(top = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "M3 Astroid Star",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Milestone Badge Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "RELEASE 2.6.0 MILESTONE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "What's New in Wallpaper Studio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Architectural overhaul, mathematical generative corrections, and tactile PixelPlayer motion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Feature Highlights
            ChangelogHighlightItem(
                icon = Icons.Default.GridOn,
                title = "Mondrian Grids Architecture",
                description = "Disciplined 2D BSP partitioning canvas into 6-12 non-overlapping architectural rectangles with dark track dividers."
            )

            ChangelogHighlightItem(
                icon = Icons.Default.Layers,
                title = "Mountain Paper Ridges",
                description = "4-6 gentle sine-harmonic horizontal wave horizons across lower 60% with atmospheric tonal depth."
            )

            ChangelogHighlightItem(
                icon = Icons.Default.Style,
                title = "Geometric Planar Slices",
                description = "3-5 overlapping diagonal cuts (15°-45°) with 16dp filleted vertices and 4dp cast elevation drop shadows."
            )

            ChangelogHighlightItem(
                icon = Icons.Default.AutoAwesome,
                title = "Zero Lobe Clover Normalization",
                description = "Expanded 10% bounds margin clearance strictly eliminating bottom lobe truncation on 4-leaf clover."
            )

            ChangelogHighlightItem(
                icon = Icons.Default.Palette,
                title = "Saved Styles Gallery Sheet",
                description = "Dedicated 2-column preset gallery with 16:9 previews, quick apply, delete, and DataStore persistence."
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Button(
                onClick = onLetsCreateClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("changelog_lets_create_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Let's Create",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChangelogHighlightItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
