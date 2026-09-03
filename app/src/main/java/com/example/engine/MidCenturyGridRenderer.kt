package com.example.engine

import android.graphics.Bitmap

/**
 * Mid-Century Grid Procedural Wallpaper Engine.
 * Delegates directly to ModularBauhausRenderer for a strict 4 x 7 geometric tile matrix.
 */
object MidCenturyGridRenderer : WallpaperRenderer {
    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        ModularBauhausRenderer.render(bitmap, params)
    }
}
