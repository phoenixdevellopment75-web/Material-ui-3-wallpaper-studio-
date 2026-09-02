package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AspectRatioPreset
import com.example.engine.WallpaperParams
import com.example.engine.WallpaperPatternType
import com.example.palette.ColorPalette
import com.example.palette.GradientType
import com.example.palette.PaletteEngine
import com.example.ui.components.FloatingPillTabRow
import com.example.ui.tabs.AiStudioTab

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspectorContent(
    state: WallpaperUiState,
    viewModel: WallpaperViewModel,
    onTabSelected: (InspectorTab) -> Unit,
    onParamsChange: ((WallpaperParams) -> WallpaperParams) -> Unit,
    onRollSeed: () -> Unit,
    onOpenColorPicker: (Int) -> Unit,
    onApplyDynamicMonet: () -> Unit,
    onApplyAlgorithmicHarmony: (String) -> Unit,
    onAddColorStop: () -> Unit,
    onRemoveColorStop: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Floating Pill Tab Navigation Bar
            FloatingPillTabRow(
                tabs = InspectorTab.entries,
                selectedTab = state.activeInspectorTab,
                onTabSelected = { tab ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(tab)
                },
                tabLabel = { tab -> tab.label },
                tabIcon = { tab, isSelected ->
                    val icon = when (tab) {
                        InspectorTab.PATTERNS -> Icons.Default.GridOn
                        InspectorTab.MATH_CONTROLS -> Icons.Default.Tune
                        InspectorTab.PALETTES -> Icons.Default.Palette
                        InspectorTab.AI_STUDIO -> Icons.Default.AutoAwesome
                        InspectorTab.FORMAT -> Icons.Default.Crop
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(15.dp)
                            .padding(end = 4.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Tab Body Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
                    .padding(top = 8.dp)
            ) {
                when (state.activeInspectorTab) {
                    InspectorTab.PATTERNS -> {
                        PatternSelectorTab(
                            params = state.params,
                            onParamsChange = onParamsChange,
                            onRollSeed = onRollSeed
                        )
                    }
                    InspectorTab.MATH_CONTROLS -> {
                        MathTuningTab(
                            params = state.params,
                            onParamsChange = onParamsChange
                        )
                    }
                    InspectorTab.PALETTES -> {
                        PaletteTab(
                            params = state.params,
                            onParamsChange = onParamsChange,
                            onOpenColorPicker = onOpenColorPicker,
                            onApplyDynamicMonet = onApplyDynamicMonet,
                            onApplyAlgorithmicHarmony = onApplyAlgorithmicHarmony,
                            onAddColorStop = onAddColorStop,
                            onRemoveColorStop = onRemoveColorStop
                        )
                    }
                    InspectorTab.AI_STUDIO -> {
                        AiStudioTab(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    InspectorTab.FORMAT -> {
                        FormatTab(
                            params = state.params,
                            onParamsChange = onParamsChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternSelectorTab(
    params: WallpaperParams,
    onParamsChange: ((WallpaperParams) -> WallpaperParams) -> Unit,
    onRollSeed: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pattern Families Horizontal List
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(WallpaperPatternType.entries) { type ->
                val isSelected = params.patternType == type
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onParamsChange { it.copy(patternType = type, subTypeIndex = 0) }
                    },
                    label = { Text(type.displayName, fontSize = 13.sp) },
                    leadingIcon = {
                        val icon = when (type) {
                            WallpaperPatternType.NESTED_ARCHES -> Icons.Default.GridOn
                            WallpaperPatternType.TOPOGRAPHIC_CONTOURS -> Icons.Default.Grain
                            WallpaperPatternType.DESERT_DUNES -> Icons.Default.Waves
                            WallpaperPatternType.ORGANIC_SCALLOPS -> Icons.Default.AutoAwesome
                            WallpaperPatternType.PASTURE_FOLIAGE -> Icons.Default.Landscape
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = CircleShape,
                    modifier = Modifier.testTag("chip_${type.name.lowercase()}")
                )
            }
        }

        // Subtypes Row
        Text(
            text = "ALGORITHM SUBTYPE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(params.patternType.subTypes.indices.toList()) { index ->
                val subTypeName = params.patternType.subTypes[index]
                val isSelected = params.subTypeIndex == index
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onParamsChange { it.copy(subTypeIndex = index) }
                    },
                    label = { Text(subTypeName, fontSize = 12.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    shape = CircleShape,
                    colors = if (isSelected) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else AssistChipDefaults.assistChipColors(),
                    modifier = Modifier.testTag("subtype_${index}")
                )
            }
        }

        // Random Seed Roller Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("Deterministic Seed", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("#${params.seed}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRollSeed()
                },
                shape = CircleShape,
                modifier = Modifier.testTag("roll_seed_button")
            ) {
                Text("Roll Seed")
            }
        }
    }
}

@Composable
private fun MathTuningTab(
    params: WallpaperParams,
    onParamsChange: ((WallpaperParams) -> WallpaperParams) -> Unit
) {
    val scrollState = rememberScrollState()
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Density / Scale Slider
        SliderControlRow(
            title = "Density & Scale",
            valueText = String.format("%.2fx", params.scale),
            value = params.scale,
            valueRange = 0.3f..3.0f,
            testTag = "scale_slider",
            onValueChange = { v ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onParamsChange { it.copy(scale = v) }
            }
        )

        // Complexity / Octaves Slider
        SliderControlRow(
            title = "Complexity & Octaves",
            valueText = String.format("%.2fx", params.complexity),
            value = params.complexity,
            valueRange = 0.4f..2.5f,
            testTag = "complexity_slider",
            onValueChange = { v ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onParamsChange { it.copy(complexity = v) }
            }
        )

        // Distortion / Warping Slider
        SliderControlRow(
            title = "Distortion & Turbulence",
            valueText = String.format("%.2f", params.distortion),
            value = params.distortion,
            valueRange = 0.0f..2.0f,
            testTag = "distortion_slider",
            onValueChange = { v ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onParamsChange { it.copy(distortion = v) }
            }
        )

        // Line Width Slider
        SliderControlRow(
            title = "Line & Border Width",
            valueText = String.format("%.1fdp", params.lineWidth),
            value = params.lineWidth,
            valueRange = 0.5f..8.0f,
            testTag = "line_width_slider",
            onValueChange = { v ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onParamsChange { it.copy(lineWidth = v) }
            }
        )

        // Rotation Slider
        SliderControlRow(
            title = "Canvas Rotation",
            valueText = "${params.rotationDegrees.toInt()}°",
            value = params.rotationDegrees,
            valueRange = 0f..360f,
            testTag = "rotation_slider",
            onValueChange = { v ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onParamsChange { it.copy(rotationDegrees = v) }
            }
        )

        // Color Cycle Frequency Slider
        SliderControlRow(
            title = "Color Recurrence Frequency",
            valueText = String.format("%.1fx", params.colorCycleFreq),
            value = params.colorCycleFreq,
            valueRange = 0.5f..4.0f,
            testTag = "color_freq_slider",
            onValueChange = { v ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onParamsChange { it.copy(colorCycleFreq = v) }
            }
        )

        // Wireframe vs Solid Mode Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Wireframe Outline Mode", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Render delicate geometric paths", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = params.isWireframe,
                onCheckedChange = { checked ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onParamsChange { it.copy(isWireframe = checked) }
                },
                modifier = Modifier.testTag("wireframe_switch")
            )
        }
    }
}

@Composable
private fun SliderControlRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(valueText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

@Composable
private fun PaletteTab(
    params: WallpaperParams,
    onParamsChange: ((WallpaperParams) -> WallpaperParams) -> Unit,
    onOpenColorPicker: (Int) -> Unit,
    onApplyDynamicMonet: () -> Unit,
    onApplyAlgorithmicHarmony: (String) -> Unit,
    onAddColorStop: () -> Unit,
    onRemoveColorStop: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Action Chips (Material You Monet & Harmonies)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onApplyDynamicMonet()
                    },
                    label = { Text("Monet Dynamic") },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = CircleShape,
                    modifier = Modifier.testTag("apply_monet_button")
                )
            }
            item {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onParamsChange { it.copy(palette = PaletteEngine.enforceMonotonicLuminance(it.palette, ascending = true)) }
                    },
                    label = { Text("Sort Luminance") },
                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = CircleShape,
                    modifier = Modifier.testTag("sort_luminance_button")
                )
            }
            item {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onApplyAlgorithmicHarmony("complementary")
                    },
                    label = { Text("Complementary") },
                    shape = CircleShape
                )
            }
            item {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onApplyAlgorithmicHarmony("triadic")
                    },
                    label = { Text("Triadic") },
                    shape = CircleShape
                )
            }
            item {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onApplyAlgorithmicHarmony("monochromatic")
                    },
                    label = { Text("Monochrome") },
                    shape = CircleShape
                )
            }
            item {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onApplyAlgorithmicHarmony("analogous")
                    },
                    label = { Text("Analogous") },
                    shape = CircleShape
                )
            }
        }

        // Active Color Stops Editor
        Text(
            text = "ACTIVE PALETTE STOPS (TAP TO EDIT)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            params.palette.colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenColorPicker(index)
                        }
                        .testTag("color_stop_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    if (params.palette.colors.size > 2 && index == params.palette.colors.size - 1) {
                        IconButton(
                            onClick = { onRemoveColorStop(index) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            if (params.palette.colors.size < 8) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAddColorStop()
                        }
                        .testTag("add_color_stop_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Color", modifier = Modifier.size(18.dp))
                }
            }
        }

        // Curated Presets Carousel
        Text(
            text = "CURATED PALETTE PRESETS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PaletteEngine.allPresets) { preset ->
                val isSelected = params.palette.id == preset.id
                Surface(
                    modifier = Modifier
                        .width(130.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onParamsChange { it.copy(palette = preset) }
                        }
                        .testTag("preset_${preset.id}"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(preset.colors))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatTab(
    params: WallpaperParams,
    onParamsChange: ((WallpaperParams) -> WallpaperParams) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ASPECT RATIO & TARGET RESOLUTION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )

        AspectRatioPreset.entries.forEach { preset ->
            val isSelected = params.aspectRatio == preset
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onParamsChange { it.copy(aspectRatio = preset) }
                    }
                    .testTag("aspect_${preset.name.lowercase()}"),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${preset.defaultExportWidth} × ${preset.defaultExportHeight} px",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
