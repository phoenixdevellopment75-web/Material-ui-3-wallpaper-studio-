package com.example.engine

import android.graphics.Bitmap

/**
 * Bauhaus Semicircle Procedural Wallpaper Engine.
 * Delegates directly to BauhausGridRenderer for strict 4x7 modular semicircle/quadrant matrix.
 */
object BauhausSemicircleRenderer : WallpaperRenderer {
    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        BauhausGridRenderer.render(bitmap, params)
    }
}
