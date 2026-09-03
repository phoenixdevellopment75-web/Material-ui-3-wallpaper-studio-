package com.example.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.palette.ColorPalette
import com.example.palette.GradientType
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wallpaper_studio_preferences"
)

/**
 * DataStore Repository managing persistent storage for user theme, motion scale,
 * haptic intensity, resolution presets, user-created color palettes, and last selected style.
 */
class SettingsDataStoreRepository(private val context: Context) {

    private object PreferencesKeys {
        val DYNAMIC_MONET_ENABLED = booleanPreferencesKey("dynamic_monet_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val STATIC_THEME_PRESET = stringPreferencesKey("static_theme_preset")
        val MOTION_SCALE = stringPreferencesKey("motion_scale")
        val HAPTIC_STRENGTH = stringPreferencesKey("haptic_strength")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val RESOLUTION_PRESET = stringPreferencesKey("resolution_preset")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val ANTI_ALIASING = booleanPreferencesKey("anti_aliasing")
        val SUB_SAMPLING = booleanPreferencesKey("sub_sampling")
        val DISABLE_BLUR_EFFECTS = booleanPreferencesKey("disable_blur_effects")
        val LAST_SELECTED_STYLE = stringPreferencesKey("last_selected_style")
        val USER_CUSTOM_PALETTES = stringPreferencesKey("user_custom_palettes_json")
    }

    val settingsFlow: Flow<AppSettingsState> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val dynamicMonet = preferences[PreferencesKeys.DYNAMIC_MONET_ENABLED] ?: true
            val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM)

            val staticPresetStr = preferences[PreferencesKeys.STATIC_THEME_PRESET] ?: StaticThemePreset.WARM_SAND.name
            val staticPreset = runCatching { StaticThemePreset.valueOf(staticPresetStr) }.getOrDefault(StaticThemePreset.WARM_SAND)

            val motionScaleStr = preferences[PreferencesKeys.MOTION_SCALE] ?: MotionScale.EXPRESSIVE.name
            val motionScale = runCatching { MotionScale.valueOf(motionScaleStr) }.getOrDefault(MotionScale.EXPRESSIVE)

            val hapticStr = preferences[PreferencesKeys.HAPTIC_STRENGTH] ?: HapticStrength.FIRM.name
            val hapticStrength = runCatching { HapticStrength.valueOf(hapticStr) }.getOrDefault(HapticStrength.FIRM)

            val hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true

            val resStr = preferences[PreferencesKeys.RESOLUTION_PRESET] ?: RenderResolutionPreset.QHD_PLUS.name
            val resolutionPreset = runCatching { RenderResolutionPreset.valueOf(resStr) }.getOrDefault(RenderResolutionPreset.QHD_PLUS)

            val exportStr = preferences[PreferencesKeys.EXPORT_FORMAT] ?: ExportImageFormat.PNG.name
            val exportFormat = runCatching { ExportImageFormat.valueOf(exportStr) }.getOrDefault(ExportImageFormat.PNG)

            val antiAliasing = preferences[PreferencesKeys.ANTI_ALIASING] ?: true
            val subSampling = preferences[PreferencesKeys.SUB_SAMPLING] ?: false
            val disableBlur = preferences[PreferencesKeys.DISABLE_BLUR_EFFECTS] ?: false

            AppSettingsState(
                dynamicMonetEnabled = dynamicMonet,
                themeMode = themeMode,
                staticThemePreset = staticPreset,
                motionScale = motionScale,
                resolutionPreset = resolutionPreset,
                exportFormat = exportFormat,
                antiAliasingEnabled = antiAliasing,
                subSamplingEnabled = subSampling,
                hapticStrength = hapticStrength,
                hapticsEnabled = hapticsEnabled,
                disableBlurEffects = disableBlur
            )
        }

    val customPalettesFlow: Flow<List<ColorPalette>> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[PreferencesKeys.USER_CUSTOM_PALETTES] ?: ""
            deserializePalettes(json)
        }

    val lastSelectedStyleFlow: Flow<String?> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SELECTED_STYLE]
        }

    suspend fun saveSettings(settings: AppSettingsState) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_MONET_ENABLED] = settings.dynamicMonetEnabled
            preferences[PreferencesKeys.THEME_MODE] = settings.themeMode.name
            preferences[PreferencesKeys.STATIC_THEME_PRESET] = settings.staticThemePreset.name
            preferences[PreferencesKeys.MOTION_SCALE] = settings.motionScale.name
            preferences[PreferencesKeys.HAPTIC_STRENGTH] = settings.hapticStrength.name
            preferences[PreferencesKeys.HAPTICS_ENABLED] = settings.hapticsEnabled
            preferences[PreferencesKeys.RESOLUTION_PRESET] = settings.resolutionPreset.name
            preferences[PreferencesKeys.EXPORT_FORMAT] = settings.exportFormat.name
            preferences[PreferencesKeys.ANTI_ALIASING] = settings.antiAliasingEnabled
            preferences[PreferencesKeys.SUB_SAMPLING] = settings.subSamplingEnabled
            preferences[PreferencesKeys.DISABLE_BLUR_EFFECTS] = settings.disableBlurEffects
        }
    }

    suspend fun saveLastSelectedStyle(styleName: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SELECTED_STYLE] = styleName
        }
    }

    suspend fun saveCustomPalettes(palettes: List<ColorPalette>) {
        val json = serializePalettes(palettes)
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_CUSTOM_PALETTES] = json
        }
    }

    suspend fun addCustomPalette(newPalette: ColorPalette, currentList: List<ColorPalette>): List<ColorPalette> {
        val filtered = currentList.filterNot { it.id == newPalette.id }
        val updated = listOf(newPalette) + filtered
        saveCustomPalettes(updated)
        return updated
    }

    suspend fun deleteCustomPalette(paletteId: String, currentList: List<ColorPalette>): List<ColorPalette> {
        val updated = currentList.filterNot { it.id == paletteId }
        saveCustomPalettes(updated)
        return updated
    }

    companion object {
        fun serializePalettes(palettes: List<ColorPalette>): String {
            val array = JSONArray()
            for (palette in palettes) {
                val obj = JSONObject()
                obj.put("id", palette.id)
                obj.put("name", palette.name)
                obj.put("gradientType", palette.gradientType.name)
                obj.put("isDarkBackground", palette.isDarkBackground)
                val colorsArray = JSONArray()
                for (color in palette.colors) {
                    colorsArray.put(color.toArgb())
                }
                obj.put("colors", colorsArray)
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializePalettes(json: String): List<ColorPalette> {
            if (json.isBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                val result = mutableListOf<ColorPalette>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                    val name = obj.optString("name", "Custom Palette")
                    val gradTypeStr = obj.optString("gradientType", GradientType.LINEAR.name)
                    val gradType = runCatching { GradientType.valueOf(gradTypeStr) }.getOrDefault(GradientType.LINEAR)
                    val isDark = obj.optBoolean("isDarkBackground", true)

                    val colorsArray = obj.optJSONArray("colors") ?: JSONArray()
                    val colors = mutableListOf<Color>()
                    for (c in 0 until colorsArray.length()) {
                        colors.add(Color(colorsArray.getInt(c)))
                    }
                    if (colors.size >= 2) {
                        result.add(ColorPalette(id, name, colors, gradType, isDark))
                    }
                }
                result
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
