package com.example.engine

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Master dispatcher for the curated procedural Material 3 wallpaper generators.
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
            WallpaperPatternType.MOUNTAINS -> MountainRenderer.render(bitmap, params)
            WallpaperPatternType.WAVES -> HarmonicWaveRenderer.render(bitmap, params)
            WallpaperPatternType.STACKED_PILLS -> StackedPillsRenderer.render(bitmap, params)
            WallpaperPatternType.DOT_GRID -> DotGridRenderer.render(bitmap, params)
            WallpaperPatternType.CONTOURS -> TopographicRenderer.render(bitmap, params)
            WallpaperPatternType.BAUHAUS_SEMICIRCLE -> BauhausSemicircleRenderer.render(bitmap, params)
            WallpaperPatternType.FLUTED_ARCHES -> FlutedArchesRenderer.render(bitmap, params)
            WallpaperPatternType.LAVA_BLOB -> LavaBlobRenderer.render(bitmap, params)
            WallpaperPatternType.STUDIO -> ProceduralM3Assembler.renderCustomShapes(bitmap, params)
        }
    }
}
