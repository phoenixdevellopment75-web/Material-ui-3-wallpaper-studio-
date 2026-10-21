package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.AspectRatioPreset
import com.example.engine.CustomCanvasShape
import com.example.engine.CustomShapeType
import com.example.engine.M3ShapeToken
import com.example.engine.ProceduralM3Assembler
import com.example.engine.ProceduralRenderer
import com.example.engine.StudioImageLayer
import com.example.engine.StudioTextAlign
import com.example.engine.StudioTextLayer
import com.example.engine.StudioTypographyStyle
import com.example.engine.WallpaperParams
import com.example.engine.WallpaperPatternType
import com.example.palette.PaletteEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.studioDesignsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "studio_saved_designs"
)

/**
 * Saved Studio Design entity with thumbnail preview and serialization timestamp.
 */
data class SavedStudioDesign(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val shapeCount: Int = 0,
    val textCount: Int = 0,
    val imageCount: Int = 0,
    val thumbnailBase64: String? = null,
    val serializedParams: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))

    fun getThumbnailBitmap(): Bitmap? {
        if (thumbnailBase64.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(thumbnailBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Repository managing persistent storage for Studio canvas creations.
 * Serializes List<StudioItem> state, palette, and optical parameters into JSON,
 * rendering a 120x240 thumbnail for rapid visual library browsing.
 */
class StudioDesignRepository(private val context: Context) {

    private object PreferencesKeys {
        val SAVED_DESIGNS_JSON = stringPreferencesKey("saved_studio_designs_json")
    }

    val savedDesignsFlow: Flow<List<SavedStudioDesign>> = context.studioDesignsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[PreferencesKeys.SAVED_DESIGNS_JSON] ?: ""
            deserializeDesigns(json)
        }

    suspend fun saveDesign(
        name: String,
        params: WallpaperParams
    ): SavedStudioDesign = withContext(Dispatchers.IO) {
        val thumbnail = renderThumbnailBitmap(params, 120, 240)
        val thumbnailBase64 = encodeBitmapToBase64(thumbnail)
        val serializedJson = serializeParamsToJson(params)

        val design = SavedStudioDesign(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Design " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()) },
            timestamp = System.currentTimeMillis(),
            shapeCount = params.customShapes.size,
            textCount = params.customTexts.size,
            imageCount = params.customImages.size,
            thumbnailBase64 = thumbnailBase64,
            serializedParams = serializedJson
        )

        context.studioDesignsDataStore.edit { prefs ->
            val existing = deserializeDesigns(prefs[PreferencesKeys.SAVED_DESIGNS_JSON] ?: "")
            val updated = listOf(design) + existing
            prefs[PreferencesKeys.SAVED_DESIGNS_JSON] = serializeDesigns(updated)
        }

        design
    }

    suspend fun duplicateDesign(designId: String) = withContext(Dispatchers.IO) {
        context.studioDesignsDataStore.edit { prefs ->
            val existing = deserializeDesigns(prefs[PreferencesKeys.SAVED_DESIGNS_JSON] ?: "")
            val target = existing.find { it.id == designId } ?: return@edit
            val duplicate = target.copy(
                id = UUID.randomUUID().toString(),
                name = "${target.name} (Copy)",
                timestamp = System.currentTimeMillis()
            )
            val updated = listOf(duplicate) + existing
            prefs[PreferencesKeys.SAVED_DESIGNS_JSON] = serializeDesigns(updated)
        }
    }

    suspend fun deleteDesign(designId: String) = withContext(Dispatchers.IO) {
        context.studioDesignsDataStore.edit { prefs ->
            val existing = deserializeDesigns(prefs[PreferencesKeys.SAVED_DESIGNS_JSON] ?: "")
            val updated = existing.filterNot { it.id == designId }
            prefs[PreferencesKeys.SAVED_DESIGNS_JSON] = serializeDesigns(updated)
        }
    }

    fun loadParamsFromDesign(design: SavedStudioDesign): WallpaperParams? {
        return deserializeParamsFromJson(design.serializedParams)
    }

    companion object {
        fun renderThumbnailBitmap(params: WallpaperParams, width: Int = 120, height: Int = 240): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            ProceduralRenderer.renderIntoBitmap(bitmap, params)
            return bitmap
        }

        private fun encodeBitmapToBase64(bitmap: Bitmap): String {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        }

        private fun serializeDesigns(designs: List<SavedStudioDesign>): String {
            val array = JSONArray()
            for (d in designs) {
                val obj = JSONObject().apply {
                    put("id", d.id)
                    put("name", d.name)
                    put("timestamp", d.timestamp)
                    put("shapeCount", d.shapeCount)
                    put("textCount", d.textCount)
                    put("imageCount", d.imageCount)
                    put("thumbnailBase64", d.thumbnailBase64 ?: "")
                    put("serializedParams", d.serializedParams)
                }
                array.put(obj)
            }
            return array.toString()
        }

        private fun deserializeDesigns(json: String): List<SavedStudioDesign> {
            if (json.isBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                val list = mutableListOf<SavedStudioDesign>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SavedStudioDesign(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.optString("name", "Studio Creation"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            shapeCount = obj.optInt("shapeCount", 0),
                            textCount = obj.optInt("textCount", 0),
                            imageCount = obj.optInt("imageCount", 0),
                            thumbnailBase64 = obj.optString("thumbnailBase64").takeIf { it.isNotBlank() },
                            serializedParams = obj.optString("serializedParams", "{}")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeParamsToJson(params: WallpaperParams): String {
            val root = JSONObject()
            root.put("patternType", params.patternType.name)
            root.put("paletteName", params.palette.name)
            root.put("scale", params.scale.toDouble())
            root.put("rotationDegrees", params.rotationDegrees.toDouble())
            root.put("filmGrain", params.filmGrain.toDouble())

            // Background Config
            val bgObj = JSONObject().apply {
                put("mode", params.backgroundConfig.mode.name)
                params.backgroundConfig.solidColorHex?.let { put("solidColorHex", it) }
                put("angleDegrees", params.backgroundConfig.angleDegrees.toDouble())
                val hexesArr = JSONArray()
                params.backgroundConfig.gradientColorHexes.forEach { hexesArr.put(it) }
                put("gradientColorHexes", hexesArr)
            }
            root.put("backgroundConfig", bgObj)

            // Shapes
            val shapesArr = JSONArray()
            for (s in params.customShapes) {
                val sObj = JSONObject().apply {
                    put("id", s.id)
                    put("type", s.type.name)
                    put("x", s.normalizedX.toDouble())
                    put("y", s.normalizedY.toDouble())
                    put("w", s.normalizedWidth.toDouble())
                    put("h", s.normalizedHeight.toDouble())
                    put("rot", s.rotationDeg.toDouble())
                    put("colorIdx", s.colorIndex)
                    s.customColorHex?.let { put("customColorHex", it) }
                    put("opacity", s.opacity.toDouble())
                    put("shadow", s.shadowRadius.toDouble())
                    put("blur", s.blurRadius.toDouble())
                    put("isWireframe", s.isWireframe)
                    put("strokeWidth", s.strokeWidth.toDouble())
                    put("scallopLobes", s.scallopLobes)
                    put("lockProportions", s.lockProportions)
                    put("zIndex", s.zIndex)
                }
                shapesArr.put(sObj)
            }
            root.put("shapes", shapesArr)

            // Texts
            val textsArr = JSONArray()
            for (t in params.customTexts) {
                val tObj = JSONObject().apply {
                    put("id", t.id)
                    put("text", t.text)
                    put("x", t.normalizedX.toDouble())
                    put("y", t.normalizedY.toDouble())
                    put("size", t.normalizedSize.toDouble())
                    put("scaleX", t.scaleX.toDouble())
                    put("scaleY", t.scaleY.toDouble())
                    put("rot", t.rotationDeg.toDouble())
                    put("colorIdx", t.colorIndex)
                    t.customColorHex?.let { put("customColorHex", it) }
                    put("opacity", t.opacity.toDouble())
                    put("shadow", t.shadowRadius.toDouble())
                    put("typography", t.typographyStyle.name)
                    put("isOutline", t.isOutlineOnly)
                    put("strokeWidth", t.strokeWidthDp.toDouble())
                    put("glow", t.glowRadiusDp.toDouble())
                    put("letterSpacingSp", t.letterSpacingSp.toDouble())
                    put("lineHeightMultiplier", t.lineHeightMultiplier.toDouble())
                    t.customFontPath?.let { put("customFontPath", it) }
                    t.customFontName?.let { put("customFontName", it) }
                    put("zIndex", t.zIndex)
                }
                textsArr.put(tObj)
            }
            root.put("texts", textsArr)

            // Images
            val imagesArr = JSONArray()
            for (img in params.customImages) {
                val iObj = JSONObject().apply {
                    put("id", img.id)
                    put("imagePath", img.imagePath)
                    put("maskShape", img.maskShape.name)
                    put("x", img.normalizedX.toDouble())
                    put("y", img.normalizedY.toDouble())
                    put("w", img.normalizedWidth.toDouble())
                    put("h", img.normalizedHeight.toDouble())
                    put("rot", img.rotationDeg.toDouble())
                    put("opacity", img.opacity.toDouble())
                    put("shadow", img.shadowRadius.toDouble())
                    put("zIndex", img.zIndex)
                    put("lockProportions", img.lockProportions)
                    put("cropScale", img.cropScale.toDouble())
                    put("cropOffsetX", img.cropOffsetX.toDouble())
                    put("cropOffsetY", img.cropOffsetY.toDouble())
                    put("frameWidthDp", img.frameWidthDp.toDouble())
                    img.frameColorHex?.let { put("frameColorHex", it) }
                }
                imagesArr.put(iObj)
            }
            root.put("images", imagesArr)

            return root.toString()
        }

        fun deserializeParamsFromJson(json: String): WallpaperParams? {
            if (json.isBlank()) return null
            return try {
                val root = JSONObject(json)
                val patternTypeName = root.optString("patternType", WallpaperPatternType.STUDIO.name)
                val patternType = runCatching { WallpaperPatternType.valueOf(patternTypeName) }.getOrDefault(WallpaperPatternType.STUDIO)
                val palName = root.optString("paletteName", "")
                val matchedPalette = com.example.palette.PaletteEngine.allPresets.find { it.name.equals(palName, ignoreCase = true) }
                    ?: com.example.palette.PaletteEngine.PRESET_ALPINE_MIST

                val scale = root.optDouble("scale", 1.0).toFloat()
                val rot = root.optDouble("rotationDegrees", 0.0).toFloat()
                val filmGrain = root.optDouble("filmGrain", 0.0).toFloat()

                // Background Config
                val bgObj = root.optJSONObject("backgroundConfig")
                val bgConfig = if (bgObj != null) {
                    val modeName = bgObj.optString("mode", com.example.engine.BackgroundMode.LINEAR_GRADIENT.name)
                    val mode = runCatching { com.example.engine.BackgroundMode.valueOf(modeName) }.getOrDefault(com.example.engine.BackgroundMode.LINEAR_GRADIENT)
                    val solidHex = if (bgObj.has("solidColorHex")) bgObj.getLong("solidColorHex") else null
                    val angle = bgObj.optDouble("angleDegrees", 90.0).toFloat()
                    val hexesArr = bgObj.optJSONArray("gradientColorHexes")
                    val hexes = mutableListOf<Long>()
                    if (hexesArr != null) {
                        for (idx in 0 until hexesArr.length()) {
                            hexes.add(hexesArr.getLong(idx))
                        }
                    }
                    com.example.engine.CanvasBackgroundConfig(
                        mode = mode,
                        solidColorHex = solidHex,
                        gradientColorHexes = hexes,
                        angleDegrees = angle
                    )
                } else {
                    com.example.engine.CanvasBackgroundConfig()
                }

                val shapes = mutableListOf<CustomCanvasShape>()
                val shapesArr = root.optJSONArray("shapes") ?: JSONArray()
                for (i in 0 until shapesArr.length()) {
                    val sObj = shapesArr.getJSONObject(i)
                    val shapeTypeName = sObj.optString("type", CustomShapeType.CLOVER_4.name)
                    val shapeType = runCatching { CustomShapeType.valueOf(shapeTypeName) }.getOrDefault(CustomShapeType.CLOVER_4)
                    val customColHex = if (sObj.has("customColorHex")) sObj.getLong("customColorHex") else null
                    shapes.add(
                        CustomCanvasShape(
                            id = sObj.optString("id", UUID.randomUUID().toString()),
                            type = shapeType,
                            normalizedX = sObj.optDouble("x", 0.5).toFloat(),
                            normalizedY = sObj.optDouble("y", 0.5).toFloat(),
                            normalizedWidth = sObj.optDouble("w", 0.38).toFloat(),
                            normalizedHeight = sObj.optDouble("h", 0.38).toFloat(),
                            rotationDeg = sObj.optDouble("rot", 0.0).toFloat(),
                            colorIndex = sObj.optInt("colorIdx", 0),
                            customColorHex = customColHex,
                            opacity = sObj.optDouble("opacity", 1.0).toFloat(),
                            shadowRadius = sObj.optDouble("shadow", 16.0).toFloat(),
                            blurRadius = sObj.optDouble("blur", 0.0).toFloat(),
                            isWireframe = sObj.optBoolean("isWireframe", false),
                            strokeWidth = sObj.optDouble("strokeWidth", 2.5).toFloat(),
                            scallopLobes = sObj.optInt("scallopLobes", 8),
                            lockProportions = sObj.optBoolean("lockProportions", false),
                            zIndex = sObj.optInt("zIndex", i)
                        )
                    )
                }

                val texts = mutableListOf<StudioTextLayer>()
                val textsArr = root.optJSONArray("texts") ?: JSONArray()
                for (i in 0 until textsArr.length()) {
                    val tObj = textsArr.getJSONObject(i)
                    val typoName = tObj.optString("typography", StudioTypographyStyle.NAGASAKI_HYPEROS_TALL.name)
                    val typoStyle = runCatching { StudioTypographyStyle.valueOf(typoName) }.getOrDefault(StudioTypographyStyle.NAGASAKI_HYPEROS_TALL)
                    val customColHex = if (tObj.has("customColorHex")) tObj.getLong("customColorHex") else null
                    val fontPath = if (tObj.has("customFontPath") && !tObj.isNull("customFontPath")) tObj.getString("customFontPath") else null
                    val fontName = if (tObj.has("customFontName") && !tObj.isNull("customFontName")) tObj.getString("customFontName") else null

                    texts.add(
                        StudioTextLayer(
                            id = tObj.optString("id", UUID.randomUUID().toString()),
                            text = tObj.optString("text", "STUDIO"),
                            normalizedX = tObj.optDouble("x", 0.5).toFloat(),
                            normalizedY = tObj.optDouble("y", 0.5).toFloat(),
                            normalizedSize = tObj.optDouble("size", 0.16).toFloat(),
                            scaleX = tObj.optDouble("scaleX", 1.0).toFloat(),
                            scaleY = tObj.optDouble("scaleY", 1.0).toFloat(),
                            rotationDeg = tObj.optDouble("rot", 0.0).toFloat(),
                            colorIndex = tObj.optInt("colorIdx", 1),
                            customColorHex = customColHex,
                            opacity = tObj.optDouble("opacity", 1.0).toFloat(),
                            shadowRadius = tObj.optDouble("shadow", 16.0).toFloat(),
                            typographyStyle = typoStyle,
                            isOutlineOnly = tObj.optBoolean("isOutline", false),
                            strokeWidthDp = tObj.optDouble("strokeWidth", 3.0).toFloat(),
                            glowRadiusDp = tObj.optDouble("glow", 0.0).toFloat(),
                            letterSpacingSp = tObj.optDouble("letterSpacingSp", 0.0).toFloat(),
                            lineHeightMultiplier = tObj.optDouble("lineHeightMultiplier", 1.0).toFloat(),
                            customFontPath = fontPath,
                            customFontName = fontName,
                            zIndex = tObj.optInt("zIndex", i)
                        )
                    )
                }

                val images = mutableListOf<StudioImageLayer>()
                val imagesArr = root.optJSONArray("images") ?: JSONArray()
                for (i in 0 until imagesArr.length()) {
                    val iObj = imagesArr.getJSONObject(i)
                    val maskName = iObj.optString("maskShape", CustomShapeType.CLOVER_4.name)
                    val maskShape = runCatching { CustomShapeType.valueOf(maskName) }.getOrDefault(CustomShapeType.CLOVER_4)
                    val frameColorHex = if (iObj.has("frameColorHex")) iObj.getLong("frameColorHex") else null

                    images.add(
                        StudioImageLayer(
                            id = iObj.optString("id", UUID.randomUUID().toString()),
                            imagePath = iObj.optString("imagePath", ""),
                            maskShape = maskShape,
                            normalizedX = iObj.optDouble("x", 0.5).toFloat(),
                            normalizedY = iObj.optDouble("y", 0.5).toFloat(),
                            normalizedWidth = iObj.optDouble("w", 0.38).toFloat(),
                            normalizedHeight = iObj.optDouble("h", 0.38).toFloat(),
                            rotationDeg = iObj.optDouble("rot", 0.0).toFloat(),
                            opacity = iObj.optDouble("opacity", 1.0).toFloat(),
                            shadowRadius = iObj.optDouble("shadow", 16.0).toFloat(),
                            zIndex = iObj.optInt("zIndex", i),
                            lockProportions = iObj.optBoolean("lockProportions", true),
                            cropScale = iObj.optDouble("cropScale", 1.0).toFloat(),
                            cropOffsetX = iObj.optDouble("cropOffsetX", 0.0).toFloat(),
                            cropOffsetY = iObj.optDouble("cropOffsetY", 0.0).toFloat(),
                            frameWidthDp = iObj.optDouble("frameWidthDp", 0.0).toFloat(),
                            frameColorHex = frameColorHex
                        )
                    )
                }

                WallpaperParams(
                    patternType = patternType,
                    palette = matchedPalette,
                    scale = scale,
                    rotationDegrees = rot,
                    filmGrain = filmGrain,
                    backgroundConfig = bgConfig,
                    customShapes = shapes,
                    customTexts = texts,
                    customImages = images
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
