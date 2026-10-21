package com.example.ui

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.AiKeyStorage
import com.example.ai.AiPaletteCandidate
import com.example.ai.AiPaletteService
import com.example.ai.AiProvider
import com.example.ai.DaylightContext
import com.example.palette.ColorPalette
import kotlinx.coroutines.launch

/**
 * Modernized AI Palette Studio.
 * Connects to Google Gemini (and other providers) to generate 2 to 3 alternate
 * 5-stop colorway candidates from natural language prompts, with real-time preview,
 * one-tap wallpaper application, and manual swatch stop refinement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPaletteStudioSheet(
    activePalette: ColorPalette,
    patternName: String,
    onApplyPalette: (ColorPalette) -> Unit,
    onEditStops: (ColorPalette) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // AI Provider and BYOK Key state
    var selectedProvider by remember { mutableStateOf(AiProvider.GEMINI) }
    var apiKey by remember {
        mutableStateOf(AiKeyStorage.getInstance(context).getApiKey(selectedProvider))
    }
    var showApiKeyField by remember { mutableStateOf(apiKey.isEmpty()) }

    // Prompt & Context state
    var promptText by remember { mutableStateOf("Nordic sunrise over misty pines") }
    var daylightContext by remember { mutableStateOf(DaylightContext.TWILIGHT) }

    // Generation & Candidates state
    var isLoading by remember { mutableStateOf(false) }
    var candidates by remember {
        mutableStateOf<List<AiPaletteCandidate>>(
            AiPaletteService.generateLocalFallbackCandidates(promptText, "Nordic", daylightContext)
        )
    }
    var selectedCandidateIndex by remember { mutableStateOf<Int?>(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickPrompts = listOf(
        "Nordic sunrise over misty pines",
        "Cyberpunk Tokyo rain",
        "Warm desert dune golden hour",
        "Earthy matcha tea garden",
        "Midnight deep space OLED"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
        contentWindowInsets = { WindowInsets.navigationBars },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        modifier = Modifier.testTag("ai_palette_studio_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Palette Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Generate custom Monet harmony ramps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_ai_palette_studio_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Provider selection row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiProvider.values().forEach { provider ->
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = {
                            selectedProvider = provider
                            val saved = AiKeyStorage.getInstance(context).getApiKey(provider)
                            apiKey = saved
                            if (saved.isEmpty()) showApiKeyField = true
                        },
                        label = { Text(provider.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Optional BYOK API Key input
            AnimatedVisibility(visible = showApiKeyField || apiKey.isEmpty()) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            AiKeyStorage.getInstance(context).saveApiKey(selectedProvider, it)
                        },
                        label = { Text("${selectedProvider.displayName} API Key (BYOK)") },
                        placeholder = { Text(selectedProvider.keyPlaceholder) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null)
                        },
                        trailingIcon = {
                            if (apiKey.isNotEmpty()) {
                                IconButton(onClick = { showApiKeyField = false }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save Key")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_api_key_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Text(
                        text = "Keys are securely stored on device. Defaults to offline harmonic generation if empty.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Natural Language Prompt Field
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("Color Inspiration Prompt") },
                placeholder = { Text("e.g. Nordic sunrise over misty pines") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                },
                trailingIcon = {
                    if (promptText.isNotEmpty()) {
                        IconButton(onClick = { promptText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_prompt_text_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Quick Prompt Inspiration Chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { qp ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { promptText = qp }
                    ) {
                        Text(
                            text = qp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Daylight Context Selector
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Daylight Lighting Context",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DaylightContext.values().forEach { dl ->
                    FilterChip(
                        selected = daylightContext == dl,
                        onClick = { daylightContext = dl },
                        label = { Text(dl.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Generate Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val res = AiPaletteService.default.generatePaletteCandidates(
                            provider = selectedProvider,
                            apiKey = apiKey,
                            model = selectedProvider.defaultModel,
                            patternName = patternName,
                            subTypeName = "Procedural",
                            moodTag = promptText.ifEmpty { "Harmonic" },
                            daylightContext = daylightContext,
                            customPrompt = promptText
                        )
                        isLoading = false
                        res.onSuccess { list ->
                            candidates = list
                            selectedCandidateIndex = 0
                            list.firstOrNull()?.let { cand ->
                                onApplyPalette(cand.toColorPalette())
                            }
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Failed to generate candidates"
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("ai_generate_palettes_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Architecting Colorways...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Colorways with AI", fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(18.dp))

            // Candidates Preview Section (2 to 3 alternate colorways)
            Text(
                text = "Colorway Candidates (${candidates.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap any candidate to apply to active wallpaper",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                candidates.forEachIndexed { index, candidate ->
                    val isSelected = selectedCandidateIndex == index
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                selectedCandidateIndex = index
                                onApplyPalette(candidate.toColorPalette())
                            }
                            .testTag("ai_candidate_card_$index")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = candidate.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (candidate.description.isNotEmpty()) {
                                        Text(
                                            text = candidate.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 5-stop color ramp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                candidate.colors.forEach { col ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(col)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Edit Stops Action Button
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(
                onClick = {
                    val activeCand = selectedCandidateIndex?.let { candidates.getOrNull(it) }
                    val palToEdit = activeCand?.toColorPalette() ?: activePalette
                    onEditStops(palToEdit)
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_stops_button")
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Individual Swatch Stops")
            }
        }
    }
}
