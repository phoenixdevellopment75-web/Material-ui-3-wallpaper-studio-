package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palette.ColorPalette
import com.example.palette.GradientType
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Custom Palette Creator Modal Bottom Sheet.
 * Allows users to add, reorder, and remove color stops (2 to 6 stops),
 * tune colors with an interactive HSV and Hex picker, preview the continuous tonal ramp,
 * and save custom palettes to persistent storage.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomPaletteBuilderSheet(
    initialPalette: ColorPalette? = null,
    onSavePalette: (ColorPalette) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    var paletteName by remember {
        mutableStateOf(initialPalette?.name ?: "Custom Palette")
    }

    val stops = remember {
        mutableStateListOf<Color>().apply {
            if (initialPalette != null && initialPalette.colors.isNotEmpty()) {
                addAll(initialPalette.colors)
            } else {
                addAll(listOf(Color(0xFFE0E5FF), Color(0xFF90A4AE), Color(0xFF263238)))
            }
        }
    }

    var selectedStopIndex by remember { mutableIntStateOf(0) }
    var gradientType by remember {
        mutableStateOf(initialPalette?.gradientType ?: GradientType.LINEAR)
    }

    // Active color HSV state for selected stop
    val activeColor = stops.getOrElse(selectedStopIndex) { Color.Cyan }
    var hue by remember(selectedStopIndex, activeColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(activeColor.toArgb(), hsv)
        mutableFloatStateOf(hsv[0])
    }
    var saturation by remember(selectedStopIndex, activeColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(activeColor.toArgb(), hsv)
        mutableFloatStateOf(hsv[1])
    }
    var valueBrightness by remember(selectedStopIndex, activeColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(activeColor.toArgb(), hsv)
        mutableFloatStateOf(hsv[2])
    }

    fun updateColorFromHsv(h: Float, s: Float, v: Float) {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
        val newColor = Color(argb)
        if (selectedStopIndex in stops.indices) {
            stops[selectedStopIndex] = newColor
        }
    }

    var hexInputText by remember(selectedStopIndex, activeColor) {
        val argb = activeColor.toArgb()
        mutableStateOf(String.format("%06X", 0xFFFFFF and argb))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_palette_builder_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Colorize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Custom Palette Creator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Interactive HSV Ramp Generator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Palette Name Input
            OutlinedTextField(
                value = paletteName,
                onValueChange = { paletteName = it },
                label = { Text("Palette Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("palette_name_input")
            )

            // Live Continuous Tonal Ramp Preview Bar
            Text(
                text = "Continuous Tonal Ramp Preview",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(stops.toList()))
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
            )

            // Color Stop Pills with Reorder / Delete / Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color Stops (${stops.size}/6)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Add Stop Button (up to 6)
                    Button(
                        onClick = {
                            if (stops.size < 6) {
                                // Add stop interpolating between last two or complimentary
                                val last = stops.last()
                                val newColor = Color(
                                    red = (last.red * 0.8f).coerceIn(0f, 1f),
                                    green = (last.green * 0.9f).coerceIn(0f, 1f),
                                    blue = (last.blue * 1.1f).coerceIn(0f, 1f),
                                    alpha = 1f
                                )
                                stops.add(newColor)
                                selectedStopIndex = stops.size - 1
                            }
                        },
                        enabled = stops.size < 6,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stop", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Stops Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stops.forEachIndexed { index, color ->
                    val isSelected = index == selectedStopIndex
                    val animatedBorderColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        label = "stop_border_anim"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color,
                        border = BorderStroke(if (isSelected) 3.dp else 1.dp, animatedBorderColor),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedStopIndex = index
                            }
                    ) {
                        if (isSelected) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Stop Position Reordering & Deletion Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Move Left
                    IconButton(
                        onClick = {
                            if (selectedStopIndex > 0) {
                                val item = stops.removeAt(selectedStopIndex)
                                stops.add(selectedStopIndex - 1, item)
                                selectedStopIndex -= 1
                            }
                        },
                        enabled = selectedStopIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Move Left")
                    }

                    // Move Right
                    IconButton(
                        onClick = {
                            if (selectedStopIndex < stops.size - 1) {
                                val item = stops.removeAt(selectedStopIndex)
                                stops.add(selectedStopIndex + 1, item)
                                selectedStopIndex += 1
                            }
                        },
                        enabled = selectedStopIndex < stops.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Move Right")
                    }
                }

                // Delete Selected Stop
                Button(
                    onClick = {
                        if (stops.size > 2) {
                            stops.removeAt(selectedStopIndex)
                            selectedStopIndex = (selectedStopIndex - 1).coerceAtLeast(0)
                        }
                    },
                    enabled = stops.size > 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Stop", style = MaterialTheme.typography.labelSmall)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Interactive HSV Color Picker Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Fine-Tune Stop #${selectedStopIndex + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Saturation / Value 2D Touch Surface
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        val surfaceW = constraints.maxWidth.toFloat()
                        val surfaceH = constraints.maxHeight.toFloat()

                        // 2D HSV surface gradient: base color with saturation gradient, overlaid with black value gradient
                        val pureHueColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.White, pureHueColor)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black)
                                    )
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            saturation = (offset.x / surfaceW).coerceIn(0f, 1f)
                                            valueBrightness = (1f - (offset.y / surfaceH)).coerceIn(0f, 1f)
                                            updateColorFromHsv(hue, saturation, valueBrightness)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            saturation = (change.position.x / surfaceW).coerceIn(0f, 1f)
                                            valueBrightness = (1f - (change.position.y / surfaceH)).coerceIn(0f, 1f)
                                            updateColorFromHsv(hue, saturation, valueBrightness)
                                        }
                                    )
                                }
                        )

                        // Draggable Pointer Thumb
                        val thumbX = (saturation * surfaceW).roundToInt()
                        val thumbY = ((1f - valueBrightness) * surfaceH).roundToInt()

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(thumbX - 12.dp.roundToPx(), thumbY - 12.dp.roundToPx()) }
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .background(activeColor)
                        )
                    }

                    // 2. Hue Slider (0°..360°)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Hue Spectrum", style = MaterialTheme.typography.bodySmall)
                            Text(text = "${hue.roundToInt()}°", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Red, Color.Yellow, Color.Green,
                                            Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                        )
                                    )
                                )
                        )
                        Slider(
                            value = hue,
                            onValueChange = {
                                hue = it
                                updateColorFromHsv(hue, saturation, valueBrightness)
                            },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeColor,
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent
                            )
                        )
                    }

                    // 3. Hex Code Input & Quick Swatches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hexInputText,
                            onValueChange = { input ->
                                hexInputText = input.take(6).uppercase()
                                if (hexInputText.length == 6) {
                                    runCatching {
                                        val parsed = android.graphics.Color.parseColor("#$hexInputText")
                                        val c = Color(parsed)
                                        stops[selectedStopIndex] = c
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(parsed, hsv)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        valueBrightness = hsv[2]
                                    }
                                }
                            },
                            label = { Text("Hex Color") },
                            prefix = { Text("#") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )

                        // Current swatch preview box
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(activeColor)
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        )
                    }
                }
            }

            // Save Palette Action Button
            Button(
                onClick = {
                    val finalPalette = ColorPalette(
                        id = initialPalette?.id ?: UUID.randomUUID().toString(),
                        name = paletteName.ifBlank { "My Custom Palette" },
                        colors = stops.toList(),
                        gradientType = gradientType,
                        isDarkBackground = stops.firstOrNull()?.let { it.luminance() < 0.5f } ?: true
                    )
                    onSavePalette(finalPalette)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_custom_palette_button"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Palette to My Palettes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Color luminance helper
private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
