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
import kotlin.random.Random

/**
 * Minimalist Planar Diagonal Slices Engine.
 * Renders sharp razor-edge geometric polygons, layered angular cuts,
 * and subtle cast shadows between diagonal planes.
 */
object SlicesRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        NewGenerativeEngines.GeometricSlicesRenderer.render(bitmap, params)
    }
}
