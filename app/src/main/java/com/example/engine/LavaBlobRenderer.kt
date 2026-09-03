package com.example.engine

import android.graphics.Bitmap

/**
 * Lava Blob Procedural Wallpaper Engine.
 * Delegates directly to FlatLiquidCamoRenderer to eliminate all 3D gloss, specular shine,
 * and radial drop shadows in favor of pure authentic 2D liquid camo vector art.
 */
object LavaBlobRenderer : WallpaperRenderer {
    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        LiquidCamoRenderer.render(bitmap, params)
    }
}
