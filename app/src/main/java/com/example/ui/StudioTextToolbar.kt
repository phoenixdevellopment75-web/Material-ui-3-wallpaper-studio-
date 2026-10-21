package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.TextFields
import com.example.engine.StudioTextLayer
import com.example.engine.StudioTypographyStyle
import com.example.palette.ColorPalette

/**
 * Granular tuning toolbar for Studio Depth Text layers.
 * Anchored to the top of the canvas viewport so it never overlaps the text or bottom dock.
 * Supports:
 * - Direct text edit (custom phrase)
 * - Palette color selection
 * - Lockscreen display typography styles (Architectural, Modern Serif, Grotesque)
 * - Stroke Outline mode vs Solid Fill
 * - Atmospheric edge glow & stroke width
 * - zIndex Bring to Front / Send to Back
 * - Opacity (0.05 to 1.0)
 * - Elevation Drop Shadow (0 to 40dp)
 * - Rotate (+45 deg step)
 * - Delete
 */
@Composable
fun StudioTextToolbar(
    textLayer: StudioTextLayer,
    palette: ColorPalette,
    textBounds: Rect = Rect.Zero,
    canvasTopOffsetDp: Dp = 0.dp,
    containerHeightDp: Dp = 800.dp,
    onUpdateText: (String) -> Unit,
    onColorSelected: (Int) -> Unit,
    onRotate: (Float) -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDelete: () -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onUpdateShadowRadius: (Float) -> Unit,
    onDeselect: () -> Unit,
    onUpdateTypographyStyle: (StudioTypographyStyle) -> Unit = {},
    onImportCustomFont: (Uri) -> Unit = {},
    onClearCustomFont: () -> Unit = {},
    onToggleOutlineOnly: () -> Unit = {},
    onUpdateStrokeWidth: (Float) -> Unit = {},
    onUpdateGlowRadius: (Float) -> Unit = {},
    onUpdateScaleX: (Float) -> Unit = {},
    onUpdateScaleY: (Float) -> Unit = {},
    onUpdateLetterSpacing: (Float) -> Unit = {},
    onUpdateLineHeight: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportCustomFont(uri)
        }
    }

    if (showEditDialog) {
        var editText by remember { mutableStateOf(textLayer.text) }
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Depth Text",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it.uppercase() },
                        label = { Text("Display Text") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                onUpdateText(editText.ifBlank { "STUDIO" })
                                showEditDialog = false
                            }
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .wrapContentHeight()
                .testTag("studio_text_toolbar")
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Edit Text String
                    FilterChip(
                        selected = false,
                        onClick = { showEditDialog = true },
                        label = { Text(textLayer.text, fontWeight = FontWeight.Black) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Text", modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )

                    // Fill vs Stroke Outline Toggle
                    FilterChip(
                        selected = textLayer.isOutlineOnly,
                        onClick = onToggleOutlineOnly,
                        label = { Text(if (textLayer.isOutlineOnly) "Outline" else "Fill", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (textLayer.isOutlineOnly) Icons.Default.BorderColor else Icons.Default.Brush,
                                contentDescription = "Toggle Outline",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    // Palette Color Dots
                    Row(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLowest,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palette.colors.forEachIndexed { idx, col ->
                            val isSelected = textLayer.colorIndex == idx
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(idx) }
                            )
                        }
                    }

                    // Bring to Front / Send to Back
                    IconButton(
                        onClick = onBringToFront,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Bring to Front",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onSendToBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Send to Back",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Rotate
                    IconButton(
                        onClick = { onRotate((textLayer.rotationDeg + 45f) % 360f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "Rotate 45°",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Expand Optics Tune (Opacity & Shadow)
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Optics Tune",
                            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Layer
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Text",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close / Deselect
                    IconButton(
                        onClick = onDeselect,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Deselect",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Granular Optics Expansion (Opacity & Elevation Drop Shadow)
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 4.dp, end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        // Typography Style Selector
                        Text(
                            text = "Display Typography Style",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active custom imported font chip
                            if (textLayer.customFontPath != null) {
                                FilterChip(
                                    selected = true,
                                    onClick = { onClearCustomFont() },
                                    label = { Text(textLayer.customFontName ?: "Custom Font", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove font",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }

                            // Import .ttf / .otf button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        fontPickerLauncher.launch(
                                            arrayOf(
                                                "font/*",
                                                "application/x-font-ttf",
                                                "application/x-font-otf",
                                                "application/octet-stream",
                                                "*/*"
                                            )
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Import Font",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Import Font",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            StudioTypographyStyle.entries.forEach { style ->
                                val isSelected = textLayer.typographyStyle == style && textLayer.customFontPath == null
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onClearCustomFont()
                                        onUpdateTypographyStyle(style)
                                    },
                                    label = { Text(style.displayName, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // Outline Stroke Width Slider (when Outline mode is active)
                        if (textLayer.isOutlineOnly) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BorderColor,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stroke ${textLayer.strokeWidthDp.toInt()}dp",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(90.dp)
                                )
                                Slider(
                                    value = textLayer.strokeWidthDp,
                                    onValueChange = onUpdateStrokeWidth,
                                    valueRange = 1.0f..12.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Atmospheric Edge Glow Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edge Glow ${textLayer.glowRadiusDp.toInt()}dp",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = textLayer.glowRadiusDp,
                                onValueChange = onUpdateGlowRadius,
                                valueRange = 0.0f..32.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.tertiary,
                                    activeTrackColor = MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }

                        // Opacity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Opacity ${(textLayer.opacity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = textLayer.opacity,
                                onValueChange = onUpdateOpacity,
                                valueRange = 0.05f..1.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Elevation Drop Shadow Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Elevation ${textLayer.shadowRadius.toInt()}dp",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = textLayer.shadowRadius,
                                onValueChange = onUpdateShadowRadius,
                                valueRange = 0.0f..40.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }

                        // Granular Letter Spacing / Tracking Slider (-4.0sp to +12.0sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tracking ${String.format("%+.1fsp", textLayer.letterSpacingSp)}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(100.dp)
                            )
                            Slider(
                                value = textLayer.letterSpacingSp,
                                onValueChange = {
                                    onUpdateLetterSpacing(it)
                                    ExpressiveMotionSpec.performSliderHaptic(haptic)
                                },
                                valueRange = -4.0f..12.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Granular Line Height / Vertical Lead Slider (0.75x to 1.60x)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatBold,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lead ${String.format("%.2fx", textLayer.lineHeightMultiplier)}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(100.dp)
                            )
                            Slider(
                                value = textLayer.lineHeightMultiplier,
                                onValueChange = {
                                    onUpdateLineHeight(it)
                                    ExpressiveMotionSpec.performSliderHaptic(haptic)
                                },
                                valueRange = 0.75f..1.60f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }

                        // Independent 2D Stretching (Horizontal & Vertical)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stretch X ${String.format("%.2fx", textLayer.scaleX)}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = textLayer.scaleX,
                                onValueChange = onUpdateScaleX,
                                valueRange = 0.25f..3.0f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stretch Y ${String.format("%.2fx", textLayer.scaleY)}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = textLayer.scaleY,
                                onValueChange = onUpdateScaleY,
                                valueRange = 0.25f..3.0f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
