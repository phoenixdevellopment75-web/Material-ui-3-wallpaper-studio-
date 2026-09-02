package com.example.engine

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Master dispatcher for the 5 curated procedural Material 3 wallpaper generators.
 */
object ProceduralRenderer {

    /**
     * Renders wallpaper onto a newly allocated Bitmap offscreen in background coroutine.
     */
    suspend fun renderToBitmap(
        width: Int,
        height: Int,
        params: WallpaperParams
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        renderIntoBitmap(bitmap, params)
        bitmap
    }

    /**
     * Renders wallpaper directly into an existing Bitmap.
     */
    fun renderIntoBitmap(
        bitmap: Bitmap,
        params: WallpaperParams
    ) {
        when (params.patternType) {
            WallpaperPatternType.NESTED_ARCHES -> NestedArchesRenderer.render(bitmap, params)
            WallpaperPatternType.TOPOGRAPHIC_CONTOURS -> TopographicRenderer.render(bitmap, params)
            WallpaperPatternType.DESERT_DUNES -> DuneRenderer.render(bitmap, params)
            WallpaperPatternType.ORGANIC_SCALLOPS -> ScallopBadgeRenderer.render(bitmap, params)
            WallpaperPatternType.PASTURE_FOLIAGE -> PastureFoliageRenderer.render(bitmap, params)
        }
    }
}
