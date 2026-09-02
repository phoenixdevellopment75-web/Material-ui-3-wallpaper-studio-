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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

import com.example.ui.components.FloatingPillTabRow

enum class MotionScale(val label: String, val description: String, val stiffness: Float, val damping: Float) {
    SNAPPY("Snappy", "Fast, crisp spring transitions", 800f, 0.75f),
    EXPRESSIVE("Expressive", "Fluid organic spring physics (Default)", 300f, 0.75f),
    GENTLE("Gentle", "Soft, flowing motion", 180f, 0.85f)
}

enum class AppThemeMode(val label: String, val description: String) {
    SYSTEM_MONET("Monet Engine", "Dynamic Material You accent theming"),
    DARK("Dark Theme", "Rich contrast dark surfaces"),
    LIGHT("Light Theme", "Crisp, bright light surfaces")
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
    val motionScale: MotionScale = MotionScale.EXPRESSIVE,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM_MONET,
    val resolutionPreset: RenderResolutionPreset = RenderResolutionPreset.QHD_PLUS,
    val exportFormat: ExportImageFormat = ExportImageFormat.PNG,
    val antiAliasingEnabled: Boolean = true,
    val subSamplingEnabled: Boolean = false,
    val hapticStrength: HapticStrength = HapticStrength.FIRM,
    val hapticsEnabled: Boolean = true
)

enum class SettingsTab(val label: String, val icon: ImageVector) {
    PERSONALIZATION("Personalization", Icons.Default.Palette),
    ADVANCED("Advanced", Icons.Default.Tune),
    ABOUT("About", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsState,
    onUpdateSettings: (AppSettingsState) -> Unit,
    onResetDefaults: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var selectedTab by remember { mutableStateOf(SettingsTab.PERSONALIZATION) }
    val tabs = SettingsTab.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings & Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.90f)
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

            // Shared-axis slide and fade transitions between sub-menus
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val isForward = targetState.ordinal > initialState.ordinal
                    val slideIn = slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
                        initialOffsetX = { if (isForward) it else -it }
                    ) + fadeIn()
                    val slideOut = slideOutHorizontally(
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
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
                    SettingsTab.ADVANCED -> AdvancedTabContent(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings,
                        onResetDefaults = onResetDefaults
                    )
                    SettingsTab.ABOUT -> AboutTabContent(
                        settings = settings
                    )
                }
            }
        }
    }
}

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
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Theme Selection
        SettingsSectionHeader(title = "DYNAMIC THEMING & ACCENT", icon = Icons.Default.Palette)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    val isSelected = settings.themeMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(themeMode = mode))
                            }
                            .testTag("theme_${mode.name.lowercase()}"),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.description,
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

        // Motion Scale Adjustments
        SettingsSectionHeader(title = "UI MOTION SCALE & SPRING PHYSICS", icon = Icons.Default.Tune)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MotionScale.entries.forEach { option ->
                    val isSelected = settings.motionScale == option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(motionScale = option))
                            }
                            .testTag("motion_${option.name.lowercase()}"),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = option.description,
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

        // Haptic Feedback Slider
        SettingsSectionHeader(title = "HAPTIC FEEDBACK STRENGTH", icon = Icons.Default.Tune)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Vibration Intensity",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        settings.hapticStrength.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                val sliderValue = when (settings.hapticStrength) {
                    HapticStrength.OFF -> 0f
                    HapticStrength.SUBTLE -> 1f
                    HapticStrength.FIRM -> 2f
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { newVal ->
                        val strength = when (newVal.toInt()) {
                            0 -> HapticStrength.OFF
                            1 -> HapticStrength.SUBTLE
                            else -> HapticStrength.FIRM
                        }
                        if (strength != HapticStrength.OFF) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onUpdateSettings(
                            settings.copy(
                                hapticStrength = strength,
                                hapticsEnabled = (strength != HapticStrength.OFF)
                            )
                        )
                    },
                    valueRange = 0f..2f,
                    steps = 1,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("haptic_strength_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Off", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Subtle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Firm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AdvancedTabContent(
    settings: AppSettingsState,
    onUpdateSettings: (AppSettingsState) -> Unit,
    onResetDefaults: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Canvas Resolution & Export Scaling
        SettingsSectionHeader(title = "CANVAS RESOLUTION & EXPORT SCALING", icon = Icons.Default.HighQuality)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RenderResolutionPreset.entries.forEach { preset ->
                    val isSelected = settings.resolutionPreset == preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(resolutionPreset = preset))
                            }
                            .testTag("resolution_${preset.name.lowercase()}"),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preset.width} × ${preset.height} px",
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

        // Export Format Selector
        SettingsSectionHeader(title = "EXPORT IMAGE FORMAT", icon = Icons.Default.Tune)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportImageFormat.entries.forEach { format ->
                    val isSelected = settings.exportFormat == format
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (settings.hapticStrength != HapticStrength.OFF) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onUpdateSettings(settings.copy(exportFormat = format))
                            }
                            .testTag("format_${format.name.lowercase()}"),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = format.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
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

        // Rendering Quality & Anti-Aliasing Toggles
        SettingsSectionHeader(title = "ENGINE QUALITY & SAMPLING FLAGS", icon = Icons.Default.HighQuality)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Multi-pass Anti-Aliasing",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Smooth sub-pixel edge filtering on vector contours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.antiAliasingEnabled,
                        onCheckedChange = {
                            if (settings.hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onUpdateSettings(settings.copy(antiAliasingEnabled = it))
                        },
                        modifier = Modifier.testTag("antialiasing_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Vector Sub-sampling",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Adaptive step tessellation for intricate curves",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.subSamplingEnabled,
                        onCheckedChange = {
                            if (settings.hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onUpdateSettings(settings.copy(subSamplingEnabled = it))
                        },
                        modifier = Modifier.testTag("subsampling_switch")
                    )
                }
            }
        }

        // Reset Engine Defaults
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    if (settings.hapticStrength != HapticStrength.OFF) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onResetDefaults()
                }
                .testTag("reset_defaults_button"),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Column {
                    Text(
                        "Reset Engine & Math Defaults",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Restore factory mathematical parameters and palettes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AboutTabContent(
    settings: AppSettingsState
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var isCloverPulsing by remember { mutableStateOf(false) }
    val cloverScale by animateFloatAsState(
        targetValue = if (isCloverPulsing) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 300f),
        label = "clover_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Title & Interactive Pulsing M3 Clover Badge
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(cloverScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            if (settings.hapticStrength != HapticStrength.OFF) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            isCloverPulsing = true
                        }
                        .testTag("interactive_clover_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_clover),
                        contentDescription = "Material 3 Clover Icon",
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Reset pulse after animation
                if (isCloverPulsing) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(200)
                        isCloverPulsing = false
                    }
                }

                Text(
                    text = "Wallpaper Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Version 2.4.0 • Material You",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Vibe Coding Attribution
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Crafted by Phoenix with vibe coding",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Architectural Details Breakdown
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Architecture & Mathematical Core",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "• 100% deterministic, local-first Kotlin math engine.\n" +
                    "• Multi-pass ambient elevation shadows via Paint.setShadowLayer().\n" +
                    "• Continuous 2D scalar fields with Marching Squares isoline extraction.\n" +
                    "• Polar scallop geometry with smooth harmonic lobe curves.\n" +
                    "• Strict monotonic luminance color matrix enforcing 8-12% lightness steps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        // External Links & Open Source
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                            context.startActivity(intent)
                        }
                        .testTag("github_link_button"),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GitHub Repository & Source", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"))
                            context.startActivity(intent)
                        }
                        .testTag("license_link_button"),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Apache 2.0 Open Source License", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )
    }
}
