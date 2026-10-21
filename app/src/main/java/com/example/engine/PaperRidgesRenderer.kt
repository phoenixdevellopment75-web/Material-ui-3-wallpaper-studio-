package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.max
import kotlin.math.min

/**
 * Minimalist Paper Ridges Engine.
 * Renders tactile embossed stepped paper folds, alternating lit and shadowed facets,
 * and origami architectural ridges with soft creased shadows.
 */
object PaperRidgesRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        NewGenerativeEngines.LayeredPaperRidgesRenderer.render(bitmap, params)
    }
}
