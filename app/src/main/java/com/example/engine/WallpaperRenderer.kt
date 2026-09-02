package com.example.engine

import android.graphics.Bitmap

/**
 * Common interface implemented by all deterministic 2D procedural wallpaper engines.
 */
interface WallpaperRenderer {
    fun render(bitmap: Bitmap, params: WallpaperParams)
}
