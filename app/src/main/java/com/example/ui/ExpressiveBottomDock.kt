package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.palette.ColorPalette
import kotlinx.coroutines.launch

enum class BottomDockMode(val displayName: String, val description: String) {
    DETACHED_CAPSULE("Detached Floating Capsule", "Floating pill 16dp above navigation bar"),
    ATTACHED_BAR("Attached Bottom Bar", "Anchored to bottom edge with integrated download button")
}

/**
 * Material 3 Expressive Iconographic Bottom Navigation Dock.
 *
 * Implements:
 * - Asymmetric pill for Style
 * - Starburst / Clover circular badge for Shapes
 * - Squircle for Typography Text
 * - Rounded capsule for Image / Photo layer
 * - Tri-tone circles for Palette
 * - Conditional AI Generation Sparkle Gem button (when isAiEnabled is true)
 * - Seamless Attached Bottom Bar vs. Detached Floating Capsule layouts
 */
@Composable
fun ExpressiveStudioDock(
    palette: ColorPalette,
    styleIcon: ImageVector,
    dockMode: BottomDockMode = BottomDockMode.DETACHED_CAPSULE,
    isAiEnabled: Boolean = false,
    isMultiSelectActive: Boolean = false,
    onOpenStyleSheet: () -> Unit,
    onOpenAddShapeSheet: () -> Unit,
    onOpenAddTextSheet: () -> Unit,
    onPickImage: () -> Unit,
    onGenerateLayout: () -> Unit,
    onOpenPaletteSheet: () -> Unit,
    onOpenAiSheet: () -> Unit = {},
    onToggleMultiSelect: () -> Unit = {},
    onOpenSavedDesigns: () -> Unit = {},
    onExportWallpaper: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val centerButtonScale = remember { Animatable(1f) }
    val motionPhysics = LocalMotionPhysics.current

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
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .height(64.dp)
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
            .testTag("expressive_studio_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (dockMode == BottomDockMode.ATTACHED_BAR) {
                        Modifier
                            .navigationBarsPadding()
                            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 12.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1. Style: Asymmetric Pill
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 8.dp, bottomStart = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 8.dp, bottomStart = 8.dp))
                    .clickable { onOpenStyleSheet() }
                    .testTag("dock_style_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Style Controls",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Shapes: Starburst / Clover Circular Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .clickable { onOpenAddShapeSheet() }
                    .testTag("dock_shapes_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = "Add Geometric Shape",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Text: Architectural 'A' / 'T' Squircle
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenAddTextSheet() }
                    .testTag("dock_text_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = "Add Depth Text",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 4. Image: Photo Add Rounded Capsule
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPickImage() }
                    .testTag("dock_image_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Picture inside Shape",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 5. Center Auto-Arrange / Re-roll (Primary Action)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .scale(centerButtonScale.value)
                    .clip(CircleShape)
                    .clickable {
                        scope.launch {
                            centerButtonScale.animateTo(0.88f, animationSpec = motionPhysics.floatSpring)
                            onGenerateLayout()
                            centerButtonScale.animateTo(1f, animationSpec = motionPhysics.floatSpring)
                        }
                    }
                    .testTag("dock_arrange_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Auto Arrange Layout",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 6. Colors: Tri-tone Palette Circles
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .clickable { onOpenPaletteSheet() }
                    .testTag("dock_palette_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palette.colors.take(3).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            // 7. Multi-Select Toggle Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMultiSelectActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                border = if (isMultiSelectActive) androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.primary
                ) else null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleMultiSelect() }
                    .testTag("dock_multiselect_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Multi-Element Group Select",
                        tint = if (isMultiSelectActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 8. Saved Designs Library Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenSavedDesigns() }
                    .testTag("dock_saved_designs_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Saved Designs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 9. Dedicated AI Generation Button (Conditionally Rendered)
            if (isAiEnabled) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onOpenAiSheet() }
                        .testTag("dock_ai_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Gemini AI Studio Generator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 10. Integrated Download / Export Button (Only in Attached Bottom Bar mode)
            if (dockMode == BottomDockMode.ATTACHED_BAR) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onExportWallpaper() }
                        .testTag("dock_download_integrated_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Wallpaper",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Standard Dock for procedural wallpaper generation patterns.
 */
@Composable
fun ExpressiveStandardDock(
    patternIcon: ImageVector,
    patternName: String,
    palette: ColorPalette,
    dockMode: BottomDockMode = BottomDockMode.DETACHED_CAPSULE,
    isAiEnabled: Boolean = false,
    onOpenStyleSheet: () -> Unit,
    onGenerate: () -> Unit,
    onOpenPaletteSheet: () -> Unit,
    onOpenAiSheet: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onExportWallpaper: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val generateButtonScale = remember { Animatable(1f) }
    val motionPhysics = LocalMotionPhysics.current

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
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .height(64.dp)
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
            .testTag("expressive_standard_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (dockMode == BottomDockMode.ATTACHED_BAR) {
                        Modifier
                            .navigationBarsPadding()
                            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 12.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Style Chip
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .height(48.dp)
                    .clip(CircleShape)
                    .clickable { onOpenStyleSheet() }
                    .testTag("standard_style_chip_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = patternIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = patternName.split(" ").firstOrNull() ?: "Style",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Generate Button with Expressive Motion
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .scale(generateButtonScale.value)
                    .clip(CircleShape)
                    .clickable {
                        scope.launch {
                            generateButtonScale.animateTo(0.88f, animationSpec = motionPhysics.floatSpring)
                            onGenerate()
                            generateButtonScale.animateTo(1f, animationSpec = motionPhysics.floatSpring)
                        }
                    }
                    .testTag("standard_generate_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Generate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Palette Chip
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier
                    .height(48.dp)
                    .clip(CircleShape)
                    .clickable { onOpenPaletteSheet() }
                    .testTag("standard_palette_chip_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palette.colors.take(3).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                    Text(
                        text = "Palette",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Dedicated AI Button if enabled
            if (isAiEnabled) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onOpenAiSheet() }
                        .testTag("standard_ai_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Gemini AI Studio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Attached mode Export button
            if (dockMode == BottomDockMode.ATTACHED_BAR) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onExportWallpaper() }
                        .testTag("standard_export_integrated_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Wallpaper",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
