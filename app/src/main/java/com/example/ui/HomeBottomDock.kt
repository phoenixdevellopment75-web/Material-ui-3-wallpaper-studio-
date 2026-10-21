package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palette.ColorPalette
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive Shape Command Dock for the Home Page.
 * Supports:
 * - FLOATING_PILL / DETACHED_CAPSULE: Elevated 16dp above navigation bar with rounded capsule shape.
 * - ATTACHED_BAR: Docked flush against the bottom edge with edge-to-edge container and integrated export flank.
 * Geometric Controls:
 * - Style: Horizontal pill with tune glyph.
 * - Studio: Asymmetric pill with architecture glyph.
 * - Generate: Prominent center circle/squircle with die glyph and spring bounce.
 * - Palette: Asymmetric tri-dot palette pill.
 * - Save Style: Squircle bookmark badge to save current seed & style preset directly.
 */
@Composable
fun HomeBottomDock(
    patternIcon: ImageVector,
    patternName: String,
    palette: ColorPalette,
    dockMode: BottomDockMode,
    isStudioMode: Boolean,
    onOpenStyleSheet: () -> Unit,
    onToggleStudio: () -> Unit,
    onGenerate: () -> Unit,
    onOpenPaletteSheet: () -> Unit,
    onSaveStylePreset: () -> Unit,
    onOpenSavedDesigns: () -> Unit = onSaveStylePreset,
    onExportWallpaper: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val generateBounce = remember { Animatable(1f) }

    val containerShape = if (dockMode == BottomDockMode.ATTACHED_BAR) {
        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(32.dp)
    }

    val containerModifier = if (dockMode == BottomDockMode.ATTACHED_BAR) {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp)
            .height(68.dp)
    }

    Surface(
        shape = containerShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = containerModifier
            .clip(containerShape)
            .testTag("home_bottom_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (dockMode == BottomDockMode.ATTACHED_BAR) {
                        Modifier
                            .navigationBarsPadding()
                            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)
                    } else {
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Style Pill (Horizontal Pill with Tune glyph)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .height(48.dp)
                    .clip(CircleShape)
                    .clickable { onOpenStyleSheet() }
                    .testTag("home_dock_style_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Style Tuning",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = patternName.split(" ").firstOrNull() ?: "Style",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 2. Studio Toggle Pill (Architecture / Brush glyph)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isStudioMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onToggleStudio() }
                    .testTag("home_dock_studio_pill")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isStudioMode) Icons.Default.Brush else Icons.Default.Architecture,
                        contentDescription = "Toggle Studio Mode",
                        tint = if (isStudioMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Prominent Center Floating Generate Button (Circle / Squircle with Die glyph)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(54.dp)
                    .scale(generateBounce.value)
                    .clip(CircleShape)
                    .clickable {
                        scope.launch {
                            generateBounce.animateTo(
                                0.86f,
                                animationSpec = tween(70, easing = FastOutSlowInEasing)
                            )
                            generateBounce.animateTo(
                                1.0f,
                                animationSpec = tween(120, easing = FastOutSlowInEasing)
                            )
                        }
                        onGenerate()
                    }
                    .testTag("home_dock_generate_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Generate Wallpaper",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 4. Palette Asymmetric Tri-Dot Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenPaletteSheet() }
                    .testTag("home_dock_palette_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val previewColors = palette.colors.take(3).ifEmpty {
                        listOf(Color(0xFF263238), Color(0xFF607D8B), Color(0xFFB0BEC5))
                    }
                    previewColors.forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(col)
                        )
                    }
                }
            }

            // 5. Saved Styles / Presets Squircle Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenSavedDesigns() }
                    .testTag("home_dock_save_preset_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Saved Styles & Canvas Presets",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 6. Flank Export button (When in Attached Bar mode)
            if (dockMode == BottomDockMode.ATTACHED_BAR) {
                IconButton(
                    onClick = onExportWallpaper,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("home_dock_attached_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Wallpaper",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
