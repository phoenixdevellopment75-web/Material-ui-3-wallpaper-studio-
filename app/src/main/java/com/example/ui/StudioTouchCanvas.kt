package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.CustomCanvasShape
import com.example.engine.ProceduralM3Assembler
import com.example.engine.WallpaperParams
import com.example.palette.ColorPalette
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance, zero-friction interactive touch canvas for Studio mode.
 *
 * Key Architectural Optimizations:
 * 1. Decouples drag translation from heavy layout recompositions:
 *    Local offset is tracked in high-speed graphics layer pass and only committed on drag end.
 * 2. Direct Corner Resize Handles (On-Shape Transform):
 *    4 distinct anchor nodes at bounding box corners. Dragging any handle scales proportionally
 *    with a strict 1:1 aspect ratio lock (clamped to minimum 48dp).
 * 3. Two-finger pinch-to-scale and rotation gestures directly on canvas.
 * 4. Tap selection with tactile elevation drop shadows.
 */
@Composable
fun StudioTouchCanvas(
    params: WallpaperParams,
    selectedShapeId: String?,
    onSelectShape: (String?) -> Unit,
    onCommitShapePosition: (String, Float, Float) -> Unit,
    onCommitShapeScale: (String, Float) -> Unit,
    onCommitShapeRotation: (String, Float) -> Unit,
    onSetShapeColorIndex: (String, Int) -> Unit,
    onBringShapeToFront: (String) -> Unit,
    onSendShapeToBack: (String) -> Unit,
    onDeleteShape: (String) -> Unit,
    onDuplicateShape: (String) -> Unit,
    onToggleShapeWireframe: (String) -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val minTouchTargetPx = with(density) { 48.dp.toPx() }
    val handleTouchRadiusPx = with(density) { 28.dp.toPx() }

    val palette = params.palette
    val bgColors = if (palette.colors.size >= 2) {
        listOf(palette.colors[0], palette.colors[1])
    } else {
        listOf(Color(0xFF1C1B1F), Color(0xFF2B2930))
    }

    val selectedShape = params.customShapes.find { it.id == selectedShapeId }

    // Fast local drag offsets to avoid recomposition friction
    var localDragOffset by remember(selectedShapeId) { mutableStateOf(Offset.Zero) }
    var activeCornerHandleIndex by remember { mutableIntStateOf(-1) }
    var localScaleMultiplier by remember(selectedShapeId) { mutableFloatStateOf(1f) }
    var localRotationDeg by remember(selectedShapeId) { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier
            .testTag("studio_touch_canvas_root")
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .then(
                    if (isFullscreen) Modifier.fillMaxSize()
                    else Modifier
                        .fillMaxSize(0.94f)
                        .aspectRatio(params.aspectRatio.ratio)
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(28.dp)
                        )
                        .shadow(16.dp, RoundedCornerShape(28.dp))
                )
                .background(Brush.verticalGradient(bgColors))
                // Two-finger multi-touch transform (Pinch-to-scale & rotate directly on canvas)
                .pointerInput(selectedShapeId, params.scale) {
                    detectTransformGestures { _, _, zoom, rotation ->
                        if (selectedShapeId != null) {
                            val curr = params.customShapes.find { it.id == selectedShapeId }
                            if (curr != null) {
                                if (zoom != 1f) {
                                    val newW = (curr.normalizedWidth * zoom).coerceIn(0.12f, 1.4f)
                                    onCommitShapeScale(selectedShapeId, newW)
                                }
                                if (rotation != 0f) {
                                    val newRot = (curr.rotationDeg + rotation) % 360f
                                    onCommitShapeRotation(selectedShapeId, newRot)
                                }
                            }
                        }
                    }
                }
                // Tap hit testing & selection
                .pointerInput(params.customShapes) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val baseDim = minOf(canvasW, canvasH)
                            val normTapX = tapOffset.x / canvasW
                            val normTapY = tapOffset.y / canvasH

                            val hitShape = params.customShapes
                                .sortedByDescending { it.zIndex }
                                .firstOrNull { shape ->
                                    val scaleFactor = params.scale * baseDim
                                    val shapeW = shape.normalizedWidth * scaleFactor
                                    val shapeH = if (shape.type.isProportional1to1) {
                                        shape.normalizedWidth * scaleFactor
                                    } else {
                                        shape.normalizedHeight * scaleFactor
                                    }
                                    val halfNormW = (shapeW / canvasW) / 1.7f
                                    val halfNormH = (shapeH / canvasH) / 1.7f
                                    val dx = kotlin.math.abs(normTapX - shape.normalizedX)
                                    val dy = kotlin.math.abs(normTapY - shape.normalizedY)
                                    dx <= halfNormW && dy <= halfNormH
                                }

                            onSelectShape(hitShape?.id)
                        }
                    )
                }
                // Zero-friction drag & direct corner resize handles
                .pointerInput(selectedShapeId, params.customShapes, params.scale) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            localDragOffset = Offset.Zero
                            activeCornerHandleIndex = -1
                            localScaleMultiplier = 1f
                            localRotationDeg = null

                            if (selectedShape != null) {
                                val canvasW = size.width.toFloat()
                                val canvasH = size.height.toFloat()
                                val baseDim = minOf(canvasW, canvasH)
                                val scaleFactor = params.scale * baseDim
                                val cx = selectedShape.normalizedX * canvasW
                                val cy = selectedShape.normalizedY * canvasH
                                val w = selectedShape.normalizedWidth * scaleFactor
                                val h = if (selectedShape.type.isProportional1to1) w else selectedShape.normalizedHeight * scaleFactor
                                val halfW = (w / 2f) + 16.dp.toPx()
                                val halfH = (h / 2f) + 16.dp.toPx()
                                val stemLength = 26.dp.toPx()
                                val rotRad = ((selectedShape.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                // Calculate 4 corner handle coordinates + 1 rotation stem handle
                                val cornerOffsets = listOf(
                                    Offset(-halfW, -halfH), // 0: Top-Left
                                    Offset(halfW, -halfH),  // 1: Top-Right
                                    Offset(halfW, halfH),   // 2: Bottom-Right
                                    Offset(-halfW, halfH)   // 3: Bottom-Left
                                )

                                cornerOffsets.forEachIndexed { index, corner ->
                                    val rotatedX = cx + corner.x * cos(rotRad) - corner.y * sin(rotRad)
                                    val rotatedY = cy + corner.x * sin(rotRad) + corner.y * cos(rotRad)
                                    val dist = sqrt((startOffset.x - rotatedX) * (startOffset.x - rotatedX) + (startOffset.y - rotatedY) * (startOffset.y - rotatedY))
                                    if (dist <= handleTouchRadiusPx) {
                                        activeCornerHandleIndex = index
                                    }
                                }

                                // Check rotation stem handle (above top center)
                                val rotStemX = cx - (-halfH - stemLength) * sin(rotRad)
                                val rotStemY = cy + (-halfH - stemLength) * cos(rotRad)
                                val distStem = sqrt((startOffset.x - rotStemX) * (startOffset.x - rotStemX) + (startOffset.y - rotStemY) * (startOffset.y - rotStemY))
                                if (distStem <= handleTouchRadiusPx) {
                                    activeCornerHandleIndex = 4 // Rotation mode
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val baseDim = minOf(canvasW, canvasH)
                            val scaleFactor = params.scale * baseDim

                            if (activeCornerHandleIndex == 4 && selectedShape != null) {
                                // Rotation Handle Mode: calculate smooth 360-deg angle around center
                                val cx = selectedShape.normalizedX * canvasW
                                val cy = selectedShape.normalizedY * canvasH
                                val dx = change.position.x - cx
                                val dy = change.position.y - cy
                                val angleRad = atan2(dy, dx)
                                var angleDeg = (angleRad * (180f / PI.toFloat())) + 90f - params.rotationDegrees
                                while (angleDeg < 0f) angleDeg += 360f
                                localRotationDeg = angleDeg % 360f
                            } else if (activeCornerHandleIndex in 0..3 && selectedShape != null) {
                                // Direct Corner Resize Mode: scale shape proportionally with locked 1:1 aspect ratio
                                val cx = selectedShape.normalizedX * canvasW
                                val cy = selectedShape.normalizedY * canvasH
                                val currentDist = sqrt((change.position.x - cx) * (change.position.x - cx) + (change.position.y - cy) * (change.position.y - cy))
                                val initialHalfDiag = (selectedShape.normalizedWidth * scaleFactor) / 1.414f
                                val scaleMultiplier = if (initialHalfDiag > 0f) currentDist / initialHalfDiag else 1f
                                localScaleMultiplier = scaleMultiplier.coerceIn(0.2f, 3.5f)
                            } else if (selectedShape != null) {
                                // Zero-Friction Translation Mode: decoupled local offset for 120fps motion
                                localDragOffset += dragAmount
                            }
                        },
                        onDragEnd = {
                            if (selectedShape != null) {
                                val canvasW = size.width.toFloat()
                                val canvasH = size.height.toFloat()
                                val baseDim = minOf(canvasW, canvasH)
                                val scaleFactor = params.scale * baseDim

                                if (activeCornerHandleIndex == 4 && localRotationDeg != null) {
                                    onCommitShapeRotation(selectedShape.id, localRotationDeg!!)
                                } else if (activeCornerHandleIndex in 0..3) {
                                    val newNormW = (selectedShape.normalizedWidth * localScaleMultiplier)
                                        .coerceIn(minTouchTargetPx / scaleFactor, 1.4f)
                                    onCommitShapeScale(selectedShape.id, newNormW)
                                } else if (activeCornerHandleIndex == -1 && localDragOffset != Offset.Zero) {
                                    val newX = (selectedShape.normalizedX + (localDragOffset.x / canvasW)).coerceIn(0.05f, 0.95f)
                                    val newY = (selectedShape.normalizedY + (localDragOffset.y / canvasH)).coerceIn(0.05f, 0.95f)
                                    onCommitShapePosition(selectedShape.id, newX, newY)
                                }
                            }
                            localDragOffset = Offset.Zero
                            activeCornerHandleIndex = -1
                            localScaleMultiplier = 1f
                            localRotationDeg = null
                        },
                        onDragCancel = {
                            localDragOffset = Offset.Zero
                            activeCornerHandleIndex = -1
                            localScaleMultiplier = 1f
                            localRotationDeg = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sorted = params.customShapes.sortedBy { it.zIndex }
                val colors = palette.colors
                val baseDim = minOf(size.width, size.height)

                for (shape in sorted) {
                    val isSelected = shape.id == selectedShapeId
                    val dragX = if (isSelected) localDragOffset.x else 0f
                    val dragY = if (isSelected) localDragOffset.y else 0f

                    val cx = (shape.normalizedX * size.width) + dragX
                    val cy = (shape.normalizedY * size.height) + dragY
                    val scaleFactor = params.scale * baseDim
                    val effectiveScale = if (isSelected && activeCornerHandleIndex in 0..3) localScaleMultiplier else 1f
                    val effectiveRot = if (isSelected && activeCornerHandleIndex == 4 && localRotationDeg != null) {
                        localRotationDeg!!
                    } else {
                        shape.rotationDeg + params.rotationDegrees
                    }

                    val w = shape.normalizedWidth * scaleFactor * effectiveScale
                    val h = if (shape.type.isProportional1to1) {
                        w
                    } else {
                        shape.normalizedHeight * scaleFactor * effectiveScale
                    }

                    val shapeColor = if (shape.customColorHex != null) {
                        Color(shape.customColorHex.toULong())
                    } else {
                        colors[shape.colorIndex % colors.size]
                    }

                    // Native Canvas drawing for performance and elevation shadow
                    drawIntoCanvas { composeCanvas ->
                        val nativeCanvas = composeCanvas.nativeCanvas
                        ProceduralM3Assembler.drawSingleShape(
                            canvas = nativeCanvas,
                            type = shape.type,
                            cx = cx,
                            cy = cy,
                            w = w,
                            h = h,
                            rotationDeg = effectiveRot,
                            color = shapeColor.toArgb(),
                            opacity = shape.opacity,
                            isWireframe = shape.isWireframe || params.isWireframe,
                            strokeWidth = shape.strokeWidth * (size.width / 500f),
                            scallopLobes = shape.scallopLobes,
                            castShadow = true
                        )
                    }

                    // Draw direct corner transform handles and bounding box
                    if (isSelected) {
                        drawSelectionBoundingBoxWithCorners(
                            cx = cx,
                            cy = cy,
                            w = w,
                            h = h,
                            rotationDeg = effectiveRot
                        )
                    }
                }
            }

            // Floating Shape Inspector Bar
            if (selectedShape != null && !isFullscreen) {
                FloatingShapeInspectorBar(
                    shape = selectedShape,
                    palette = palette,
                    onColorSelected = { colorIdx -> onSetShapeColorIndex(selectedShape.id, colorIdx) },
                    onRotate = { deg -> onCommitShapeRotation(selectedShape.id, deg) },
                    onBringToFront = { onBringShapeToFront(selectedShape.id) },
                    onSendToBack = { onSendShapeToBack(selectedShape.id) },
                    onDuplicate = { onDuplicateShape(selectedShape.id) },
                    onDelete = { onDeleteShape(selectedShape.id) },
                    onToggleWireframe = { onToggleShapeWireframe(selectedShape.id) },
                    onDeselect = { onSelectShape(null) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 12.dp, end = 12.dp)
                )
            }
        }
    }
}

/**
 * Draws the bounding box and 4 prominent corner pill/circle anchor nodes.
 */
private fun DrawScope.drawSelectionBoundingBoxWithCorners(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    rotationDeg: Float
) {
    val halfW = (w / 2f) + 16.dp.toPx()
    val halfH = (h / 2f) + 16.dp.toPx()

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        native.save()
        if (rotationDeg != 0f) {
            native.rotate(rotationDeg, cx, cy)
        }

        // 1. Dashed Bounding Outline
        val boxPaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.dp.toPx()
            color = android.graphics.Color.WHITE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(16f, 12f), 0f)
            isAntiAlias = true
        }
        native.drawRoundRect(
            cx - halfW, cy - halfH, cx + halfW, cy + halfH,
            16.dp.toPx(), 16.dp.toPx(), boxPaint
        )

        // Rotation stem line extending upward
        val stemLength = 26.dp.toPx()
        val stemPaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.dp.toPx()
            color = android.graphics.Color.WHITE
            isAntiAlias = true
        }
        native.drawLine(cx, cy - halfH, cx, cy - halfH - stemLength, stemPaint)

        // 2. 4 Distinct Corner Pill/Circle Transform Anchors + Rotation Handle
        val outerHandlePaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            setShadowLayer(8f, 0f, 4f, 0x66000000)
        }
        val innerHandlePaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            color = 0xFF6750A4.toInt() // Primary purple dot
            isAntiAlias = true
        }
        val rotHandlePaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            color = 0xFF386A20.toInt() // Accent Green for rotation node
            isAntiAlias = true
        }

        val handleR = 9.dp.toPx()
        val innerR = 4.dp.toPx()

        val corners = listOf(
            Pair(cx - halfW, cy - halfH),
            Pair(cx + halfW, cy - halfH),
            Pair(cx + halfW, cy + halfH),
            Pair(cx - halfW, cy + halfH)
        )

        for ((hx, hy) in corners) {
            native.drawCircle(hx, hy, handleR, outerHandlePaint)
            native.drawCircle(hx, hy, innerR, innerHandlePaint)
        }

        // Draw dedicated rotation node
        native.drawCircle(cx, cy - halfH - stemLength, handleR + 2.dp.toPx(), outerHandlePaint)
        native.drawCircle(cx, cy - halfH - stemLength, innerR + 1.dp.toPx(), rotHandlePaint)

        native.restore()
    }
}

/**
 * Floating shape inspector bar with quick actions.
 */
@Composable
private fun FloatingShapeInspectorBar(
    shape: CustomCanvasShape,
    palette: ColorPalette,
    onColorSelected: (Int) -> Unit,
    onRotate: (Float) -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onToggleWireframe: () -> Unit,
    onDeselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${shape.type.displayName} • Drag corners to scale",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDeselect, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Deselect", modifier = Modifier.size(16.dp))
                }
            }

            // Quick Color Palette Swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.colors.forEachIndexed { index, color ->
                    val isSelected = index == (shape.colorIndex % palette.colors.size)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(index) }
                    )
                }
            }

            // Quick Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotate 45 deg
                IconButton(
                    onClick = { onRotate((shape.rotationDeg + 45f) % 360f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate 45°", modifier = Modifier.size(20.dp))
                }

                // Bring to front
                IconButton(onClick = onBringToFront, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FlipToFront, contentDescription = "Bring Forward", modifier = Modifier.size(20.dp))
                }

                // Send to back
                IconButton(onClick = onSendToBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FlipToBack, contentDescription = "Send Back", modifier = Modifier.size(20.dp))
                }

                // Wireframe toggle
                IconButton(onClick = onToggleWireframe, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.GridOn, contentDescription = "Wireframe Toggle", modifier = Modifier.size(20.dp))
                }

                // Duplicate
                IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(20.dp))
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
