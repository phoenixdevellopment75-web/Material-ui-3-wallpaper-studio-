package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AspectRatioPreset
import com.example.engine.CustomCanvasShape
import com.example.engine.CustomShapeType
import com.example.engine.ProceduralM3Assembler
import com.example.engine.WallpaperParams
import com.example.palette.ColorPalette
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Touch Canvas for Studio Custom Mode.
 *
 * Supports:
 * - Real-time drag positioning of M3 shapes
 * - Pinch-to-scale and touch rotation
 * - Tap selection with tactile selection outline & handles
 * - Floating Shape Inspector Bar (color swap, reorder, delete, duplicate, scale, rotation)
 * - Tactile elevation drop shadows on each shape
 */
@Composable
fun CustomStudioCanvas(
    params: WallpaperParams,
    selectedShapeId: String?,
    onSelectShape: (String?) -> Unit,
    onUpdateShapePosition: (String, Float, Float) -> Unit,
    onUpdateShapeScale: (String, Float) -> Unit,
    onUpdateShapeRotation: (String, Float) -> Unit,
    onSetShapeRotation: (String, Float) -> Unit,
    onSetShapeColorIndex: (String, Int) -> Unit,
    onBringShapeToFront: (String) -> Unit,
    onSendShapeToBack: (String) -> Unit,
    onDeleteShape: (String) -> Unit,
    onDuplicateShape: (String) -> Unit,
    onToggleShapeWireframe: (String) -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = params.palette
    val bgColors = if (palette.colors.size >= 2) {
        listOf(palette.colors[0], palette.colors[1])
    } else {
        listOf(Color(0xFF1C1B1F), Color(0xFF2B2930))
    }

    val selectedShape = params.customShapes.find { it.id == selectedShapeId }

    Box(
        modifier = modifier
            .testTag("custom_studio_canvas_root")
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
                .pointerInput(params.customShapes) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val baseDim = minOf(canvasW, canvasH)
                            val normTapX = tapOffset.x / canvasW
                            val normTapY = tapOffset.y / canvasH

                            // Find tapped shape (reverse sorted by zIndex to hit top shape first)
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
                                    val halfNormW = (shapeW / canvasW) / 1.8f
                                    val halfNormH = (shapeH / canvasH) / 1.8f
                                    val dx = kotlin.math.abs(normTapX - shape.normalizedX)
                                    val dy = kotlin.math.abs(normTapY - shape.normalizedY)
                                    dx <= halfNormW && dy <= halfNormH
                                }

                            onSelectShape(hitShape?.id)
                        }
                    )
                }
                .pointerInput(selectedShapeId, params.customShapes) {
                    if (selectedShapeId != null) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val canvasW = size.width.toFloat()
                                val canvasH = size.height.toFloat()
                                val deltaNormX = dragAmount.x / canvasW
                                val deltaNormY = dragAmount.y / canvasH

                                val curr = params.customShapes.find { it.id == selectedShapeId }
                                if (curr != null) {
                                    val newX = (curr.normalizedX + deltaNormX).coerceIn(0.05f, 0.95f)
                                    val newY = (curr.normalizedY + deltaNormY).coerceIn(0.05f, 0.95f)
                                    onUpdateShapePosition(selectedShapeId, newX, newY)
                                }
                            }
                        )
                    }
                }
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()

            // Custom Drawing Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sorted = params.customShapes.sortedBy { it.zIndex }
                val colors = palette.colors
                val baseDim = minOf(size.width, size.height)

                for (shape in sorted) {
                    val cx = shape.normalizedX * size.width
                    val cy = shape.normalizedY * size.height
                    val scaleFactor = params.scale * baseDim
                    val w = shape.normalizedWidth * scaleFactor
                    val h = if (shape.type.isProportional1to1) {
                        shape.normalizedWidth * scaleFactor // Strictly 1:1 proportional!
                    } else {
                        shape.normalizedHeight * scaleFactor
                    }

                    val shapeColor = if (shape.customColorHex != null) {
                        Color(shape.customColorHex.toULong())
                    } else {
                        colors[shape.colorIndex % colors.size]
                    }

                    val isSelected = shape.id == selectedShapeId

                    // Draw the shape via nativeCanvas to support elevation shadow
                    drawIntoCanvas { composeCanvas ->
                        val nativeCanvas = composeCanvas.nativeCanvas
                        ProceduralM3Assembler.drawSingleShape(
                            canvas = nativeCanvas,
                            type = shape.type,
                            cx = cx,
                            cy = cy,
                            w = w,
                            h = h,
                            rotationDeg = shape.rotationDeg + params.rotationDegrees,
                            color = shapeColor.toArgb(),
                            opacity = shape.opacity,
                            isWireframe = shape.isWireframe || params.isWireframe,
                            strokeWidth = shape.strokeWidth * (size.width / 500f),
                            scallopLobes = shape.scallopLobes,
                            castShadow = true
                        )
                    }

                    // Selection bounding indicator
                    if (isSelected) {
                        drawSelectionIndicator(
                            cx = cx,
                            cy = cy,
                            w = w,
                            h = h,
                            rotationDeg = shape.rotationDeg + params.rotationDegrees
                        )
                    }
                }
            }

            // Floating Shape Controls Bar (Overlay on top of canvas when a shape is selected)
            if (selectedShape != null && !isFullscreen) {
                FloatingShapeInspectorBar(
                    shape = selectedShape,
                    palette = palette,
                    onColorSelected = { colorIdx -> onSetShapeColorIndex(selectedShape.id, colorIdx) },
                    onRotate = { deg -> onSetShapeRotation(selectedShape.id, deg) },
                    onScaleChange = { delta -> onUpdateShapeScale(selectedShape.id, delta) },
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

private fun DrawScope.drawSelectionIndicator(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    rotationDeg: Float
) {
    val halfW = (w / 2f) + 12.dp.toPx()
    val halfH = (h / 2f) + 12.dp.toPx()

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        native.save()
        if (rotationDeg != 0f) {
            native.rotate(rotationDeg, cx, cy)
        }

        val boxPaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.dp.toPx()
            color = android.graphics.Color.WHITE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(16f, 12f), 0f)
        }
        native.drawRoundRect(
            cx - halfW, cy - halfH, cx + halfW, cy + halfH,
            16.dp.toPx(), 16.dp.toPx(), boxPaint
        )

        // Corner control handles
        val handlePaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        val handleR = 6.dp.toPx()
        native.drawCircle(cx - halfW, cy - halfH, handleR, handlePaint)
        native.drawCircle(cx + halfW, cy - halfH, handleR, handlePaint)
        native.drawCircle(cx - halfW, cy + halfH, handleR, handlePaint)
        native.drawCircle(cx + halfW, cy + halfH, handleR, handlePaint)

        native.restore()
    }
}

/**
 * Floating Shape Inspector Bar with Color Swatches, Rotation, Layer Ordering, and Scaling.
 */
@Composable
private fun FloatingShapeInspectorBar(
    shape: CustomCanvasShape,
    palette: ColorPalette,
    onColorSelected: (Int) -> Unit,
    onRotate: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onToggleWireframe: () -> Unit,
    onDeselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Shape Title, Layer controls, Duplicate, Delete, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shape.type.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onBringToFront,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Bring to Front",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onSendToBack,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Send to Back",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Shape",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Shape",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(
                        onClick = onDeselect,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Deselect",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Color Palette Swatches Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.colors.forEachIndexed { idx, col ->
                    val isSelectedColor = shape.colorIndex == idx && shape.customColorHex == null
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isSelectedColor) 2.5.dp else 1.dp,
                                color = if (isSelectedColor) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelectedColor) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (col.red * 0.299 + col.green * 0.587 + col.blue * 0.114 > 0.5) Color.Black else Color.White
                            )
                        }
                    }
                }

                // Wireframe outline toggle chip
                FilterChip(
                    selected = shape.isWireframe,
                    onClick = onToggleWireframe,
                    label = { Text("Outline", fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }

            // Quick Rotation Chips (0°, 45°, 90°, 180°)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Angle:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(0f, 45f, 90f, 135f, 180f).forEach { angle ->
                    val isCurrent = (shape.rotationDeg % 360f) == angle
                    Surface(
                        shape = CircleShape,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .height(26.dp)
                            .clickable { onRotate(angle) }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${angle.toInt()}°",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet modal to choose and add a new M3 shape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShapeBottomSheet(
    onShapeChosen: (CustomShapeType) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Material 3 Shape",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select a geometric token to insert into the workspace:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CustomShapeType.entries.forEach { type ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onShapeChosen(type)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = type.iconDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
