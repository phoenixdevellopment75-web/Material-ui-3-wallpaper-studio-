package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiGenerationState
import com.example.ai.AiProvider
import com.example.ai.DaylightContext
import com.example.ai.GeneratedAiPalette
import com.example.ui.WallpaperUiState
import com.example.ui.WallpaperViewModel

val MOOD_PRESETS = listOf(
    "Warm Nordic Clay",
    "OLED Deep Space",
    "Botanical Sage",
    "Terracotta Dawn",
    "Desert Dune Solstice",
    "Earthy Matcha",
    "Cyberpunk Pastel",
    "Mineral Quartz",
    "Lavender Mist",
    "Sunlit Amber"
)

@Composable
fun AiStudioTab(
    state: WallpaperUiState,
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.initAiKeyStorage(context)
    }

    var keyInput by remember(state.selectedAiProvider) {
        mutableStateOf(viewModel.getApiKey(state.selectedAiProvider))
    }
    var keyVisible by remember { mutableStateOf(false) }
    var keySavedToast by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Studio",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BYOK AI Palette & Harmony",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Synthesizes 5-step M3 tonal ramps matching active pattern geometry & daylight lighting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Provider Selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "AI Model Provider",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiProvider.values().forEach { provider ->
                    val isSelected = provider == state.selectedAiProvider
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setAiProvider(provider)
                            keyInput = viewModel.getApiKey(provider)
                        },
                        label = { Text(provider.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    )
                }
            }
        }

        // 3. BYOK API Key Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "API Key (${state.selectedAiProvider.displayName})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (viewModel.getApiKey(state.selectedAiProvider).isNotEmpty()) "Key Configured" else "Optional (Local Fallback)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (viewModel.getApiKey(state.selectedAiProvider).isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                placeholder = { Text(state.selectedAiProvider.keyPlaceholder, fontSize = 13.sp) },
                singleLine = true,
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (keyVisible) "Hide Key" else "Show Key"
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.saveApiKey(state.selectedAiProvider, keyInput)
                                focusManager.clearFocus()
                                keySavedToast = true
                            },
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Save Key",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = "Key Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.saveApiKey(state.selectedAiProvider, keyInput)
                    focusManager.clearFocus()
                }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input")
            )
        }

        // 4. Mood Preset Selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Mood & Aesthetic Vibe",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MOOD_PRESETS.forEach { mood ->
                    val isSelected = mood == state.aiMoodPrompt
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAiMoodPrompt(mood) },
                        label = { Text(mood, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = CircleShape
                    )
                }
            }
        }

        // 5. Daylight Lighting Context
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Daylight Lighting Context",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DaylightContext.values().forEach { daylight ->
                    val isSelected = daylight == state.aiDaylightContext
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAiDaylightContext(daylight) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LightMode,
                                contentDescription = daylight.label,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(daylight.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = CircleShape
                    )
                }
            }
        }

        // 6. Custom Prompt Text Field
        OutlinedTextField(
            value = state.aiCustomPrompt,
            onValueChange = { viewModel.setAiCustomPrompt(it) },
            placeholder = { Text("Optional custom vibe (e.g. 'high-contrast matcha with plum accents')") },
            label = { Text("Custom Vibe Instructions") },
            maxLines = 2,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // 7. Generate Action Button
        Button(
            onClick = {
                viewModel.generateAiPalette()
            },
            enabled = state.aiGenerationState !is AiGenerationState.Loading,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_ai_palette_button")
        ) {
            if (state.aiGenerationState is AiGenerationState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Synthesizing Harmony Ramp...")
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate AI Harmony Palette",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // 8. Result Preview Card
        AnimatedVisibility(
            visible = state.aiGenerationState is AiGenerationState.Success,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (state.aiGenerationState is AiGenerationState.Success) {
                val generated = state.aiGenerationState.palette
                GeneratedPaletteCard(
                    generated = generated,
                    onApply = { viewModel.applyAiPalette(generated) }
                )
            }
        }
    }
}

@Composable
fun GeneratedPaletteCard(
    generated: GeneratedAiPalette,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = generated.paletteName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "5-Step Monotonic M3 Tonal Ramp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "Generated",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 5 Tonal Color Swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp)),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                generated.colors.forEachIndexed { index, color ->
                    val hex = generated.hexCodes.getOrNull(index) ?: ""
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .background(color),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = hex.takeLast(6),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Apply Button
            Button(
                onClick = onApply,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("apply_ai_palette_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply to Active Wallpaper", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
