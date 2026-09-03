package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiGenerationState
import com.example.ai.AiKeyStorage
import com.example.ai.AiPaletteService
import com.example.ai.AiProvider
import com.example.ai.DaylightContext
import com.example.ai.GeneratedAiPalette
import com.example.engine.AspectRatioPreset
import com.example.engine.CustomCanvasShape
import com.example.engine.CustomShapeType
import com.example.engine.ProceduralRenderer
import com.example.engine.WallpaperParams
import com.example.engine.WallpaperPatternType
import com.example.export.ExportResult
import com.example.export.WallpaperExporter
import com.example.export.WallpaperTarget
import com.example.math.MathUtils
import com.example.palette.ColorPalette
import com.example.palette.PaletteEngine
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WallpaperUiState(
    val params: WallpaperParams = WallpaperParams(),
    val previewBitmap: Bitmap? = null,
    val isGeneratingPreview: Boolean = false,
    val isFullscreenPreview: Boolean = false,
    val showLauncherMockup: Boolean = false,
    val showExportDialog: Boolean = false,
    val showStyleSheet: Boolean = false,
    val showPaletteSheet: Boolean = false,
    val showAddShapeSheet: Boolean = false,
    val showColorPickerModal: Boolean = false,
    val activeColorStopIndex: Int = 0,
    val selectedShapeId: String? = null,
    val isExporting: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val settings: AppSettingsState = AppSettingsState(),
    val snackbarMessage: String? = null,
    val isSuccessMessage: Boolean = true,
    // AI Integration Settings States
    val selectedAiProvider: AiProvider = AiProvider.GEMINI,
    val availableModels: Map<AiProvider, List<String>> = emptyMap(),
    val selectedModels: Map<AiProvider, String> = emptyMap(),
    val isFetchingModels: Boolean = false,
    val modelFetchError: String? = null,
    val aiTestState: AiGenerationState = AiGenerationState.Idle,
    val aiTestDaylight: DaylightContext = DaylightContext.TWILIGHT,
    val aiTestMood: String = "Warm Nordic Clay"
)

class WallpaperViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperUiState())
    val uiState: StateFlow<WallpaperUiState> = _uiState.asStateFlow()

    val aiService = AiPaletteService()
    private var aiKeyStorage: AiKeyStorage? = null

    private var previewRenderJob: Job? = null
    private var lastRenderParams: WallpaperParams? = null

    init {
        // Prepopulate default models
        val initialModels = mutableMapOf<AiProvider, List<String>>()
        val initialSelected = mutableMapOf<AiProvider, String>()
        for (provider in AiProvider.entries) {
            val defaults = aiService.getDefaultModels(provider)
            initialModels[provider] = defaults
            initialSelected[provider] = defaults.firstOrNull() ?: provider.defaultModel
        }
        _uiState.update {
            it.copy(
                availableModels = initialModels,
                selectedModels = initialSelected
            )
        }
        generatePreview(debounceMs = 0)
    }

    fun initAiKeyStorage(context: Context) {
        if (aiKeyStorage == null) {
            val storage = AiKeyStorage.getInstance(context)
            aiKeyStorage = storage
            // Restore stored models
            val restoredSelected = _uiState.value.selectedModels.toMutableMap()
            for (provider in AiProvider.entries) {
                val stored = storage.getSelectedModel(provider)
                if (stored.isNotBlank()) {
                    restoredSelected[provider] = stored
                }
            }
            _uiState.update { it.copy(selectedModels = restoredSelected) }
        }
    }

    fun getApiKey(provider: AiProvider): String {
        return aiKeyStorage?.getApiKey(provider) ?: ""
    }

    fun saveApiKey(provider: AiProvider, key: String) {
        aiKeyStorage?.saveApiKey(provider, key)
    }

    fun setAiProvider(provider: AiProvider) {
        _uiState.update { it.copy(selectedAiProvider = provider, modelFetchError = null) }
    }

    fun setSelectedModel(provider: AiProvider, model: String) {
        aiKeyStorage?.saveSelectedModel(provider, model)
        val updated = _uiState.value.selectedModels.toMutableMap()
        updated[provider] = model
        _uiState.update { it.copy(selectedModels = updated) }
    }

    fun fetchModelsForProvider(provider: AiProvider) {
        val apiKey = getApiKey(provider)
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingModels = true, modelFetchError = null) }
            val result = aiService.fetchAvailableModels(provider, apiKey)
            result.fold(
                onSuccess = { models ->
                    val updatedMap = _uiState.value.availableModels.toMutableMap()
                    updatedMap[provider] = models
                    val selectedMap = _uiState.value.selectedModels.toMutableMap()
                    val currentSelected = selectedMap[provider]
                    if (currentSelected !in models) {
                        val newDefault = models.firstOrNull() ?: provider.defaultModel
                        selectedMap[provider] = newDefault
                        aiKeyStorage?.saveSelectedModel(provider, newDefault)
                    }
                    _uiState.update {
                        it.copy(
                            isFetchingModels = false,
                            availableModels = updatedMap,
                            selectedModels = selectedMap,
                            snackbarMessage = "Fetched ${models.size} models from ${provider.displayName}"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isFetchingModels = false,
                            modelFetchError = err.message ?: "Failed to fetch models",
                            snackbarMessage = "Model list error: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    fun setAiTestDaylight(daylight: DaylightContext) {
        _uiState.update { it.copy(aiTestDaylight = daylight) }
    }

    fun setAiTestMood(mood: String) {
        _uiState.update { it.copy(aiTestMood = mood) }
    }

    fun testGenerateAiPalette() {
        val currentState = _uiState.value
        val provider = currentState.selectedAiProvider
        val apiKey = getApiKey(provider)
        val model = currentState.selectedModels[provider] ?: provider.defaultModel
        val patternName = currentState.params.patternType.displayName
        val subTypeName = currentState.params.patternType.subTypes.getOrNull(currentState.params.subTypeIndex) ?: "Standard"
        val mood = currentState.aiTestMood
        val daylight = currentState.aiTestDaylight

        viewModelScope.launch {
            _uiState.update { it.copy(aiTestState = AiGenerationState.Loading) }
            val result = aiService.generatePalette(
                provider = provider,
                apiKey = apiKey,
                model = model,
                patternName = patternName,
                subTypeName = subTypeName,
                moodTag = mood,
                daylightContext = daylight,
                customPrompt = ""
            )

            result.fold(
                onSuccess = { generated ->
                    _uiState.update {
                        it.copy(
                            aiTestState = AiGenerationState.Success(generated),
                            snackbarMessage = "AI Palette '${generated.paletteName}' generated!",
                            isSuccessMessage = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            aiTestState = AiGenerationState.Error(error.message ?: "Generation failed"),
                            snackbarMessage = "AI error: ${error.message}",
                            isSuccessMessage = false
                        )
                    }
                }
            )
        }
    }

    fun applyGeneratedPalette(generated: GeneratedAiPalette) {
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

    fun showStyleSheet(show: Boolean) {
        _uiState.update { it.copy(showStyleSheet = show, showPaletteSheet = false, showAddShapeSheet = false) }
    }

    fun showPaletteSheet(show: Boolean) {
        _uiState.update { it.copy(showPaletteSheet = show, showStyleSheet = false, showAddShapeSheet = false) }
    }

    fun showAddShapeSheet(show: Boolean) {
        _uiState.update { it.copy(showAddShapeSheet = show, showStyleSheet = false, showPaletteSheet = false) }
    }

    fun selectShape(id: String?) {
        _uiState.update { it.copy(selectedShapeId = id) }
    }

    fun addCustomShape(type: CustomShapeType) {
        val currentShapes = _uiState.value.params.customShapes
        val colorsCount = _uiState.value.params.palette.colors.size.coerceAtLeast(1)
        val nextZ = (currentShapes.maxOfOrNull { it.zIndex } ?: -1) + 1
        val newShape = CustomCanvasShape(
            id = UUID.randomUUID().toString(),
            type = type,
            normalizedX = 0.5f,
            normalizedY = 0.5f,
            normalizedWidth = if (type.isProportional1to1) 0.38f else 0.36f,
            normalizedHeight = if (type.isProportional1to1) 0.38f else 0.48f,
            rotationDeg = 0f,
            colorIndex = (currentShapes.size + 1) % colorsCount,
            zIndex = nextZ
        )
        val updatedList = currentShapes + newShape
        updateParams { it.copy(customShapes = updatedList) }
        _uiState.update { it.copy(selectedShapeId = newShape.id) }
    }

    fun updateShapePosition(id: String, normX: Float, normY: Float) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.map {
            if (it.id == id) it.copy(normalizedX = normX, normalizedY = normY) else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun updateShapeScale(id: String, delta: Float) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.map {
            if (it.id == id) {
                val newW = (it.normalizedWidth * delta).coerceIn(0.12f, 1.2f)
                val newH = (it.normalizedHeight * delta).coerceIn(0.12f, 1.2f)
                it.copy(normalizedWidth = newW, normalizedHeight = newH)
            } else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun updateShapeRotation(id: String, deltaDeg: Float) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.map {
            if (it.id == id) {
                val newRot = (it.rotationDeg + deltaDeg) % 360f
                it.copy(rotationDeg = if (newRot < 0) newRot + 360f else newRot)
            } else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun setShapeRotation(id: String, deg: Float) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.map {
            if (it.id == id) it.copy(rotationDeg = deg % 360f) else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun setShapeColorIndex(id: String, colorIndex: Int) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.map {
            if (it.id == id) it.copy(colorIndex = colorIndex, customColorHex = null) else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun bringShapeToFront(id: String) {
        val currentShapes = _uiState.value.params.customShapes
        val maxZ = (currentShapes.maxOfOrNull { it.zIndex } ?: 0) + 1
        val updated = currentShapes.map {
            if (it.id == id) it.copy(zIndex = maxZ) else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun sendShapeToBack(id: String) {
        val currentShapes = _uiState.value.params.customShapes
        val minZ = (currentShapes.minOfOrNull { it.zIndex } ?: 0) - 1
        val updated = currentShapes.map {
            if (it.id == id) it.copy(zIndex = minZ) else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun deleteShape(id: String) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.filterNot { it.id == id }
        updateParams { it.copy(customShapes = updated) }
        if (_uiState.value.selectedShapeId == id) {
            _uiState.update { it.copy(selectedShapeId = null) }
        }
    }

    fun duplicateShape(id: String) {
        val currentShapes = _uiState.value.params.customShapes
        val target = currentShapes.find { it.id == id } ?: return
        val maxZ = (currentShapes.maxOfOrNull { it.zIndex } ?: 0) + 1
        val copy = target.copy(
            id = UUID.randomUUID().toString(),
            normalizedX = (target.normalizedX + 0.08f).coerceIn(0.1f, 0.9f),
            normalizedY = (target.normalizedY + 0.08f).coerceIn(0.1f, 0.9f),
            zIndex = maxZ
        )
        val updated = currentShapes + copy
        updateParams { it.copy(customShapes = updated) }
        _uiState.update { it.copy(selectedShapeId = copy.id) }
    }

    fun toggleShapeWireframe(id: String) {
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.map {
            if (it.id == id) it.copy(isWireframe = !it.isWireframe) else it
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun shuffleCustomShapes() {
        val rng = MathUtils.FastRandom(System.nanoTime())
        val goldenAnchors = listOf(
            Pair(0.382f, 0.618f),
            Pair(0.618f, 0.382f),
            Pair(0.618f, 0.764f),
            Pair(0.382f, 0.236f),
            Pair(0.500f, 0.500f)
        )
        val currentShapes = _uiState.value.params.customShapes
        val updated = currentShapes.mapIndexed { i, shape ->
            val anchor = goldenAnchors[i % goldenAnchors.size]
            val jitterX = (rng.nextFloat() - 0.5f) * 0.16f
            val jitterY = (rng.nextFloat() - 0.5f) * 0.16f
            val rot = when (i % 4) {
                0 -> 0f
                1 -> 45f
                2 -> 90f
                else -> 135f
            }
            shape.copy(
                normalizedX = (anchor.first + jitterX).coerceIn(0.15f, 0.85f),
                normalizedY = (anchor.second + jitterY).coerceIn(0.15f, 0.85f),
                rotationDeg = rot,
                colorIndex = (i + 1) % _uiState.value.params.palette.colors.size.coerceAtLeast(1)
            )
        }
        updateParams { it.copy(customShapes = updated) }
    }

    fun resetToDefaults() {
        _uiState.update {
            it.copy(
                params = WallpaperParams(),
                settings = AppSettingsState(),
                selectedShapeId = null
            )
        }
        generatePreview(debounceMs = 0)
    }

    fun updateParams(transform: (WallpaperParams) -> WallpaperParams) {
        _uiState.update { current ->
            current.copy(params = transform(current.params))
        }
        generatePreview(debounceMs = 50)
    }

    fun onGenerateOrShuffleClicked() {
        if (_uiState.value.params.patternType == WallpaperPatternType.STUDIO) {
            shuffleCustomShapes()
        } else {
            randomizeSeed()
        }
    }

    fun randomizeSeed() {
        val newSeed = (MathUtils.FastRandom(System.nanoTime()).nextFloat() * 1000000).toLong()
        updateParams { it.copy(seed = newSeed) }
    }

    fun setPatternType(type: WallpaperPatternType) {
        val stylePalette = PaletteEngine.getDefaultPaletteForPattern(type)
        _uiState.update { it.copy(selectedShapeId = null) }
        updateParams {
            it.copy(
                patternType = type,
                subTypeIndex = 0,
                palette = stylePalette
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

    fun applyDynamicMonet(primary: Color, secondary: Color, tertiary: Color, surface: Color, background: Color) {
        val monetPalette = PaletteEngine.createFromDynamicScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            surface = surface,
            background = background
        )
        setPalette(monetPalette)
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
                            snackbarMessage = "Saved to Gallery (${res.width}x${res.height})!",
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
                            snackbarMessage = "Wallpaper set for ${target.label}!",
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
            val aspect = currentParams.aspectRatio
            val previewWidth = 540
            val previewHeight = (previewWidth / aspect.ratio).toInt().coerceIn(360, 1200)

            val bitmap = withContext(Dispatchers.Default) {
                ProceduralRenderer.renderToBitmap(previewWidth, previewHeight, currentParams)
            }

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
