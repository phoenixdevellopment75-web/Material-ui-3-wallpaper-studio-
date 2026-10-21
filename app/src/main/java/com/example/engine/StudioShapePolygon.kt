package com.example.engine

import android.graphics.Matrix
import android.graphics.Path as AndroidPath
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

/**
 * Margin-safe polygon path conversion eliminating clover and petal truncation.
 *
 * Adds an explicit safety margin to ensure outward-curving lobes (such as 4-leaf and 8-leaf clovers,
 * blooms, and sunbursts) are never clipped or chopped off flat by container bounds or canvas edges.
 */
object StudioShapePolygon {

    /**
     * Converts a RoundedPolygon into a normalized Compose Path strictly contained within
     * [0, width] x [0, height] with safe-bounds normalization (0.88f scale provides 6% clearance
     * around all outer lobes, completely eliminating bottom and lateral lobe truncation).
     */
    fun RoundedPolygon.toSafeNormalizedPath(width: Float, height: Float): ComposePath {
        val rawBounds = calculateBounds()
        val w = rawBounds[2] - rawBounds[0]
        val h = rawBounds[3] - rawBounds[1]
        val maxDimension = maxOf(w, h).coerceAtLeast(0.001f)

        // 0.88f scale provides 6% clearance around all outer lobes
        val scaleFactor = (minOf(width, height) * 0.88f) / maxDimension
        val matrix = Matrix().apply {
            postTranslate(-(rawBounds[0] + w / 2f), -(rawBounds[1] + h / 2f))
            postScale(scaleFactor, scaleFactor)
            postTranslate(width / 2f, height / 2f)
        }

        val path = this.toPath()
        path.transform(matrix)
        return path.asComposePath()
    }

    /**
     * Converts a RoundedPolygon into a normalized Android Path centered at (cx, cy)
     * with size (w, h) with safe-bounds normalization.
     */
    fun RoundedPolygon.toSafeNormalizedAndroidPath(
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        clearanceScale: Float = 0.88f
    ): AndroidPath {
        val rawBounds = calculateBounds()
        val bw = rawBounds[2] - rawBounds[0]
        val bh = rawBounds[3] - rawBounds[1]
        val maxDimension = maxOf(bw, bh).coerceAtLeast(0.001f)

        val scaleFactor = (minOf(w, h) * clearanceScale) / maxDimension
        val matrix = Matrix().apply {
            postTranslate(-(rawBounds[0] + bw / 2f), -(rawBounds[1] + bh / 2f))
            postScale(scaleFactor, scaleFactor)
            postTranslate(cx, cy)
        }

        val path = this.toPath()
        path.transform(matrix)
        return path
    }

    /**
     * Converts a RoundedPolygon into a normalized Compose Path strictly contained within
     * [0, targetWidth] x [0, targetHeight] with an explicit safety margin.
     */
    fun RoundedPolygon.toNormalizedComposePath(
        targetWidth: Float,
        targetHeight: Float,
        safetyMargin: Float = 0.88f
    ): ComposePath = toSafeNormalizedPath(targetWidth, targetHeight)

    /**
     * Converts a RoundedPolygon into a normalized Android Path centered at (cx, cy)
     * with size (w, h) with an explicit inner safety margin.
     */
    fun RoundedPolygon.toNormalizedAndroidPath(
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        safetyMargin: Float = 0.92f
    ): AndroidPath {
        val bounds = calculateBounds()
        val shapeWidth = bounds[2] - bounds[0]
        val shapeHeight = bounds[3] - bounds[1]
        val maxDim = maxOf(shapeWidth, shapeHeight).coerceAtLeast(0.001f)

        val scaleX = (w * safetyMargin) / maxDim
        val scaleY = (h * safetyMargin) / maxDim
        val scale = minOf(scaleX, scaleY)

        val matrix = Matrix().apply {
            postTranslate(-(bounds[0] + shapeWidth / 2f), -(bounds[1] + shapeHeight / 2f))
            postScale(scale, scale)
            postTranslate(cx, cy)
        }

        val androidPath = this.toPath()
        androidPath.transform(matrix)
        return androidPath
    }
}
