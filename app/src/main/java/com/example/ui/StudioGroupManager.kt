package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.engine.CustomCanvasShape
import com.example.engine.StudioImageLayer
import com.example.engine.StudioItem
import com.example.engine.StudioTextLayer
import com.example.engine.WallpaperParams

/**
 * Geometric bounding box for a multi-selected group of Studio Canvas entities.
 */
data class GroupBounds(
    val minNormX: Float,
    val minNormY: Float,
    val maxNormX: Float,
    val maxNormY: Float
) {
    val centerNormX: Float get() = (minNormX + maxNormX) / 2f
    val centerNormY: Float get() = (minNormY + maxNormY) / 2f
    val widthNorm: Float get() = (maxNormX - minNormX).coerceAtLeast(0.05f)
    val heightNorm: Float get() = (maxNormY - minNormY).coerceAtLeast(0.05f)

    fun toPixelRect(canvasW: Float, canvasH: Float): Rect {
        return Rect(
            left = minNormX * canvasW,
            top = minNormY * canvasH,
            right = maxNormX * canvasW,
            bottom = maxNormY * canvasH
        )
    }
}

/**
 * Multi-Element Group Transform & Selection Engine.
 * Supports simultaneous multi-item selection, unified bounding box hit-testing,
 * group translation, group proportional scaling, and atomic group deletion.
 */
object StudioGroupManager {

    /**
     * Calculates the tight enclosing normalized bounding box for all selected items.
     */
    fun calculateGroupBounds(
        selectedIds: Set<String>,
        params: WallpaperParams
    ): GroupBounds? {
        if (selectedIds.isEmpty()) return null

        val selectedShapes = params.customShapes.filter { it.id in selectedIds }
        val selectedTexts = params.customTexts.filter { it.id in selectedIds }
        val selectedImages = params.customImages.filter { it.id in selectedIds }

        if (selectedShapes.isEmpty() && selectedTexts.isEmpty() && selectedImages.isEmpty()) {
            return null
        }

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        for (shape in selectedShapes) {
            val halfW = (shape.normalizedWidth / 2f).coerceAtLeast(0.02f)
            val halfH = (shape.normalizedHeight / 2f).coerceAtLeast(0.02f)
            minX = minOf(minX, shape.normalizedX - halfW)
            minY = minOf(minY, shape.normalizedY - halfH)
            maxX = maxOf(maxX, shape.normalizedX + halfW)
            maxY = maxOf(maxY, shape.normalizedY + halfH)
        }

        for (text in selectedTexts) {
            val halfS = (text.normalizedSize / 2f).coerceAtLeast(0.02f)
            minX = minOf(minX, text.normalizedX - halfS * 1.5f)
            minY = minOf(minY, text.normalizedY - halfS)
            maxX = maxOf(maxX, text.normalizedX + halfS * 1.5f)
            maxY = maxOf(maxY, text.normalizedY + halfS)
        }

        for (image in selectedImages) {
            val halfW = (image.normalizedWidth / 2f).coerceAtLeast(0.02f)
            val halfH = (image.normalizedHeight / 2f).coerceAtLeast(0.02f)
            minX = minOf(minX, image.normalizedX - halfW)
            minY = minOf(minY, image.normalizedY - halfH)
            maxX = maxOf(maxX, image.normalizedX + halfW)
            maxY = maxOf(maxY, image.normalizedY + halfH)
        }

        return GroupBounds(
            minNormX = minX.coerceIn(0f, 1f),
            minNormY = minY.coerceIn(0f, 1f),
            maxNormX = maxX.coerceIn(0f, 1f),
            maxNormY = maxY.coerceIn(0f, 1f)
        )
    }

    /**
     * Translates all elements in the selected group simultaneously by delta in normalized space.
     */
    fun translateGroup(
        params: WallpaperParams,
        selectedIds: Set<String>,
        deltaNormX: Float,
        deltaNormY: Float
    ): WallpaperParams {
        if (selectedIds.isEmpty()) return params

        val updatedShapes = params.customShapes.map { shape ->
            if (shape.id in selectedIds) {
                shape.copy(
                    normalizedX = (shape.normalizedX + deltaNormX).coerceIn(0.05f, 0.95f),
                    normalizedY = (shape.normalizedY + deltaNormY).coerceIn(0.05f, 0.95f)
                )
            } else shape
        }

        val updatedTexts = params.customTexts.map { text ->
            if (text.id in selectedIds) {
                text.copy(
                    normalizedX = (text.normalizedX + deltaNormX).coerceIn(0.05f, 0.95f),
                    normalizedY = (text.normalizedY + deltaNormY).coerceIn(0.05f, 0.95f)
                )
            } else text
        }

        val updatedImages = params.customImages.map { image ->
            if (image.id in selectedIds) {
                image.copy(
                    normalizedX = (image.normalizedX + deltaNormX).coerceIn(0.05f, 0.95f),
                    normalizedY = (image.normalizedY + deltaNormY).coerceIn(0.05f, 0.95f)
                )
            } else image
        }

        return params.copy(
            customShapes = updatedShapes,
            customTexts = updatedTexts,
            customImages = updatedImages
        )
    }

    /**
     * Scales all elements in the selected group relative to the group's center.
     */
    fun scaleGroup(
        params: WallpaperParams,
        selectedIds: Set<String>,
        scaleFactor: Float,
        groupCenterNormX: Float,
        groupCenterNormY: Float
    ): WallpaperParams {
        if (selectedIds.isEmpty()) return params

        val updatedShapes = params.customShapes.map { shape ->
            if (shape.id in selectedIds) {
                val relX = (shape.normalizedX - groupCenterNormX) * scaleFactor
                val relY = (shape.normalizedY - groupCenterNormY) * scaleFactor
                shape.copy(
                    normalizedX = (groupCenterNormX + relX).coerceIn(0.05f, 0.95f),
                    normalizedY = (groupCenterNormY + relY).coerceIn(0.05f, 0.95f),
                    normalizedWidth = (shape.normalizedWidth * scaleFactor).coerceIn(0.08f, 1.4f),
                    normalizedHeight = (shape.normalizedHeight * scaleFactor).coerceIn(0.08f, 1.4f)
                )
            } else shape
        }

        val updatedTexts = params.customTexts.map { text ->
            if (text.id in selectedIds) {
                val relX = (text.normalizedX - groupCenterNormX) * scaleFactor
                val relY = (text.normalizedY - groupCenterNormY) * scaleFactor
                text.copy(
                    normalizedX = (groupCenterNormX + relX).coerceIn(0.05f, 0.95f),
                    normalizedY = (groupCenterNormY + relY).coerceIn(0.05f, 0.95f),
                    normalizedSize = (text.normalizedSize * scaleFactor).coerceIn(0.05f, 0.6f)
                )
            } else text
        }

        val updatedImages = params.customImages.map { image ->
            if (image.id in selectedIds) {
                val relX = (image.normalizedX - groupCenterNormX) * scaleFactor
                val relY = (image.normalizedY - groupCenterNormY) * scaleFactor
                image.copy(
                    normalizedX = (groupCenterNormX + relX).coerceIn(0.05f, 0.95f),
                    normalizedY = (groupCenterNormY + relY).coerceIn(0.05f, 0.95f),
                    normalizedWidth = (image.normalizedWidth * scaleFactor).coerceIn(0.08f, 1.4f),
                    normalizedHeight = (image.normalizedHeight * scaleFactor).coerceIn(0.08f, 1.4f)
                )
            } else image
        }

        return params.copy(
            customShapes = updatedShapes,
            customTexts = updatedTexts,
            customImages = updatedImages
        )
    }

    /**
     * Deletes all elements belonging to the selected group.
     */
    fun deleteGroup(
        params: WallpaperParams,
        selectedIds: Set<String>
    ): WallpaperParams {
        return params.copy(
            customShapes = params.customShapes.filterNot { it.id in selectedIds },
            customTexts = params.customTexts.filterNot { it.id in selectedIds },
            customImages = params.customImages.filterNot { it.id in selectedIds }
        )
    }
}
