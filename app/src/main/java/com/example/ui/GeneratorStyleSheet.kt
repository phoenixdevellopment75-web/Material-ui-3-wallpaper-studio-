package com.example.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LineStyle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.AspectRatioPreset
import com.example.engine.WallpaperParams
import com.example.engine.WallpaperPatternType

/**
 * Compact, content-wrapping bottom sheet wrapper for Generator Style & Tuning.
 * Hugs its content without vertical dead space, sliding up smoothly from the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorStyleBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        // Wrap height to content so it only takes as much space as it needs
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("generator_style_bottom_sheet"),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .wrapContentHeight()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Generator Style & Tuning",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time canvas tuning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compact content: Only occupies necessary height
            content()
        }
    }
}

/**
 * Compact, bottom-anchored bottom sheet for Generator Style & Tuning.
 * Hugs content height so the top canvas remains an unblocked, live-updating preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorStyleSheet(
    params: WallpaperParams,
    hapticStrength: HapticStrength,
    onParamsChange: ((WallpaperParams) -> WallpaperParams) -> Unit,
    onPatternSelect: (WallpaperPatternType) -> Unit,
    onAspectRatioChange: (AspectRatioPreset) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Pattern", "Tuning", "Geometry")

    GeneratorStyleBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            if (hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Scrollable Content for active tab - strictly compact & height constrained
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Pattern & Subtypes
                        Text(
                            text = "Architectural Patterns",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WallpaperPatternType.entries.forEach { pattern ->
                                val isSelected = pattern == params.patternType
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (hapticStrength != HapticStrength.OFF) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onPatternSelect(pattern)
                                    },
                                    label = { Text(pattern.displayName) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getPatternIcon(pattern),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        // Subtypes
                        if (params.patternType.subTypes.isNotEmpty()) {
                            Text(
                                text = "Pattern Style Variations",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                params.patternType.subTypes.forEachIndexed { index, subType ->
                                    val isSelected = params.subTypeIndex == index
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (hapticStrength != HapticStrength.OFF) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            onParamsChange { it.copy(subTypeIndex = index) }
                                        },
                                        label = { Text(subType) },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }

                        // Wireframe Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Wireframe Outline Mode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Render hollow architectural vector strokes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = params.isWireframe,
                                onCheckedChange = { checked ->
                                    onParamsChange { it.copy(isWireframe = checked) }
                                }
                            )
                        }
                    }

                    1 -> { // Parametric Tuning
                        // Complexity Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Complexity & Density", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = String.format("%.2fx", params.complexity),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = params.complexity,
                                onValueChange = { comp -> onParamsChange { it.copy(complexity = comp) } },
                                valueRange = 0.5f..2.5f
                            )
                        }

                        // Stroke Weight / Line Width Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Stroke Weight", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = String.format("%.1fdp", params.lineWidth),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = params.lineWidth,
                                onValueChange = { lw -> onParamsChange { it.copy(lineWidth = lw) } },
                                valueRange = 1.0f..12.0f
                            )
                        }

                        // Scale Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Base Scale Factor", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = String.format("%.2fx", params.scale),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = params.scale,
                                onValueChange = { sc -> onParamsChange { it.copy(scale = sc) } },
                                valueRange = 0.5f..2.5f
                            )
                        }

                        // Distortion / Turbulence
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Wave Distortion & Warp", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = String.format("%.1f%%", params.distortion * 100f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = params.distortion,
                                onValueChange = { dist -> onParamsChange { it.copy(distortion = dist) } },
                                valueRange = 0.0f..1.0f
                            )
                        }

                        // Film Grain Overlay Noise Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Grain,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = "Film Grain Texture", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = "${(params.filmGrain * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = params.filmGrain,
                                onValueChange = { grain -> onParamsChange { it.copy(filmGrain = grain) } },
                                valueRange = 0.0f..1.0f,
                                modifier = Modifier.testTag("film_grain_slider")
                            )
                        }

                        // Pattern-specific: Stacked Pills
                        if (params.patternType == WallpaperPatternType.STACKED_PILLS) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Text(
                                text = "Pill Geometry",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Pill Width", style = MaterialTheme.typography.bodySmall)
                                    Text(text = String.format("%.1f%%", params.pillWidth * 100f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = params.pillWidth,
                                    onValueChange = { w -> onParamsChange { it.copy(pillWidth = w) } },
                                    valueRange = 0.15f..0.85f
                                )
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Pill Height", style = MaterialTheme.typography.bodySmall)
                                    Text(text = String.format("%.1f%%", params.pillHeight * 100f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = params.pillHeight,
                                    onValueChange = { h -> onParamsChange { it.copy(pillHeight = h) } },
                                    valueRange = 0.02f..0.15f
                                )
                            }
                        }
                    }

                    2 -> { // Geometry & Aspect Ratio
                        Text(
                            text = "Canvas Aspect Ratio",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AspectRatioPreset.entries.forEach { aspect ->
                                val isSelected = aspect == params.aspectRatio
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (hapticStrength != HapticStrength.OFF) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onAspectRatioChange(aspect)
                                    },
                                    label = { Text(aspect.displayName) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Rotation Degrees
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Global Canvas Angle", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "${params.rotationDegrees.toInt()}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = params.rotationDegrees,
                                onValueChange = { rot -> onParamsChange { it.copy(rotationDegrees = rot) } },
                                valueRange = 0f..360f
                            )
                        }

                        // Color Shift
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Color Progression Frequency", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = String.format("%.1fx", params.colorCycleFreq),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = params.colorCycleFreq,
                                onValueChange = { freq -> onParamsChange { it.copy(colorCycleFreq = freq) } },
                                valueRange = 0.5f..3.0f
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
