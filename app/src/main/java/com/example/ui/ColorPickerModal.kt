package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palette.PaletteEngine

@Composable
fun ColorPickerModal(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val hsv = remember(initialColor) {
        val array = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.hashCode(), array)
        array
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }

    val currentColor = remember(hue, saturation, value) {
        PaletteEngine.colorFromHsv(hue, saturation, value)
    }

    var hexText by remember(currentColor) {
        mutableStateOf(PaletteEngine.colorToHex(currentColor))
    }

    val haptics = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color Inspector",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_color_picker_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color Preview Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(currentColor)
                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    )

                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            hexText = input
                            if (input.startsWith("#") && (input.length == 7 || input.length == 9)) {
                                val parsed = PaletteEngine.hexToColor(input)
                                val array = FloatArray(3)
                                android.graphics.Color.colorToHSV(parsed.hashCode(), array)
                                hue = array[0]
                                saturation = array[1]
                                value = array[2]
                            }
                        },
                        label = { Text("Hex Code") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hex_color_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Hue Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hue", style = MaterialTheme.typography.labelMedium)
                        Text("${hue.toInt()}°", style = MaterialTheme.typography.labelSmall)
                    }
                    val hueGradient = remember {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Red, Color.Yellow, Color.Green,
                                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(hueGradient)
                    )
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hue_slider")
                    )
                }

                // Saturation Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saturation", style = MaterialTheme.typography.labelMedium)
                        Text("${(saturation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saturation_slider")
                    )
                }

                // Value / Brightness Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Luminance / Value", style = MaterialTheme.typography.labelMedium)
                        Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = value,
                        onValueChange = {
                            value = it
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("value_slider")
                    )
                }

                // Quick Palette Swatches
                Text("Harmonic Swatches", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val swatches = listOf(
                        Color(0xFF00F2FE),
                        Color(0xFFFF3366),
                        Color(0xFFFFD23F),
                        Color(0xFF6BFFB8),
                        Color(0xFFE0AAFF),
                        Color(0xFFF78A33),
                        Color(0xFF050505),
                        Color(0xFFFFFFFF)
                    )
                    swatches.forEach { swatch ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    val array = FloatArray(3)
                                    android.graphics.Color.colorToHSV(swatch.hashCode(), array)
                                    hue = array[0]
                                    saturation = array[1]
                                    value = array[2]
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColor)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_color_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Apply Color")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_color_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
