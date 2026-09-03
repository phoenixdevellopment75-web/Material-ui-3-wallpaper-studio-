package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.engine.AspectRatioPreset

/**
 * Optimized Wallpaper Viewport.
 *
 * Edge-to-edge canvas with hardware-accelerated drawWithCache rendering:
 * - Occupies the absolute full physical screen dimensions (Modifier.fillMaxSize())
 * - Eliminates all bezel boxes, fake inner margins, rounded borders, and outer black frames
 * - Stretches seamlessly behind the status bar and navigation bar
 * - Utilizes drawWithCache and GraphicsLayer for silky 60/120 FPS pinch & pan interactions
 */
@Composable
fun WallpaperViewport(
    bitmap: Bitmap?,
    isGenerating: Boolean,
    aspectRatioPreset: AspectRatioPreset,
    isFullscreen: Boolean,
    showLauncherMockup: Boolean,
    paletteColors: List<Color> = emptyList(),
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.85f, 4.0f)
        offset += offsetChange
    }

    // Cache ImageBitmap conversion
    val imageBitmap = remember(bitmap) {
        bitmap?.asImageBitmap()
    }

    Box(
        modifier = modifier
            .testTag("wallpaper_viewport_root")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        // High performance edge-to-edge canvas using drawWithCache
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .testTag("wallpaper_canvas_draw_surface")
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .drawWithCache {
                    val canvasWidth = size.width.toInt()
                    val canvasHeight = size.height.toInt()
                    onDrawBehind {
                        if (imageBitmap != null && canvasWidth > 0 && canvasHeight > 0) {
                            drawImage(
                                image = imageBitmap,
                                dstOffset = IntOffset.Zero,
                                dstSize = IntSize(canvasWidth, canvasHeight),
                                filterQuality = FilterQuality.High
                            )
                        }
                    }
                }
        )

        // Subtle generating indicator in status bar safe area
        AnimatedVisibility(
            visible = isGenerating,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                M3ShapeMorphLoader(
                    size = 24.dp,
                    paletteColors = paletteColors,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // Launcher Mockup Overlay
        if (showLauncherMockup) {
            LauncherMockupOverlay()
        }
    }
}
