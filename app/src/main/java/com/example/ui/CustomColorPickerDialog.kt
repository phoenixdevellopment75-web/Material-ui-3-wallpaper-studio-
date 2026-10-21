package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

/**
 * State holder that encapsulates HSV color components independently.
 *
 * Prevents the critical HSV reset bug where touching or releasing a slider
 * recalculates HSV from RGB and loses the chosen Hue (especially when Saturation or Value approaches zero).
 */
class ColorPickerState(
    initialHue: Float = 0f,
    initialSaturation: Float = 1f,
    initialValue: Float = 1f
) {
    var hue by mutableFloatStateOf(initialHue.coerceIn(0f, 360f))
    var saturation by mutableFloatStateOf(initialSaturation.coerceIn(0f, 1f))
    var valueBrightness by mutableFloatStateOf(initialValue.coerceIn(0f, 1f))

    val currentColor: Color
        get() {
            val argb = android.graphics.Color.HSVToColor(
                floatArrayOf(hue, saturation, valueBrightness)
            )
            return Color(argb)
        }

    fun updateHsv(h: Float = hue, s: Float = saturation, v: Float = valueBrightness) {
        hue = h.coerceIn(0f, 360f)
        saturation = s.coerceIn(0f, 1f)
        valueBrightness = v.coerceIn(0f, 1f)
    }

    fun setFromColor(color: Color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        // If color has chroma (saturation > 0.04 and value > 0.04), track hue;
        // otherwise preserve the previously chosen hue so grey/black doesn't snap to orange.
        if (hsv[1] > 0.04f && hsv[2] > 0.04f) {
            hue = hsv[0]
        }
        saturation = hsv[1]
        valueBrightness = hsv[2]
    }

    companion object {
        val Saver: Saver<ColorPickerState, *> = listSaver(
            save = { listOf(it.hue, it.saturation, it.valueBrightness) },
            restore = {
                ColorPickerState(
                    initialHue = it[0],
                    initialSaturation = it[1],
                    initialValue = it[2]
                )
            }
        )

        fun fromColor(color: Color): ColorPickerState {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(color.toArgb(), hsv)
            return ColorPickerState(
                initialHue = hsv[0],
                initialSaturation = hsv[1],
                initialValue = hsv[2]
            )
        }
    }
}

@Composable
fun rememberColorPickerState(initialColor: Color): ColorPickerState {
    return rememberSaveable(saver = ColorPickerState.Saver) {
        ColorPickerState.fromColor(initialColor)
    }
}

/**
 * Embedded HSV color picker surface.
 * Decouples drag gesture tracking from RGB recalculations to guarantee zero hue reset.
 */
@Composable
fun HsvColorPicker(
    state: ColorPickerState,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Saturation / Value 2D Touch Surface
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
        ) {
            val surfaceW = constraints.maxWidth.toFloat()
            val surfaceH = constraints.maxHeight.toFloat()

            val pureHueColor = Color(
                android.graphics.Color.HSVToColor(floatArrayOf(state.hue, 1f, 1f))
            )

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
                    .pointerInput(surfaceW, surfaceH) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val s = (offset.x / surfaceW).coerceIn(0f, 1f)
                                val v = (1f - (offset.y / surfaceH)).coerceIn(0f, 1f)
                                state.updateHsv(s = s, v = v)
                                onColorChanged(state.currentColor)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val s = (change.position.x / surfaceW).coerceIn(0f, 1f)
                                val v = (1f - (change.position.y / surfaceH)).coerceIn(0f, 1f)
                                state.updateHsv(s = s, v = v)
                                onColorChanged(state.currentColor)
                            }
                        )
                    }
            )

            // Draggable Pointer Thumb
            val thumbX = (state.saturation * surfaceW).roundToInt()
            val thumbY = ((1f - state.valueBrightness) * surfaceH).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbX - 12.dp.roundToPx(), thumbY - 12.dp.roundToPx()) }
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .background(state.currentColor)
            )
        }

        // 2. Hue Slider (0°..360°)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hue Spectrum",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${state.hue.roundToInt()}°",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF0000),
                                Color(0xFFFFFF00),
                                Color(0xFF00FF00),
                                Color(0xFF00FFFF),
                                Color(0xFF0000FF),
                                Color(0xFFFF00FF),
                                Color(0xFFFF0000)
                            )
                        )
                    )
            )

            Slider(
                value = state.hue,
                onValueChange = { newHue ->
                    state.updateHsv(h = newHue)
                    onColorChanged(state.currentColor)
                },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hue_spectrum_slider")
            )
        }
    }
}

/**
 * Dedicated Custom Color Picker Dialog with isolated state persistence and exact HSV integrity.
 */
@Composable
fun CustomColorPickerDialog(
    initialColor: Color,
    onColorConfirmed: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    title: String = "Select Color"
) {
    val state = rememberColorPickerState(initialColor)

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Color Preview Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .background(state.currentColor)
                    )
                }

                HsvColorPicker(
                    state = state,
                    onColorChanged = { /* state holds live color */ }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onColorConfirmed(state.currentColor)
                            onDismissRequest()
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
