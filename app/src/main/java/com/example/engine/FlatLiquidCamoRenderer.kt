package com.example.engine

import android.graphics.Bitmap

/**
 * Flat Organic Liquid Camo Procedural Wallpaper Engine.
 * Delegates directly to LiquidCamoRenderer for smooth, continuous cubic Bézier rivers.
 */
object FlatLiquidCamoRenderer : WallpaperRenderer {
    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        LiquidCamoRenderer.render(bitmap, params)
    }
}
