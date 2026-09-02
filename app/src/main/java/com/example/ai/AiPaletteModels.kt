package com.example.ai

import androidx.compose.ui.graphics.Color
import com.example.palette.ColorPalette

enum class AiProvider(
    val displayName: String,
    val endpointDescription: String,
    val defaultModel: String,
    val keyPlaceholder: String
) {
    GEMINI(
        displayName = "Google Gemini",
        endpointDescription = "generativelanguage.googleapis.com",
        defaultModel = "gemini-1.5-flash",
        keyPlaceholder = "AIzaSy..."
    ),
    OPENAI(
        displayName = "OpenAI / ChatGPT",
        endpointDescription = "api.openai.com",
        defaultModel = "gpt-4o-mini",
        keyPlaceholder = "sk-..."
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        endpointDescription = "openrouter.ai",
        defaultModel = "google/gemini-2.5-flash",
        keyPlaceholder = "sk-or-..."
    ),
    NVIDIA_NIM(
        displayName = "NVIDIA NIM",
        endpointDescription = "integrate.api.nvidia.com",
        defaultModel = "meta/llama-3.1-70b-instruct",
        keyPlaceholder = "nvapi-..."
    )
}

enum class DaylightContext(val label: String, val iconDescription: String) {
    DAWN_SUNRISE("Dawn / Sunrise", "Soft amber and muted rose light"),
    MIDDAY_SUN("Midday Sunlight", "High-contrast clean daylight"),
    GOLDEN_HOUR("Golden Hour", "Rich warm ochre and terracotta"),
    TWILIGHT("Nordic Twilight", "Muted indigo and lavender dusk"),
    MIDNIGHT_OLED("Midnight OLED", "Deep contrast true black and bioluminescent accents")
}

data class GeneratedAiPalette(
    val paletteName: String,
    val hexCodes: List<String>,
    val colors: List<Color>
) {
    fun toColorPalette(): ColorPalette {
        return ColorPalette(
            id = "ai_${paletteName.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}",
            name = "AI: $paletteName",
            colors = colors
        )
    }
}

sealed class AiGenerationState {
    object Idle : AiGenerationState()
    object Loading : AiGenerationState()
    data class Success(val palette: GeneratedAiPalette) : AiGenerationState()
    data class Error(val message: String) : AiGenerationState()
}
