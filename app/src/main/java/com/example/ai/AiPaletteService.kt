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

    companion object {
        val default = AiPaletteService()

        fun generateLocalFallbackCandidates(
            prompt: String,
            mood: String,
            daylight: DaylightContext
        ): List<AiPaletteCandidate> = default.generateLocalFallbackCandidates(prompt, mood, daylight)
    }

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
            // Graceful degradation: instant fallback to curated procedural presets if the network or API fails
            Result.success(
                generateLocalFallbackPalette(patternName, moodTag, daylightContext)
            )
        }
    }

    /**
     * Generates 2 to 3 distinct candidate colorways matching user prompt and lighting context.
     */
    suspend fun generatePaletteCandidates(
        provider: AiProvider,
        apiKey: String,
        model: String,
        patternName: String,
        subTypeName: String,
        moodTag: String,
        daylightContext: DaylightContext,
        customPrompt: String = ""
    ): Result<List<AiPaletteCandidate>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        val chosenModel = model.trim().ifEmpty { provider.defaultModel }

        if (trimmedKey.isEmpty()) {
            return@withContext Result.success(
                generateLocalFallbackCandidates(customPrompt, moodTag, daylightContext)
            )
        }

        try {
            val systemPrompt = """
                You are an expert Material Design 3 color palette architect and dynamic Monet harmony designer.
                Your task is to generate 3 distinct, beautifully balanced 5-step tonal colorway candidates for a wallpaper design based on the user's prompt.
                Each candidate must contain a name, a short aesthetic description, and exactly 5 hex color codes with monotonically balanced HCT luminance from surface/background to bright accent.

                You MUST return ONLY a JSON object with this exact schema:
                {
                  "candidates": [
                    {
                      "name": "Creative Name 1",
                      "description": "Short aesthetic tone description",
                      "hexCodes": ["#111827", "#1F2937", "#4B5563", "#9CA3AF", "#F3F4F6"]
                    },
                    {
                      "name": "Creative Name 2",
                      "description": "Short aesthetic tone description",
                      "hexCodes": ["#0B192C", "#1E3E62", "#415A77", "#84A98C", "#E0E1DD"]
                    },
                    {
                      "name": "Creative Name 3",
                      "description": "Short aesthetic tone description",
                      "hexCodes": ["#221E22", "#44344F", "#564D65", "#988B8E", "#F5EBE0"]
                    }
                  ]
                }
                Do not include markdown backticks or any conversational text outside the JSON object.
            """.trimIndent()

            val userPrompt = buildString {
                appendLine("Design 3 alternate Material 3 colorway candidates for:")
                appendLine("Pattern: $patternName ($subTypeName)")
                appendLine("Aesthetic / Theme: $moodTag")
                appendLine("Daylight Context: ${daylightContext.label} (${daylightContext.iconDescription})")
                if (customPrompt.isNotBlank()) {
                    appendLine("User Prompt: $customPrompt")
                }
                appendLine("Ensure each candidate has 5 valid 6-character hex codes starting with #.")
            }

            val rawJsonText = when (provider) {
                AiProvider.GEMINI -> callGemini(trimmedKey, chosenModel, systemPrompt, userPrompt)
                AiProvider.OPENAI -> callOpenAi(trimmedKey, chosenModel, systemPrompt, userPrompt)
                AiProvider.OPENROUTER -> callOpenRouter(trimmedKey, chosenModel, systemPrompt, userPrompt)
                AiProvider.NVIDIA_NIM -> callNvidiaNim(trimmedKey, chosenModel, systemPrompt, userPrompt)
            }

            val candidates = parseCandidatesJson(rawJsonText, customPrompt, moodTag, daylightContext)
            Result.success(candidates)
        } catch (e: Exception) {
            val fallback = generateLocalFallbackCandidates(customPrompt, moodTag, daylightContext)
            Result.success(fallback)
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

    private fun parseCandidatesJson(
        rawText: String,
        customPrompt: String,
        moodTag: String,
        daylight: DaylightContext
    ): List<AiPaletteCandidate> {
        var cleaned = rawText.trim()
        if (cleaned.startsWith("```json")) cleaned = cleaned.removePrefix("```json")
        if (cleaned.startsWith("```")) cleaned = cleaned.removePrefix("```")
        if (cleaned.endsWith("```")) cleaned = cleaned.removeSuffix("```")
        cleaned = cleaned.trim()

        try {
            val json = JSONObject(cleaned)
            val candidatesArray = json.optJSONArray("candidates")
            if (candidatesArray != null && candidatesArray.length() > 0) {
                val list = mutableListOf<AiPaletteCandidate>()
                for (i in 0 until minOf(3, candidatesArray.length())) {
                    val item = candidatesArray.getJSONObject(i)
                    val name = item.optString("name", "Colorway ${i + 1}")
                    val desc = item.optString("description", "")
                    val hexArr = item.optJSONArray("hexCodes") ?: item.optJSONArray("tones") ?: JSONArray()
                    val hexList = mutableListOf<String>()
                    val colorList = mutableListOf<Color>()
                    for (k in 0 until hexArr.length()) {
                        val h = hexArr.optString(k, "")
                        val col = parseHexColorOrNull(h)
                        if (col != null) {
                            hexList.add(h.uppercase())
                            colorList.add(col)
                        }
                    }
                    if (colorList.size >= 5) {
                        list.add(
                            AiPaletteCandidate(
                                name = name,
                                description = desc,
                                hexCodes = hexList.take(5),
                                colors = colorList.take(5)
                            )
                        )
                    }
                }
                if (list.isNotEmpty()) return list
            }
        } catch (_: Exception) { }

        return generateLocalFallbackCandidates(customPrompt, moodTag, daylight)
    }

    fun generateLocalFallbackCandidates(
        prompt: String,
        mood: String,
        daylight: DaylightContext
    ): List<AiPaletteCandidate> {
        val p = prompt.lowercase()
        return when {
            p.contains("nordic") || p.contains("pine") || p.contains("mist") -> listOf(
                AiPaletteCandidate(
                    name = "Nordic Pine & Fog",
                    description = "Cool deep firs rising through pale morning mist",
                    hexCodes = listOf("#1A2621", "#2D3F37", "#5B7368", "#A4B8AF", "#F0F4F2"),
                    colors = listOf(Color(0xFF1A2621), Color(0xFF2D3F37), Color(0xFF5B7368), Color(0xFFA4B8AF), Color(0xFFF0F4F2))
                ),
                AiPaletteCandidate(
                    name = "Arctic Fjord Twilight",
                    description = "Glacial indigo shadows meeting crisp crystalline snow",
                    hexCodes = listOf("#131B24", "#243342", "#4D637B", "#99B2CC", "#EDF3F8"),
                    colors = listOf(Color(0xFF131B24), Color(0xFF243342), Color(0xFF4D637B), Color(0xFF99B2CC), Color(0xFFEDF3F8))
                ),
                AiPaletteCandidate(
                    name = "Muted Birch Bark",
                    description = "Organic warm grey with Scandinavian ochre accents",
                    hexCodes = listOf("#262220", "#453E3B", "#7D726D", "#C4B9B3", "#F5F2F0"),
                    colors = listOf(Color(0xFF262220), Color(0xFF453E3B), Color(0xFF7D726D), Color(0xFFC4B9B3), Color(0xFFF5F2F0))
                )
            )
            p.contains("cyber") || p.contains("tokyo") || p.contains("neon") || p.contains("rain") -> listOf(
                AiPaletteCandidate(
                    name = "Shinjuku Rain",
                    description = "Wet asphalt reflecting electric violet and cyan neon",
                    hexCodes = listOf("#0C0E14", "#1E1E2E", "#4E3D67", "#8F65AF", "#D6B8F6"),
                    colors = listOf(Color(0xFF0C0E14), Color(0xFF1E1E2E), Color(0xFF4E3D67), Color(0xFF8F65AF), Color(0xFFD6B8F6))
                ),
                AiPaletteCandidate(
                    name = "Neon Cyberpunk",
                    description = "Deep slate indigo with radiant bioluminescent amber",
                    hexCodes = listOf("#0A1118", "#162836", "#2D556E", "#5EB1BF", "#F4F9F9"),
                    colors = listOf(Color(0xFF0A1118), Color(0xFF162836), Color(0xFF2D556E), Color(0xFF5EB1BF), Color(0xFFF4F9F9))
                ),
                AiPaletteCandidate(
                    name = "Midnight Alley",
                    description = "Moody dark OLED contrast with high-visibility coral glow",
                    hexCodes = listOf("#090A0F", "#1A1528", "#4A2B4B", "#A3485E", "#F28482"),
                    colors = listOf(Color(0xFF090A0F), Color(0xFF1A1528), Color(0xFF4A2B4B), Color(0xFFA3485E), Color(0xFFF28482))
                )
            )
            else -> listOf(
                AiPaletteCandidate(
                    name = "Earthy Amber Hearth",
                    description = "Warm organic clay with roasted terracotta and cream",
                    hexCodes = listOf("#241A18", "#452C27", "#7D4E44", "#BF8377", "#F7ECE9"),
                    colors = listOf(Color(0xFF241A18), Color(0xFF452C27), Color(0xFF7D4E44), Color(0xFFBF8377), Color(0xFFF7ECE9))
                ),
                AiPaletteCandidate(
                    name = "Nordic Twilight",
                    description = "Calm dusk tones with balanced lavender and muted slate",
                    hexCodes = listOf("#1A1C24", "#2E3342", "#4F5770", "#8D97B8", "#DDE2F2"),
                    colors = listOf(Color(0xFF1A1C24), Color(0xFF2E3342), Color(0xFF4F5770), Color(0xFF8D97B8), Color(0xFFDDE2F2))
                ),
                AiPaletteCandidate(
                    name = "Botanical Moss",
                    description = "Deep forest shadow flowing into sage and warm paper",
                    hexCodes = listOf("#18241B", "#2E3F32", "#526B57", "#8EAA94", "#E8F0E9"),
                    colors = listOf(Color(0xFF18241B), Color(0xFF2E3F32), Color(0xFF526B57), Color(0xFF8EAA94), Color(0xFFE8F0E9))
                )
            )
        }
    }
}
