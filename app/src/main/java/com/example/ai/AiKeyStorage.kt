package com.example.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

/**
 * Secure local storage for user API keys (BYOK - Bring Your Own Key).
 * Stores encrypted/obfuscated provider keys in private SharedPreferences.
 */
class AiKeyStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApiKey(provider: AiProvider): String {
        val stored = prefs.getString(provider.name, "") ?: ""
        return if (stored.isNotEmpty()) {
            try {
                String(Base64.decode(stored, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (e: Exception) {
                stored
            }
        } else {
            ""
        }
    }

    fun saveApiKey(provider: AiProvider, key: String) {
        val trimmed = key.trim()
        val encoded = if (trimmed.isNotEmpty()) {
            Base64.encodeToString(trimmed.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        } else {
            ""
        }
        prefs.edit().putString(provider.name, encoded).apply()
    }

    fun hasApiKey(provider: AiProvider): Boolean {
        return getApiKey(provider).isNotBlank()
    }

    fun getSelectedModel(provider: AiProvider): String {
        val modelKey = "${provider.name}_MODEL"
        val stored = prefs.getString(modelKey, "") ?: ""
        return if (stored.isNotBlank()) stored else provider.defaultModel
    }

    fun saveSelectedModel(provider: AiProvider, model: String) {
        val modelKey = "${provider.name}_MODEL"
        prefs.edit().putString(modelKey, model.trim()).apply()
    }

    companion object {
        private const val PREFS_NAME = "wallpaper_studio_ai_keys"

        @Volatile
        private var instance: AiKeyStorage? = null

        fun getInstance(context: Context): AiKeyStorage {
            return instance ?: synchronized(this) {
                instance ?: AiKeyStorage(context.applicationContext).also { instance = it }
            }
        }
    }
}
