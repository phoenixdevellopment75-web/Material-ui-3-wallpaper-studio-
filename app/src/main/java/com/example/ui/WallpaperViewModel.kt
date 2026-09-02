package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AspectRatioPreset
import com.example.engine.ProceduralRenderer
import com.example.engine.WallpaperParams
import com.example.engine.WallpaperPatternType
import com.example.export.ExportResult
import com.example.export.WallpaperExporter
import com.example.export.WallpaperTarget
import com.example.math.MathUtils
import com.example.palette.ColorPalette
import com.example.palette.PaletteEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import com.example.ai.AiGenerationState
import com.example.ai.AiKeyStorage
import com.example.ai.AiPaletteService
import com.example.ai.AiProvider
import com.example.ai.DaylightContext
import com.example.ai.GeneratedAiPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class InspectorTab(val label: String) {
    PATTERNS("Pattern"),
    MATH_CONTROLS("Tuning"),
    PALETTES("Colors"),
    AI_STUDIO("AI Studio"),
    FORMAT("Format")
}

data class WallpaperUiState(
    val params: WallpaperParams = WallpaperParams(),
    val previewBitmap: Bitmap? = null,
    val isGeneratingPreview: Boolean = false,
    val isFullscreenPreview: Boolean = false,
    val showLauncherMockup: Boolean = false,
    val showExportDialog: Boolean = false,
    val showColorPickerModal: Boolean = false,
    val activeColorStopIndex: Int = 0,
    val activeInspectorTab: InspectorTab = InspectorTab.PATTERNS,
    val isExporting: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val settings: AppSettingsState = AppSettingsState(),
    val snackbarMessage: String? = null,
    val isSuccessMessage: Boolean = true,
    val selectedAiProvider: AiProvider = AiProvider.GEMINI,
    val aiGenerationState: AiGenerationState = AiGenerationState.Idle,
    val aiMoodPrompt: String = "Warm Nordic Clay",
    val aiDaylightContext: DaylightContext = DaylightContext.TWILIGHT,
    val aiCustomPrompt: String = ""
)

class WallpaperViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperUiState())
    val uiState: StateFlow<WallpaperUiState> = _uiState.asStateFlow()

    private val aiService = AiPaletteService()
    private var aiKeyStorage: AiKeyStorage? = null

    private var previewRenderJob: Job? = null
    private var lastRenderParams: WallpaperParams? = null

    init {
        generatePreview(debounceMs = 0)
    }

    fun initAiKeyStorage(context: Context) {
        if (aiKeyStorage == null) {
            aiKeyStorage = AiKeyStorage.getInstance(context)
        }
    }

    fun setAiProvider(provider: AiProvider) {
        _uiState.update { it.copy(selectedAiProvider = provider) }
    }

    fun setAiMoodPrompt(mood: String) {
        _uiState.update { it.copy(aiMoodPrompt = mood) }
    }

    fun setAiDaylightContext(daylight: DaylightContext) {
        _uiState.update { it.copy(aiDaylightContext = daylight) }
    }

    fun setAiCustomPrompt(prompt: String) {
        _uiState.update { it.copy(aiCustomPrompt = prompt) }
    }

    fun getApiKey(provider: AiProvider): String {
        return aiKeyStorage?.getApiKey(provider) ?: ""
    }

    fun saveApiKey(provider: AiProvider, key: String) {
        aiKeyStorage?.saveApiKey(provider, key)
    }

    fun generateAiPalette() {
        val currentState = _uiState.value
        val provider = currentState.selectedAiProvider
        val apiKey = getApiKey(provider)
        val patternName = currentState.params.patternType.displayName
        val subTypeName = currentState.params.patternType.subTypes.getOrNull(currentState.params.subTypeIndex) ?: "Standard"
        val mood = currentState.aiMoodPrompt
        val daylight = currentState.aiDaylightContext
        val custom = currentState.aiCustomPrompt

        viewModelScope.launch {
            _uiState.update { it.copy(aiGenerationState = AiGenerationState.Loading) }
            val result = aiService.generatePalette(
                provider = provider,
                apiKey = apiKey,
                patternName = patternName,
                subTypeName = subTypeName,
                moodTag = mood,
                daylightContext = daylight,
                customPrompt = custom
            )

            result.fold(
                onSuccess = { generated ->
                    _uiState.update {
                        it.copy(
                            aiGenerationState = AiGenerationState.Success(generated),
                            snackbarMessage = "AI Palette '${generated.paletteName}' created!",
                            isSuccessMessage = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            aiGenerationState = AiGenerationState.Error(error.message ?: "Failed to generate AI palette"),
                            snackbarMessage = "AI Generation error: ${error.message}",
                            isSuccessMessage = false
                        )
                    }
                }
            )
        }
    }

    fun applyAiPalette(generated: GeneratedAiPalette) {
        val colorPalette = generated.toColorPalette()
        setPalette(colorPalette)
        _uiState.update {
            it.copy(
                snackbarMessage = "Applied AI Palette '${generated.paletteName}' to wallpaper",
                isSuccessMessage = true
            )
        }
    }

    fun updateSettings(settings: AppSettingsState) {
        _uiState.update { it.copy(settings = settings) }
    }

    fun openSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun resetToDefaults() {
        _uiState.update {
            it.copy(
                params = WallpaperParams(),
                settings = AppSettingsState()
            )
        }
        generatePreview(debounceMs = 0)
    }

    fun updateParams(transform: (WallpaperParams) -> WallpaperParams) {
        _uiState.update { current ->
            current.copy(params = transform(current.params))
        }
        generatePreview(debounceMs = 60)
    }

    fun randomizeSeed() {
        val newSeed = (MathUtils.FastRandom(System.nanoTime()).nextFloat() * 1000000).toLong()
        updateParams { it.copy(seed = newSeed) }
    }

    fun setPatternType(type: WallpaperPatternType) {
        updateParams {
            it.copy(
                patternType = type,
                subTypeIndex = 0
            )
        }
    }

    fun setSubType(index: Int) {
        updateParams { it.copy(subTypeIndex = index) }
    }

    fun setPalette(palette: ColorPalette) {
        updateParams { it.copy(palette = palette) }
    }

    fun setAspectRatio(aspect: AspectRatioPreset) {
        updateParams { it.copy(aspectRatio = aspect) }
    }

    fun updateColorStop(index: Int, newColor: Color) {
        val currentPalette = _uiState.value.params.palette
        val newColors = currentPalette.colors.toMutableList()
        if (index in newColors.indices) {
            newColors[index] = newColor
            val updatedPalette = currentPalette.copy(
                id = "custom_${System.currentTimeMillis()}",
                name = "Custom Palette",
                colors = newColors
            )
            setPalette(updatedPalette)
        }
    }

    fun addColorStop(color: Color) {
        val currentPalette = _uiState.value.params.palette
        if (currentPalette.colors.size < 8) {
            val newColors = currentPalette.colors + color
            val updatedPalette = currentPalette.copy(
                id = "custom_${System.currentTimeMillis()}",
                name = "Custom Palette",
                colors = newColors
            )
            setPalette(updatedPalette)
        }
    }

    fun removeColorStop(index: Int) {
        val currentPalette = _uiState.value.params.palette
        if (currentPalette.colors.size > 2 && index in currentPalette.colors.indices) {
            val newColors = currentPalette.colors.filterIndexed { i, _ -> i != index }
            val updatedPalette = currentPalette.copy(
                id = "custom_${System.currentTimeMillis()}",
                name = "Custom Palette",
                colors = newColors
            )
            setPalette(updatedPalette)
        }
    }

    fun applyDynamicTheme(primary: Color, secondary: Color, tertiary: Color, surface: Color, background: Color) {
        val monetPalette = PaletteEngine.createFromDynamicScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            surface = surface,
            background = background
        )
        setPalette(monetPalette)
    }

    fun applyAlgorithmicPalette(mode: String, baseColor: Color) {
        val palette = when (mode) {
            "monochromatic" -> PaletteEngine.generateMonochromatic(baseColor)
            "complementary" -> PaletteEngine.generateComplementary(baseColor)
            "triadic" -> PaletteEngine.generateTriadic(baseColor)
            "analogous" -> PaletteEngine.generateAnalogous(baseColor)
            else -> PaletteEngine.generateComplementary(baseColor)
        }
        setPalette(palette)
    }

    fun setActiveTab(tab: InspectorTab) {
        _uiState.update { it.copy(activeInspectorTab = tab) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreenPreview = !it.isFullscreenPreview) }
    }

    fun toggleLauncherMockup() {
        _uiState.update { it.copy(showLauncherMockup = !it.showLauncherMockup) }
    }

    fun showExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun showColorPicker(show: Boolean, stopIndex: Int = 0) {
        _uiState.update { it.copy(showColorPickerModal = show, activeColorStopIndex = stopIndex) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun saveToGallery(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, showExportDialog = false) }
            val res = _uiState.value.settings.resolutionPreset
            val result = WallpaperExporter.saveToGallery(
                context = context,
                params = _uiState.value.params,
                targetWidth = res.width,
                targetHeight = res.height
            )
            when (result) {
                is ExportResult.SavedToGallery -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            snackbarMessage = "Wallpaper saved in Pictures/Wallpapers (${res.width}x${res.height})!",
                            isSuccessMessage = true
                        )
                    }
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            snackbarMessage = "Save failed: ${result.message}",
                            isSuccessMessage = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isExporting = false) }
                }
            }
        }
    }

    fun setSystemWallpaper(context: Context, target: WallpaperTarget) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, showExportDialog = false) }
            val res = _uiState.value.settings.resolutionPreset
            val result = WallpaperExporter.applyAsSystemWallpaper(
                context = context,
                params = _uiState.value.params,
                target = target,
                targetWidth = res.width,
                targetHeight = res.height
            )
            when (result) {
                is ExportResult.AppliedToSystem -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            snackbarMessage = "Wallpaper successfully applied to ${target.label}!",
                            isSuccessMessage = true
                        )
                    }
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            snackbarMessage = "Set failed: ${result.message}",
                            isSuccessMessage = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isExporting = false) }
                }
            }
        }
    }

    fun shareWallpaper(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, showExportDialog = false) }
            val result = WallpaperExporter.createShareIntent(context, _uiState.value.params)
            when (result) {
                is ExportResult.Shared -> {
                    _uiState.update { it.copy(isExporting = false) }
                    val chooser = android.content.Intent.createChooser(result.intent, "Share Wallpaper")
                    chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            snackbarMessage = "Share failed: ${result.message}",
                            isSuccessMessage = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isExporting = false) }
                }
            }
        }
    }

    private fun generatePreview(debounceMs: Long) {
        previewRenderJob?.cancel()
        previewRenderJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            _uiState.update { it.copy(isGeneratingPreview = true) }

            val currentParams = _uiState.value.params
            // Dynamic preview resolution matching aspect ratio for responsive real-time generation (540x1200 or 720x1600)
            val aspect = currentParams.aspectRatio
            val previewWidth = 540
            val previewHeight = (previewWidth / aspect.ratio).toInt().coerceIn(360, 1200)

            val bitmap = ProceduralRenderer.renderToBitmap(previewWidth, previewHeight, currentParams)

            _uiState.update {
                it.copy(
                    previewBitmap = bitmap,
                    isGeneratingPreview = false
                )
            }
            lastRenderParams = currentParams
        }
    }
}
