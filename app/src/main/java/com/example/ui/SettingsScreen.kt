package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiGenerationState
import com.example.ai.AiProvider
import com.example.ai.DaylightContext
import com.example.ai.GeneratedAiPalette
import com.example.ui.components.FloatingPillTabRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MotionScale(val label: String, val description: String, val stiffness: Float, val damping: Float) {
    SNAPPY("Snappy", "Fast, crisp spring transitions", 800f, 0.75f),
    EXPRESSIVE("Expressive", "Fluid organic spring physics (Default)", 300f, 0.75f),
    GENTLE("Gentle", "Soft, flowing motion", 180f, 0.85f)
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class StaticThemePreset(val label: String, val description: String) {
    WARM_SAND("Warm Sand", "Earthy terracotta, warm clay, and cream tones"),
    NORDIC_SLATE("Nordic Slate", "Cool minimalist indigo, steel blue, and slate"),
    OLED_OBSIDIAN("OLED Obsidian", "Pure deep true-black OLED background with lilac accents")
}

enum class RenderResolutionPreset(val label: String, val width: Int, val height: Int) {
    FHD_PLUS("Full HD (1080p)", 1080, 2400),
    QHD_PLUS("2K QHD (1440p)", 1440, 3200),
    UHD_4K("4K UHD (2160p)", 2160, 3840),
    NATIVE_BOUNDS("Native Screen Bounds", 1080, 2400)
}

enum class ExportImageFormat(val label: String, val extension: String, val mimeType: String) {
    PNG("Lossless PNG", "png", "image/png"),
    WEBP("High-Efficiency WEBP", "webp", "image/webp")
}

enum class HapticStrength(val label: String) {
    OFF("Off"),
    SUBTLE("Subtle"),
    FIRM("Firm")
}

data class AppSettingsState(
    val dynamicMonetEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val staticThemePreset: StaticThemePreset = StaticThemePreset.WARM_SAND,
    val motionScale: MotionScale = MotionScale.EXPRESSIVE,
    val resolutionPreset: RenderResolutionPreset = RenderResolutionPreset.QHD_PLUS,
    val exportFormat: ExportImageFormat = ExportImageFormat.PNG,
    val antiAliasingEnabled: Boolean = true,
    val subSamplingEnabled: Boolean = false,
    val hapticStrength: HapticStrength = HapticStrength.FIRM,
    val hapticsEnabled: Boolean = true
)

enum class SettingsTab(val label: String, val icon: ImageVector) {
    PERSONALIZATION("Personalization", Icons.Default.Palette),
    AI_INTEGRATION("AI Integration", Icons.Default.AutoAwesome),
    ADVANCED("Advanced", Icons.Default.Tune),
    ABOUT("About", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WallpaperViewModel,
    settings: AppSettingsState,
    onUpdateSettings: (AppSettingsState) -> Unit,
    onResetDefaults: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SettingsTab.PERSONALIZATION) }
    val tabs = SettingsTab.entries

    LaunchedEffect(Unit) {
        viewModel.initAiKeyStorage(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (settings.hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onResetDefaults()
                        },
                        modifier = Modifier.testTag("settings_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Defaults"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Floating Pill Navigation Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FloatingPillTabRow(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        if (settings.hapticStrength != HapticStrength.OFF) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        selectedTab = tab
                    },
                    tabLabel = { it.label },
                    tabIcon = { tab, isSelected ->
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // Tab Content with animated transitions
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val isForward = targetState.ordinal > initialState.ordinal
                    val slideIn = slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f),
                        initialOffsetX = { if (isForward) it else -it }
                    ) + fadeIn()
                    val slideOut = slideOutHorizontally(
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f),
                        targetOffsetX = { if (isForward) -it else it }
                    ) + fadeOut()
                    slideIn togetherWith slideOut
                },
                label = "settings_tab_content",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                when (targetTab) {
                    SettingsTab.PERSONALIZATION -> PersonalizationTabContent(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings
                    )
                    SettingsTab.AI_INTEGRATION -> AiIntegrationTabContent(
                        viewModel = viewModel
                    )
                    SettingsTab.ADVANCED -> AdvancedTabContent(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings
                    )
                    SettingsTab.ABOUT -> AboutTabContent()
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: Personalization Content
// -------------------------------------------------------------
@Composable
private fun PersonalizationTabContent(
    settings: AppSettingsState,
    onUpdateSettings: (AppSettingsState) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Theming (Monet) ON/OFF Toggle
        SettingsCard(title = "Dynamic Theming (Monet)") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Monet Colors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (settings.dynamicMonetEnabled)
                            "Dynamically extracts color tokens from wallpaper & system palette"
                        else
                            "Using curated static minimalist color themes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.dynamicMonetEnabled,
                    onCheckedChange = { isEnabled ->
                        if (settings.hapticStrength != HapticStrength.OFF) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onUpdateSettings(settings.copy(dynamicMonetEnabled = isEnabled))
                    },
                    modifier = Modifier.testTag("toggle_dynamic_monet")
                )
            }
        }

        // Static Theme Presets (Active when Monet is OFF)
        if (!settings.dynamicMonetEnabled) {
            SettingsCard(title = "Curated Minimalist Theme") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StaticThemePreset.entries.forEach { preset ->
                        val isSelected = settings.staticThemePreset == preset
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (settings.hapticStrength != HapticStrength.OFF) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onUpdateSettings(settings.copy(staticThemePreset = preset))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Independent Theme Mode Selector (System / Light / Dark)
        SettingsCard(title = "Theme Mode") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Appearance Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = settings.themeMode == mode
                        FilledTonalButton(
                            onClick = {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(themeMode = mode))
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Motion & Physics Scale
        SettingsCard(title = "Motion Physics & Animation Scale") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MotionScale.entries.forEach { motion ->
                    val isSelected = settings.motionScale == motion
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(motionScale = motion))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = motion.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = motion.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Haptic Feedback Intensity
        SettingsCard(title = "Haptic Tactile Feedback") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vibration Intensity: ${settings.hapticStrength.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HapticStrength.entries.forEach { strength ->
                        val isSelected = settings.hapticStrength == strength
                        FilledTonalButton(
                            onClick = {
                                if (strength != HapticStrength.OFF) {
                                    val feedback = if (strength == HapticStrength.FIRM) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                    haptics.performHapticFeedback(feedback)
                                }
                                onUpdateSettings(settings.copy(hapticStrength = strength, hapticsEnabled = strength != HapticStrength.OFF))
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = strength.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// TAB 2: AI Integration (Moved from Home Viewport)
// -------------------------------------------------------------
@Composable
private fun AiIntegrationTabContent(
    viewModel: WallpaperViewModel
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val uiState = viewModel.uiState.value

    val currentProvider = uiState.selectedAiProvider
    var apiKeyInput by remember(currentProvider) { mutableStateOf(viewModel.getApiKey(currentProvider)) }
    var isKeyMasked by remember { mutableStateOf(true) }

    val availableModels = uiState.availableModels[currentProvider] ?: viewModel.aiService.getDefaultModels(currentProvider)
    val selectedModel = uiState.selectedModels[currentProvider] ?: availableModels.firstOrNull() ?: currentProvider.defaultModel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Provider Selector
        SettingsCard(title = "AI Provider Architecture (BYOK)") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Provider",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProvider.entries.forEach { provider ->
                        val isSelected = provider == currentProvider
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.setAiProvider(provider)
                            },
                            label = { Text(provider.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("ai_provider_${provider.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Secure API Key Input
        SettingsCard(title = "${currentProvider.displayName} API Key") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        viewModel.saveApiKey(currentProvider, it)
                    },
                    label = { Text("API Key (${currentProvider.keyPlaceholder})") },
                    placeholder = { Text(currentProvider.keyPlaceholder) },
                    visualTransformation = if (isKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isKeyMasked = !isKeyMasked }) {
                            Icon(
                                if (isKeyMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_api_key_input")
                )

                Text(
                    text = "Your API key is securely encrypted locally. It is never logged or sent to intermediate servers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dynamic Model Selector & Fetching (Fixes 404)
        SettingsCard(title = "Dynamic Model Selector") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Active Model: $selectedModel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Fetch live model list directly from ${currentProvider.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            viewModel.fetchModelsForProvider(currentProvider)
                        },
                        enabled = !uiState.isFetchingModels,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("fetch_models_button")
                    ) {
                        if (uiState.isFetchingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetch Models", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (uiState.modelFetchError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Error: ${uiState.modelFetchError}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Model Selection Chips
                Text(
                    text = "Select Target Model:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableModels.forEach { modelName ->
                        val isSelected = modelName == selectedModel
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.setSelectedModel(currentProvider, modelName)
                            },
                            label = {
                                Text(
                                    modelName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Generate AI Palette Test & Preview
        SettingsCard(title = "Test AI Palette Synthesis") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Daylight Lighting Context:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DaylightContext.entries.forEach { daylight ->
                        val isSelected = daylight == uiState.aiTestDaylight
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAiTestDaylight(daylight) },
                            label = { Text(daylight.label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        if (uiState.settings.hapticStrength != HapticStrength.OFF) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        viewModel.testGenerateAiPalette()
                    },
                    enabled = uiState.aiTestState !is AiGenerationState.Loading,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_ai_palette_button")
                ) {
                    if (uiState.aiTestState is AiGenerationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesizing Monotonic Ramp...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Palette Test", fontWeight = FontWeight.Bold)
                    }
                }

                // Test Output Preview
                when (val state = uiState.aiTestState) {
                    is AiGenerationState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.palette.paletteName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledTonalButton(
                                        onClick = { viewModel.applyGeneratedPalette(state.palette) },
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.testTag("apply_test_palette_button")
                                    ) {
                                        Text("Apply to Wallpaper", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    state.palette.colors.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is AiGenerationState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Test Failed: ${state.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// TAB 3: Advanced Tab Content
// -------------------------------------------------------------
@Composable
private fun AdvancedTabContent(
    settings: AppSettingsState,
    onUpdateSettings: (AppSettingsState) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Export Resolution Presets
        SettingsCard(title = "Wallpaper Export Resolution") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RenderResolutionPreset.entries.forEach { res ->
                    val isSelected = settings.resolutionPreset == res
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(resolutionPreset = res))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${res.width} x ${res.height} px",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export File Format
        SettingsCard(title = "Export Format") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExportImageFormat.entries.forEach { fmt ->
                    val isSelected = settings.exportFormat == fmt
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(exportFormat = fmt))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fmt.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = fmt.mimeType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quality & Anti-Aliasing Toggles
        SettingsCard(title = "Engine Graphics & Supersampling") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anti-Aliasing Filter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Smooth sub-pixel edge interpolation across all paths",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.antiAliasingEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(antiAliasingEnabled = it)) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2x Supersampling",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Renders canvas at 2x resolution before downscaling for ultra-crisp edges",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.subSamplingEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(subSamplingEnabled = it)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// TAB 4: About Tab Content
// -------------------------------------------------------------
@Composable
private fun AboutTabContent() {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var cloverTapCount by remember { mutableIntStateOf(0) }
    var isPulsing by remember { mutableStateOf(false) }
    val cloverScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.25f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
        label = "clover_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Clover Icon with spring pulse
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(96.dp)
                .scale(cloverScale)
                .clip(CircleShape)
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    cloverTapCount++
                    isPulsing = true
                }
                .testTag("about_clover_badge")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clover),
                    contentDescription = "Wallpaper Studio Clover Logo",
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        LaunchedEffect(isPulsing) {
            if (isPulsing) {
                delay(150)
                isPulsing = false
            }
        }

        Text(
            text = "Wallpaper Studio",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Version 2.4.1 Badge
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Version 2.4.1 • Material 3 Expressive",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Attribution Card: "Crafted by Phoenix with vibe coding"
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Crafted by Phoenix with vibe coding",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Official GitHub Repository Button
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                .testTag("github_repo_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Official GitHub Repository",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Repository",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Open Source License Button
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/blob/main/LICENSE")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                .testTag("license_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Open Source License",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Apache 2.0 / Open Source Software",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "View License",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            )
            content()
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
