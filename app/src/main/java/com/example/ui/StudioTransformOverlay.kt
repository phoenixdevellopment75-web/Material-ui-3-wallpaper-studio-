package com.example.ui

import android.graphics.DashPathEffect
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 8-Point Selection Bounding Box & 2D Transformation Engine.
 *
 * Provides:
 * - 4 Corner handles: Proportional Scaling
 * - 4 Midpoint handles: Independent Horizontal & Vertical Stretch
 * - 1 Rotation handle: Free 360-degree rotation
 */
enum class TransformHandle(val id: Int) {
    TOP_LEFT(0),
    TOP_RIGHT(1),
    BOTTOM_RIGHT(2),
    BOTTOM_LEFT(3),
    ROTATION(4),
    TOP_CENTER(5),     // Independent vertical stretch (height)
    RIGHT_CENTER(6),   // Independent horizontal stretch (width)
    BOTTOM_CENTER(7),  // Independent vertical stretch (height)
    LEFT_CENTER(8);    // Independent horizontal stretch (width)

    companion object {
        fun fromId(id: Int): TransformHandle? = entries.find { it.id == id }
    }
}

data class HandlePoint(
    val handle: TransformHandle,
    val canvasPosition: Offset,
    val localOffset: Offset
)

object StudioTransformOverlay {

    /**
     * Compute all 8 handles + 1 rotation handle in rotated canvas coordinate space.
     */
    fun calculateHandlePositions(
        cx: Float,
        cy: Float,
        halfW: Float,
        halfH: Float,
        stemLength: Float,
        rotRad: Float
    ): List<HandlePoint> {
        val handles = mutableListOf<HandlePoint>()

        // 4 Corner handles
        val corners = listOf(
            TransformHandle.TOP_LEFT to Offset(-halfW, -halfH),
            TransformHandle.TOP_RIGHT to Offset(halfW, -halfH),
            TransformHandle.BOTTOM_RIGHT to Offset(halfW, halfH),
            TransformHandle.BOTTOM_LEFT to Offset(-halfW, halfH)
        )

        for ((handle, local) in corners) {
            val rotX = cx + local.x * cos(rotRad) - local.y * sin(rotRad)
            val rotY = cy + local.x * sin(rotRad) + local.y * cos(rotRad)
            handles.add(HandlePoint(handle, Offset(rotX, rotY), local))
        }

        // 1 Rotation handle (top stem)
        val rotLocal = Offset(0f, -halfH - stemLength)
        val rotStemX = cx + rotLocal.x * cos(rotRad) - rotLocal.y * sin(rotRad)
        val rotStemY = cy + rotLocal.x * sin(rotRad) + rotLocal.y * cos(rotRad)
        handles.add(HandlePoint(TransformHandle.ROTATION, Offset(rotStemX, rotStemY), rotLocal))

        // 4 Midpoint handles (independent horizontal and vertical stretching)
        val midpoints = listOf(
            TransformHandle.TOP_CENTER to Offset(0f, -halfH),
            TransformHandle.RIGHT_CENTER to Offset(halfW, 0f),
            TransformHandle.BOTTOM_CENTER to Offset(0f, halfH),
            TransformHandle.LEFT_CENTER to Offset(-halfW, 0f)
        )

        for ((handle, local) in midpoints) {
            val rotX = cx + local.x * cos(rotRad) - local.y * sin(rotRad)
            val rotY = cy + local.x * sin(rotRad) + local.y * cos(rotRad)
            handles.add(HandlePoint(handle, Offset(rotX, rotY), local))
        }

        return handles
    }

    /**
     * Hit-test touch coordinate against all handles with generous touch padding.
     */
    fun hitTest(
        touch: Offset,
        handles: List<HandlePoint>,
        touchRadiusPx: Float
    ): TransformHandle? {
        var closestHandle: TransformHandle? = null
        var closestDist = Float.MAX_VALUE

        for (item in handles) {
            val dx = touch.x - item.canvasPosition.x
            val dy = touch.y - item.canvasPosition.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist <= touchRadiusPx && dist < closestDist) {
                closestDist = dist
                closestHandle = item.handle
            }
        }
        return closestHandle
    }

    /**
     * Project a point from canvas coordinate space into local unrotated entity space centered at (cx, cy).
     */
    fun projectToLocal(touch: Offset, cx: Float, cy: Float, rotRad: Float): Offset {
        val dx = touch.x - cx
        val dy = touch.y - cy
        val cosA = cos(rotRad)
        val sinA = sin(rotRad)
        // Inverse 2D rotation: R(-rotRad)
        val localX = dx * cosA + dy * sinA
        val localY = -dx * sinA + dy * cosA
        return Offset(localX, localY)
    }

    /**
     * Draw the complete 8-point transformation bounding box and tactile handles.
     */
    fun DrawScope.draw8PointBoundingBox(
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        rotationDeg: Float,
        primaryColor: Color = Color(0xFFD0BCFF),
        outlineColor: Color = Color.White
    ) {
        val halfW = (w / 2f) + 16.dp.toPx()
        val halfH = (h / 2f) + 16.dp.toPx()
        val stemLength = 26.dp.toPx()
        val cornerRadiusPx = 10.dp.toPx()
        val cornerDotRadiusPx = 4.dp.toPx()
        val midPillW = 14.dp.toPx()
        val midPillH = 6.dp.toPx()

        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            native.save()
            if (rotationDeg != 0f) {
                native.rotate(rotationDeg, cx, cy)
            }

            // 1. Dashed Bounding Outline
            val boxPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                style = AndroidPaint.Style.STROKE
                strokeWidth = 2.dp.toPx()
                pathEffect = DashPathEffect(floatArrayOf(12f, 10f), 0f)
                alpha = 230
            }
            native.drawRoundRect(
                cx - halfW, cy - halfH, cx + halfW, cy + halfH,
                16.dp.toPx(), 16.dp.toPx(), boxPaint
            )

            // 2. Rotation Stem Line
            val stemPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                style = AndroidPaint.Style.STROKE
                strokeWidth = 2.dp.toPx()
                alpha = 220
            }
            native.drawLine(cx, cy - halfH, cx, cy - halfH - stemLength, stemPaint)

            // Common handle paints
            val outerHandlePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                style = AndroidPaint.Style.FILL
                setShadowLayer(6f, 0f, 2f, 0x66000000)
            }
            val innerCornerPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(103, 80, 164) // M3 Primary
                style = AndroidPaint.Style.FILL
            }
            val midHandlePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(103, 80, 164)
                style = AndroidPaint.Style.FILL
            }
            val rotHandlePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(220, 54, 46) // Accent Red/Orange
                style = AndroidPaint.Style.FILL
            }

            // 3. 4 Corner Handles (Proportional scale)
            val corners = arrayOf(
                Pair(cx - halfW, cy - halfH),
                Pair(cx + halfW, cy - halfH),
                Pair(cx + halfW, cy + halfH),
                Pair(cx - halfW, cy + halfH)
            )
            for ((x, y) in corners) {
                native.drawCircle(x, y, cornerRadiusPx, outerHandlePaint)
                native.drawCircle(x, y, cornerDotRadiusPx, innerCornerPaint)
            }

            // 4. 4 Midpoint Handles (Horizontal and Vertical stretch)
            // Top midpoint (stretches vertical height)
            native.drawRoundRect(
                cx - (midPillW / 2f), cy - halfH - (midPillH / 2f),
                cx + (midPillW / 2f), cy - halfH + (midPillH / 2f),
                4.dp.toPx(), 4.dp.toPx(), outerHandlePaint
            )
            native.drawRoundRect(
                cx - (midPillW / 2f) + 2f, cy - halfH - (midPillH / 2f) + 2f,
                cx + (midPillW / 2f) - 2f, cy - halfH + (midPillH / 2f) - 2f,
                3.dp.toPx(), 3.dp.toPx(), midHandlePaint
            )

            // Bottom midpoint (stretches vertical height)
            native.drawRoundRect(
                cx - (midPillW / 2f), cy + halfH - (midPillH / 2f),
                cx + (midPillW / 2f), cy + halfH + (midPillH / 2f),
                4.dp.toPx(), 4.dp.toPx(), outerHandlePaint
            )
            native.drawRoundRect(
                cx - (midPillW / 2f) + 2f, cy + halfH - (midPillH / 2f) + 2f,
                cx + (midPillW / 2f) - 2f, cy + halfH + (midPillH / 2f) - 2f,
                3.dp.toPx(), 3.dp.toPx(), midHandlePaint
            )

            // Left midpoint (stretches horizontal width)
            native.drawRoundRect(
                cx - halfW - (midPillH / 2f), cy - (midPillW / 2f),
                cx - halfW + (midPillH / 2f), cy + (midPillW / 2f),
                4.dp.toPx(), 4.dp.toPx(), outerHandlePaint
            )
            native.drawRoundRect(
                cx - halfW - (midPillH / 2f) + 2f, cy - (midPillW / 2f) + 2f,
                cx - halfW + (midPillH / 2f) - 2f, cy + (midPillW / 2f) - 2f,
                3.dp.toPx(), 3.dp.toPx(), midHandlePaint
            )

            // Right midpoint (stretches horizontal width)
            native.drawRoundRect(
                cx + halfW - (midPillH / 2f), cy - (midPillW / 2f),
                cx + halfW + (midPillH / 2f), cy + (midPillW / 2f),
                4.dp.toPx(), 4.dp.toPx(), outerHandlePaint
            )
            native.drawRoundRect(
                cx + halfW - (midPillH / 2f) + 2f, cy - (midPillW / 2f) + 2f,
                cx + halfW + (midPillH / 2f) - 2f, cy + (midPillW / 2f) - 2f,
                3.dp.toPx(), 3.dp.toPx(), midHandlePaint
            )

            // 5. Rotation Handle Node
            val rotY = cy - halfH - stemLength
            native.drawCircle(cx, rotY, cornerRadiusPx, outerHandlePaint)
            native.drawCircle(cx, rotY, cornerDotRadiusPx, rotHandlePaint)

            native.restore()
        }
    }
}
