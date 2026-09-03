package com.example.ai

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiPaletteService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchAvailableModels(
        provider: AiProvider,
        apiKey: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.success(getDefaultModels(provider))
        }

        try {
            val models = when (provider) {
                AiProvider.GEMINI -> fetchGeminiModels(trimmedKey)
                AiProvider.OPENAI -> fetchOpenAiModels(trimmedKey)
                AiProvider.OPENROUTER -> fetchOpenRouterModels(trimmedKey)
                AiProvider.NVIDIA_NIM -> fetchNvidiaModels(trimmedKey)
            }
            if (models.isEmpty()) {
                Result.success(getDefaultModels(provider))
            } else {
                Result.success(models)
            }
        } catch (e: Exception) {
            // If network or key issue during model listing, return failure with message but preserve defaults available
            Result.failure(e)
        }
    }

    private fun fetchGeminiModels(apiKey: String): List<String> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()?.take(200) ?: "HTTP ${response.code}"
                throw Exception("Gemini API ($response.code): $errorBody")
            }
            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val modelsArray = root.optJSONArray("models") ?: return emptyList()
            val result = mutableListOf<String>()
            for (i in 0 until modelsArray.length()) {
                val modelObj = modelsArray.getJSONObject(i)
                val supportedMethods = modelObj.optJSONArray("supportedGenerationMethods") ?: JSONArray()
                var canGenerate = false
                for (j in 0 until supportedMethods.length()) {
                    if (supportedMethods.optString(j) == "generateContent") {
                        canGenerate = true
                        break
                    }
                }
                if (canGenerate) {
                    val rawName = modelObj.optString("name", "")
                    val cleanName = rawName.removePrefix("models/")
                    if (cleanName.isNotBlank()) {
                        result.add(cleanName)
                    }
                }
            }
            // Sort to prioritize flash and newer models first
            return result.sortedWith(compareByDescending<String> { it.contains("flash") }
                .thenByDescending { it.contains("2.5") || it.contains("2.0") }
                .thenBy { it })
        }
    }

    private fun fetchOpenAiModels(apiKey: String): List<String> {
        val url = "https://api.openai.com/v1/models"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()?.take(200) ?: "HTTP ${response.code}"
                throw Exception("OpenAI API ($response.code): $errorBody")
            }
            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val dataArray = root.optJSONArray("data") ?: return emptyList()
            val result = mutableListOf<String>()
            for (i in 0 until dataArray.length()) {
                val id = dataArray.getJSONObject(i).optString("id", "")
                if (id.startsWith("gpt-") || id.startsWith("o1") || id.startsWith("o3")) {
                    result.add(id)
                }
            }
            return result.sorted()
        }
    }

    private fun fetchOpenRouterModels(apiKey: String): List<String> {
        val url = "https://openrouter.ai/api/v1/models"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()?.take(200) ?: "HTTP ${response.code}"
                throw Exception("OpenRouter API ($response.code): $errorBody")
            }
            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val dataArray = root.optJSONArray("data") ?: return emptyList()
            val result = mutableListOf<String>()
            for (i in 0 until dataArray.length()) {
                val id = dataArray.getJSONObject(i).optString("id", "")
                if (id.isNotBlank()) {
                    result.add(id)
                }
            }
            return result.take(30)
        }
    }

    private fun fetchNvidiaModels(apiKey: String): List<String> {
        val url = "https://integrate.api.nvidia.com/v1/models"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()?.take(200) ?: "HTTP ${response.code}"
                throw Exception("NVIDIA NIM API ($response.code): $errorBody")
            }
            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val dataArray = root.optJSONArray("data") ?: return emptyList()
            val result = mutableListOf<String>()
            for (i in 0 until dataArray.length()) {
                val id = dataArray.getJSONObject(i).optString("id", "")
                if (id.isNotBlank()) {
                    result.add(id)
                }
            }
            return result.take(30)
        }
    }

    fun getDefaultModels(provider: AiProvider): List<String> {
        return when (provider) {
            AiProvider.GEMINI -> listOf(
                "gemini-2.0-flash",
                "gemini-2.5-flash",
                "gemini-1.5-flash",
                "gemini-1.5-pro",
                "gemini-1.5-flash-8b"
            )
            AiProvider.OPENAI -> listOf(
                "gpt-4o-mini",
                "gpt-4o",
                "gpt-4-turbo",
                "gpt-3.5-turbo"
            )
            AiProvider.OPENROUTER -> listOf(
                "google/gemini-2.5-flash",
                "openai/gpt-4o-mini",
                "anthropic/claude-3.5-haiku",
                "meta-llama/llama-3.3-70b-instruct"
            )
            AiProvider.NVIDIA_NIM -> listOf(
                "meta/llama-3.1-70b-instruct",
                "meta/llama-3.3-70b-instruct",
                "mistralai/mistral-large-2-instruct",
                "nvidia/llama-3.1-nemotron-70b-instruct"
            )
        }
    }

    suspend fun generatePalette(
        provider: AiProvider,
        apiKey: String,
        model: String = provider.defaultModel,
        patternName: String,
        subTypeName: String,
        moodTag: String,
        daylightContext: DaylightContext,
        customPrompt: String = ""
    ): Result<GeneratedAiPalette> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        val chosenModel = model.trim().ifEmpty { provider.defaultModel }

        if (trimmedKey.isEmpty()) {
            return@withContext Result.success(
                generateLocalFallbackPalette(patternName, moodTag, daylightContext)
            )
        }

        try {
            val systemPrompt = buildSystemPrompt()
            val userPrompt = buildUserPrompt(patternName, subTypeName, moodTag, daylightContext, customPrompt)

            val rawJsonText = when (provider) {
                AiProvider.GEMINI -> callGemini(trimmedKey, chosenModel, systemPrompt, userPrompt)
                AiProvider.OPENAI -> callOpenAi(trimmedKey, chosenModel, systemPrompt, userPrompt)
                AiProvider.OPENROUTER -> callOpenRouter(trimmedKey, chosenModel, systemPrompt, userPrompt)
                AiProvider.NVIDIA_NIM -> callNvidiaNim(trimmedKey, chosenModel, systemPrompt, userPrompt)
            }

            val parsedPalette = parsePaletteJson(rawJsonText)
            Result.success(parsedPalette)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSystemPrompt(): String {
        return """
            You are an expert Material Design 3 color palette architect and dynamic Monet harmony designer.
            Your task is to generate a pristine, harmonious 5-step tonal ramp for an organic wallpaper design.
            The tones MUST form a smooth, monotonic progression in lightness and color temperature matching the requested mood and daylight context.
            
            You MUST return ONLY a JSON object with this exact schema:
            {
              "paletteName": "Creative Short Name",
              "tones": ["#1A1C1E", "#2E3033", "#5C5F62", "#D1C4E9", "#E8DEF8"]
            }
            Do not include markdown backticks or any conversational text outside the JSON object.
        """.trimIndent()
    }

    private fun buildUserPrompt(
        patternName: String,
        subTypeName: String,
        moodTag: String,
        daylight: DaylightContext,
        customPrompt: String
    ): String {
        return buildString {
            appendLine("Design a 5-step Material 3 tonal palette for:")
            appendLine("Pattern: $patternName ($subTypeName)")
            appendLine("Mood / Aesthetic: $moodTag")
            appendLine("Daylight Lighting: ${daylight.label} (${daylight.iconDescription})")
            if (customPrompt.isNotBlank()) {
                appendLine("User Specific Request: $customPrompt")
            }
            appendLine("Ensure each tone is a valid 6-character hex code starting with #.")
        }
    }

    private fun callGemini(apiKey: String, model: String, systemPrompt: String, userPrompt: String): String {
        val cleanModel = model.removePrefix("models/").trim().ifEmpty { "gemini-2.0-flash" }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\n$userPrompt"))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            }
            put("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string()?.take(300) ?: "HTTP ${response.code}"
                throw Exception("Gemini API Error (${response.code}): $errBody")
            }
            val body = response.body?.string() ?: throw Exception("Empty response from Gemini")
            val respJson = JSONObject(body)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                throw Exception("No content candidates returned by Gemini")
            }
            val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    private fun callOpenAi(apiKey: String, model: String, systemPrompt: String, userPrompt: String): String {
        val url = "https://api.openai.com/v1/chat/completions"

        val rootJson = JSONObject().apply {
            put("model", model)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messages)
            put("response_format", JSONObject().put("type", "json_object"))
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        return executeChatCompletion(request, "OpenAI")
    }

    private fun callOpenRouter(apiKey: String, model: String, systemPrompt: String, userPrompt: String): String {
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val rootJson = JSONObject().apply {
            put("model", model)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://wallpaperstudio.aistudio.com")
            .addHeader("X-Title", "Wallpaper Studio")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        return executeChatCompletion(request, "OpenRouter")
    }

    private fun callNvidiaNim(apiKey: String, model: String, systemPrompt: String, userPrompt: String): String {
        val url = "https://integrate.api.nvidia.com/v1/chat/completions"

        val rootJson = JSONObject().apply {
            put("model", model)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        return executeChatCompletion(request, "NVIDIA NIM")
    }

    private fun executeChatCompletion(request: Request, providerName: String): String {
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string()?.take(300) ?: "HTTP ${response.code}"
                throw Exception("$providerName API Error (${response.code}): $errBody")
            }
            val body = response.body?.string() ?: throw Exception("Empty response from $providerName")
            val respJson = JSONObject(body)
            val choices = respJson.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                throw Exception("No response choices from $providerName")
            }
            return choices.getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    private fun parsePaletteJson(rawText: String): GeneratedAiPalette {
        var cleaned = rawText.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        cleaned = cleaned.trim()

        val json = JSONObject(cleaned)
        val paletteName = json.optString("paletteName", "AI Harmony Ramp")
        val tonesArray = json.optJSONArray("tones") ?: JSONArray()

        val hexList = mutableListOf<String>()
        val colorList = mutableListOf<Color>()

        for (i in 0 until tonesArray.length()) {
            val hex = tonesArray.optString(i, "")
            val parsedColor = parseHexColorOrNull(hex)
            if (parsedColor != null) {
                hexList.add(hex.uppercase())
                colorList.add(parsedColor)
            }
        }

        if (colorList.size < 5) {
            val fallbacks = generateLocalFallbackPalette("Organic", "Monet", DaylightContext.TWILIGHT)
            return GeneratedAiPalette(
                paletteName = paletteName,
                hexCodes = fallbacks.hexCodes,
                colors = fallbacks.colors
            )
        }

        return GeneratedAiPalette(
            paletteName = paletteName,
            hexCodes = hexList.take(5),
            colors = colorList.take(5)
        )
    }

    fun parseHexColorOrNull(hex: String): Color? {
        val clean = hex.trim().removePrefix("#")
        return try {
            when (clean.length) {
                6 -> {
                    val r = clean.substring(0, 2).toInt(16)
                    val g = clean.substring(2, 4).toInt(16)
                    val b = clean.substring(4, 6).toInt(16)
                    Color(r, g, b)
                }
                8 -> {
                    val a = clean.substring(0, 2).toInt(16)
                    val r = clean.substring(2, 4).toInt(16)
                    val g = clean.substring(4, 6).toInt(16)
                    val b = clean.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }
                3 -> {
                    val r = clean.substring(0, 1).repeat(2).toInt(16)
                    val g = clean.substring(1, 2).repeat(2).toInt(16)
                    val b = clean.substring(2, 3).repeat(2).toInt(16)
                    Color(r, g, b)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun generateLocalFallbackPalette(
        patternName: String,
        moodTag: String,
        daylight: DaylightContext
    ): GeneratedAiPalette {
        val (name, hexCodes) = when {
            moodTag.contains("Nordic", ignoreCase = true) || moodTag.contains("Clay", ignoreCase = true) -> {
                "Nordic Clay" to listOf("#2A2421", "#4A3B32", "#7A5E4E", "#B89682", "#E8DDD5")
            }
            moodTag.contains("OLED", ignoreCase = true) || moodTag.contains("Space", ignoreCase = true) -> {
                "OLED Deep Nebula" to listOf("#050508", "#12131A", "#262B40", "#536894", "#9BB3E8")
            }
            moodTag.contains("Sage", ignoreCase = true) || moodTag.contains("Botanical", ignoreCase = true) -> {
                "Botanical Sage" to listOf("#1C261E", "#314234", "#526B57", "#8BAA91", "#D6E4D9")
            }
            moodTag.contains("Terracotta", ignoreCase = true) || moodTag.contains("Dawn", ignoreCase = true) -> {
                "Terracotta Dawn" to listOf("#301B17", "#542D26", "#8A493D", "#C77B6B", "#F2D5CE")
            }
            moodTag.contains("Matcha", ignoreCase = true) -> {
                "Earthy Matcha" to listOf("#22261C", "#3B4230", "#636E52", "#9EAC88", "#E2E7DA")
            }
            moodTag.contains("Pastel", ignoreCase = true) || moodTag.contains("Cyber", ignoreCase = true) -> {
                "Cyberpunk Pastel" to listOf("#21192B", "#422E5C", "#76519E", "#B78AE8", "#E9D9FA")
            }
            else -> when (daylight) {
                DaylightContext.DAWN_SUNRISE -> "Dawn Amber" to listOf("#2E1E1C", "#52332F", "#8C5851", "#C78C83", "#F5DDD9")
                DaylightContext.MIDDAY_SUN -> "Clean Daylight" to listOf("#1F2426", "#3A4447", "#63747A", "#A0B5BD", "#E3EEF2")
                DaylightContext.GOLDEN_HOUR -> "Golden Solstice" to listOf("#2B2117", "#4D3823", "#825F39", "#C2945D", "#F5E4CE")
                DaylightContext.TWILIGHT -> "Nordic Twilight" to listOf("#1A1C24", "#2E3342", "#4F5770", "#8D97B8", "#DDE2F2")
                DaylightContext.MIDNIGHT_OLED -> "Midnight OLED" to listOf("#000000", "#12141A", "#242C3D", "#4B5F8A", "#8EA9E6")
            }
        }

        val colors = hexCodes.mapNotNull { parseHexColorOrNull(it) }
        return GeneratedAiPalette(
            paletteName = "$name (Harmonic)",
            hexCodes = hexCodes,
            colors = colors
        )
    }
}
