package com.example.engine

import android.graphics.Path
import android.graphics.RectF
import android.os.Build

/**
 * Material 3 Expressive Clover Shape Generator.
 *
 * Produces a single unified compound vector path without any internal wireframe seams,
 * overlapping circle arcs, or internal lines.
 * Uses native Path.Op.UNION on Android (API 19+) to combine the 4 tangent lobes
 * and center squircle into a clean, seamless silhouette.
 */
object StudioCloverShape {

    /**
     * Builds a unified 4-Leaf Clover path centered at (cx, cy) with bounds (w, h).
     * Guaranteed to have zero internal overlapping edges.
     */
    fun build4LeafCloverPath(
        cx: Float,
        cy: Float,
        w: Float,
        h: Float
    ): Path {
        val halfW = w / 2f
        val halfH = h / 2f
        val r = minOf(halfW, halfH)
        val lobeR = r * 0.48f
        val offset = r * 0.38f

        val topLobe = Path().apply {
            addOval(RectF(cx - lobeR, cy - offset - lobeR, cx + lobeR, cy - offset + lobeR), Path.Direction.CW)
        }
        val rightLobe = Path().apply {
            addOval(RectF(cx + offset - lobeR, cy - lobeR, cx + offset + lobeR, cy + lobeR), Path.Direction.CW)
        }
        val bottomLobe = Path().apply {
            addOval(RectF(cx - lobeR, cy + offset - lobeR, cx + lobeR, cy + offset + lobeR), Path.Direction.CW)
        }
        val leftLobe = Path().apply {
            addOval(RectF(cx - offset - lobeR, cy - lobeR, cx - offset + lobeR, cy + lobeR), Path.Direction.CW)
        }
        val centerSquircle = Path().apply {
            val centerR = lobeR * 0.82f
            addRoundRect(
                RectF(cx - centerR, cy - centerR, cx + centerR, cy + centerR),
                centerR * 0.5f,
                centerR * 0.5f,
                Path.Direction.CW
            )
        }

        val unionPath = Path()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            unionPath.op(topLobe, Path.Op.UNION)
            unionPath.op(rightLobe, Path.Op.UNION)
            unionPath.op(bottomLobe, Path.Op.UNION)
            unionPath.op(leftLobe, Path.Op.UNION)
            unionPath.op(centerSquircle, Path.Op.UNION)
        } else {
            // Fallback for pre-KitKat: add elements to single path
            unionPath.addPath(topLobe)
            unionPath.addPath(rightLobe)
            unionPath.addPath(bottomLobe)
            unionPath.addPath(leftLobe)
            unionPath.addPath(centerSquircle)
        }

        return unionPath
    }

    /**
     * Builds a unified 8-Leaf Clover Bloom path centered at (cx, cy) with bounds (w, h).
     * Unified into a single closed outer contour with zero internal overlapping lines.
     */
    fun build8LeafCloverPath(
        cx: Float,
        cy: Float,
        w: Float,
        h: Float
    ): Path {
        val halfW = w / 2f
        val halfH = h / 2f
        val r = minOf(halfW, halfH)
        val lobeR = r * 0.36f
        val offset = r * 0.52f

        val unionPath = Path()
        val centerCircle = Path().apply {
            addCircle(cx, cy, r * 0.52f, Path.Direction.CW)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            unionPath.op(centerCircle, Path.Op.UNION)
            for (i in 0 until 8) {
                val angle = (i * 45f) * (kotlin.math.PI.toFloat() / 180f)
                val lx = cx + offset * kotlin.math.cos(angle)
                val ly = cy + offset * kotlin.math.sin(angle)
                val lobe = Path().apply {
                    addCircle(lx, ly, lobeR, Path.Direction.CW)
                }
                unionPath.op(lobe, Path.Op.UNION)
            }
        } else {
            unionPath.addPath(centerCircle)
            for (i in 0 until 8) {
                val angle = (i * 45f) * (kotlin.math.PI.toFloat() / 180f)
                val lx = cx + offset * kotlin.math.cos(angle)
                val ly = cy + offset * kotlin.math.sin(angle)
                unionPath.addCircle(lx, ly, lobeR, Path.Direction.CW)
            }
        }

        return unionPath
    }
}
