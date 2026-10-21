package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

/**
 * Minimalist Architectural Grid Engine.
 * Renders precise multi-scale coordinate grids, intersecting crosshairs,
 * architectural metric lines, and blueprint ticks.
 */
object GridsRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        NewGenerativeEngines.MondrianBspGridsRenderer.render(bitmap, params)
    }
}
