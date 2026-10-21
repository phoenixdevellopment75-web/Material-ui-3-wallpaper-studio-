package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.ProceduralM3Assembler
import com.example.engine.StudioImageLayer
import com.example.engine.StudioImagePipeline
import com.example.engine.StudioItem
import com.example.engine.StudioTextLayer
import com.example.engine.WallpaperParams
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Reusable zero-allocation paints for selection frame and handles
private val selectionBoxPaint = android.graphics.Paint().apply {
    style = android.graphics.Paint.Style.STROKE
    color = android.graphics.Color.WHITE
    pathEffect = android.graphics.DashPathEffect(floatArrayOf(16f, 12f), 0f)
    isAntiAlias = true
}
private val selectionStemPaint = android.graphics.Paint().apply {
    style = android.graphics.Paint.Style.STROKE
    color = android.graphics.Color.WHITE
    isAntiAlias = true
}
private val selectionOuterHandlePaint = android.graphics.Paint().apply {
    style = android.graphics.Paint.Style.FILL
    color = android.graphics.Color.WHITE
    isAntiAlias = true
    setShadowLayer(8f, 0f, 4f, 0x66000000)
}
private val selectionInnerHandlePaint = android.graphics.Paint().apply {
    style = android.graphics.Paint.Style.FILL
    color = 0xFF6750A4.toInt()
    isAntiAlias = true
}
private val selectionRotHandlePaint = android.graphics.Paint().apply {
    style = android.graphics.Paint.Style.FILL
    color = 0xFF386A20.toInt()
    isAntiAlias = true
}

/**
 * High-performance, zero-friction interactive touch canvas for Studio mode.
 * Supports M3 vector shapes, Nagasaki Depth Text layers, and Picture-in-Shape Image layers.
 * Supports multi-element group selection and unified transforms.
 */
@Composable
fun StudioTouchCanvas(
    params: WallpaperParams,
    selectedShapeId: String?,
    selectedTextId: String? = null,
    selectedImageId: String? = null,
    isMultiSelectMode: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onSelectShape: (String?) -> Unit,
    onSelectTextLayer: (String?) -> Unit = {},
    onSelectImageLayer: (String?) -> Unit = {},
    onCommitShapePosition: (String, Float, Float) -> Unit,
    onCommitShapeScale: (String, Float) -> Unit,
    onCommitShapeDimensions: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitShapeRotation: (String, Float) -> Unit,
    onSetShapeColorIndex: (String, Int) -> Unit,
    onBringShapeToFront: (String) -> Unit,
    onSendShapeToBack: (String) -> Unit,
    onDeleteShape: (String) -> Unit,
    onDuplicateShape: (String) -> Unit,
    onToggleShapeWireframe: (String) -> Unit,
    onUpdateShapeStrokeWidth: (String, Float) -> Unit = { _, _ -> },
    onUpdateOpacity: (String, Float) -> Unit = { _, _ -> },
    onUpdateShadowRadius: (String, Float) -> Unit = { _, _ -> },
    onUpdateBlurRadius: (String, Float) -> Unit = { _, _ -> },
    // Text Layer Callbacks
    onUpdateTextContent: (String, String) -> Unit = { _, _ -> },
    onCommitTextPosition: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitTextScale: (String, Float) -> Unit = { _, _ -> },
    onCommitTextDimensions: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitTextRotation: (String, Float) -> Unit = { _, _ -> },
    onSetTextColorIndex: (String, Int) -> Unit = { _, _ -> },
    onBringTextToFront: (String) -> Unit = {},
    onSendTextToBack: (String) -> Unit = {},
    onDeleteText: (String) -> Unit = {},
    onUpdateTextOpacity: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextShadowRadius: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextTypographyStyle: (String, com.example.engine.StudioTypographyStyle) -> Unit = { _, _ -> },
    onToggleTextOutlineOnly: (String) -> Unit = {},
    onUpdateTextStrokeWidth: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextGlowRadius: (String, Float) -> Unit = { _, _ -> },
    // Image Layer Callbacks
    onCommitImagePosition: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitImageDimensions: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitImageRotation: (String, Float) -> Unit = { _, _ -> },
    onCommitGroupTranslation: (Float, Float) -> Unit = { _, _ -> },
    onCommitGroupScale: (Float) -> Unit = {},
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val minTouchTargetPx = with(density) { 48.dp.toPx() }
    val handleTouchRadiusPx = with(density) { 28.dp.toPx() }

    val palette = params.palette
    val bgColors = if (palette.colors.size >= 2) {
        listOf(palette.colors[0], palette.colors[1])
    } else {
        listOf(Color(0xFF1C1B1F), Color(0xFF2B2930))
    }

    val selectedShape = params.customShapes.find { it.id == selectedShapeId }
    val selectedText = params.customTexts.find { it.id == selectedTextId }
    val selectedImage = params.customImages.find { it.id == selectedImageId }

    // Fast local drag offsets to avoid recomposition friction
    var localDragOffset by remember(selectedShapeId, selectedTextId, selectedImageId, selectedItemIds) { mutableStateOf(Offset.Zero) }
    var activeCornerHandleIndex by remember { mutableIntStateOf(-1) }
    var localScaleMultiplier by remember(selectedShapeId, selectedTextId, selectedImageId) { mutableFloatStateOf(1f) }
    var localNormW by remember(selectedShapeId, selectedTextId, selectedImageId) { mutableFloatStateOf(1f) }
    var localNormH by remember(selectedShapeId, selectedTextId, selectedImageId) { mutableFloatStateOf(1f) }
    var localRotationDeg by remember(selectedShapeId, selectedTextId, selectedImageId) { mutableStateOf<Float?>(null) }
    var activeDragItemId by remember(selectedShapeId, selectedTextId, selectedImageId) { mutableStateOf<String?>(null) }

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
                .pointerInput(selectedShapeId, selectedTextId, selectedImageId, selectedItemIds, params.scale) {
                    detectTransformGestures { _, _, zoom, rotation ->
                        if (isMultiSelectMode && selectedItemIds.isNotEmpty()) {
                            if (zoom != 1f) {
                                onCommitGroupScale(zoom)
                            }
                        } else if (selectedShapeId != null) {
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
                        } else if (selectedTextId != null) {
                            val currText = params.customTexts.find { it.id == selectedTextId }
                            if (currText != null) {
                                if (zoom != 1f) {
                                    val newSize = (currText.normalizedSize * zoom).coerceIn(0.06f, 0.45f)
                                    onCommitTextScale(selectedTextId, newSize)
                                }
                                if (rotation != 0f) {
                                    val newRot = (currText.rotationDeg + rotation) % 360f
                                    onCommitTextRotation(selectedTextId, newRot)
                                }
                            }
                        } else if (selectedImageId != null) {
                            val currImg = params.customImages.find { it.id == selectedImageId }
                            if (currImg != null) {
                                if (zoom != 1f) {
                                    val newW = (currImg.normalizedWidth * zoom).coerceIn(0.12f, 1.4f)
                                    val newH = if (currImg.lockProportions) newW else (currImg.normalizedHeight * zoom).coerceIn(0.12f, 1.4f)
                                    onCommitImageDimensions(selectedImageId, newW, newH)
                                }
                                if (rotation != 0f) {
                                    val newRot = (currImg.rotationDeg + rotation) % 360f
                                    onCommitImageRotation(selectedImageId, newRot)
                                }
                            }
                        }
                    }
                }
                // Tap hit testing & selection for shapes, texts, and photo layers
                .pointerInput(params.customShapes, params.customTexts, params.customImages, isMultiSelectMode, params.scale) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val baseDim = minOf(canvasW, canvasH)

                            val allItems: List<StudioItem> = (params.customShapes + params.customTexts + params.customImages)
                            val topHit = allItems.sortedByDescending { it.zIndex }.firstOrNull { item ->
                                item.contains(tapOffset, canvasW, canvasH, baseDim, params.scale)
                            }

                            if (isMultiSelectMode) {
                                if (topHit != null) {
                                    onToggleItemSelection(topHit.id)
                                }
                            } else {
                                when (topHit) {
                                    null -> {
                                        onSelectShape(null)
                                        onSelectTextLayer(null)
                                        onSelectImageLayer(null)
                                    }
                                    is com.example.engine.CustomCanvasShape -> {
                                        onSelectShape(topHit.id)
                                        onSelectTextLayer(null)
                                        onSelectImageLayer(null)
                                    }
                                    is StudioTextLayer -> {
                                        onSelectTextLayer(topHit.id)
                                        onSelectShape(null)
                                        onSelectImageLayer(null)
                                    }
                                    is StudioImageLayer -> {
                                        onSelectImageLayer(topHit.id)
                                        onSelectShape(null)
                                        onSelectTextLayer(null)
                                    }
                                }
                            }
                        }
                    )
                }
                // Zero-friction drag & direct 8-point resize & stretch handles
                .pointerInput(selectedShapeId, selectedTextId, selectedImageId, selectedItemIds, isMultiSelectMode, params.customShapes, params.customTexts, params.customImages, params.scale) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            localDragOffset = Offset.Zero
                            activeCornerHandleIndex = -1
                            localScaleMultiplier = 1f
                            localRotationDeg = null
                            activeDragItemId = null

                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val baseDim = minOf(canvasW, canvasH)
                            val scaleFactor = params.scale * baseDim

                            // 1. Check if startOffset hits any of the 8 handles + rotation node
                            if (selectedShape != null) {
                                val cx = selectedShape.normalizedX * canvasW
                                val cy = selectedShape.normalizedY * canvasH
                                val isCircle = selectedShape.type == com.example.engine.CustomShapeType.CIRCLE
                                val isLocked = isCircle || selectedShape.lockProportions
                                val w = selectedShape.normalizedWidth * scaleFactor
                                val h = if (isLocked) w else selectedShape.normalizedHeight * scaleFactor
                                val halfW = (w / 2f) + 16.dp.toPx()
                                val halfH = (h / 2f) + 16.dp.toPx()
                                val stemLength = 26.dp.toPx()
                                val rotRad = ((selectedShape.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                val handles = StudioTransformOverlay.calculateHandlePositions(cx, cy, halfW, halfH, stemLength, rotRad)
                                val hitHandle = StudioTransformOverlay.hitTest(startOffset, handles, handleTouchRadiusPx)
                                if (hitHandle != null) {
                                    activeCornerHandleIndex = hitHandle.id
                                    localNormW = selectedShape.normalizedWidth
                                    localNormH = if (isLocked) selectedShape.normalizedWidth else selectedShape.normalizedHeight
                                }
                            } else if (selectedText != null) {
                                val cx = selectedText.normalizedX * canvasW
                                val cy = selectedText.normalizedY * canvasH
                                val bounds = ProceduralM3Assembler.measureTextBounds(selectedText, baseDim, params.scale)
                                val halfW = (bounds.width() / 2f) + 16.dp.toPx()
                                val halfH = (bounds.height() / 2f) + 16.dp.toPx()
                                val stemLength = 26.dp.toPx()
                                val rotRad = ((selectedText.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                val handles = StudioTransformOverlay.calculateHandlePositions(cx, cy, halfW, halfH, stemLength, rotRad)
                                val hitHandle = StudioTransformOverlay.hitTest(startOffset, handles, handleTouchRadiusPx)
                                if (hitHandle != null) {
                                    activeCornerHandleIndex = hitHandle.id
                                    localNormW = selectedText.scaleX
                                    localNormH = selectedText.scaleY
                                }
                            } else if (selectedImage != null) {
                                val cx = selectedImage.normalizedX * canvasW
                                val cy = selectedImage.normalizedY * canvasH
                                val isLocked = selectedImage.lockProportions
                                val w = selectedImage.normalizedWidth * scaleFactor
                                val h = if (isLocked) w else selectedImage.normalizedHeight * scaleFactor
                                val halfW = (w / 2f) + 16.dp.toPx()
                                val halfH = (h / 2f) + 16.dp.toPx()
                                val stemLength = 26.dp.toPx()
                                val rotRad = ((selectedImage.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                val handles = StudioTransformOverlay.calculateHandlePositions(cx, cy, halfW, halfH, stemLength, rotRad)
                                val hitHandle = StudioTransformOverlay.hitTest(startOffset, handles, handleTouchRadiusPx)
                                if (hitHandle != null) {
                                    activeCornerHandleIndex = hitHandle.id
                                    localNormW = selectedImage.normalizedWidth
                                    localNormH = if (isLocked) selectedImage.normalizedWidth else selectedImage.normalizedHeight
                                }
                            }

                            // 2. If NO handle hit, hit-test all StudioItems in zIndex order
                            if (activeCornerHandleIndex == -1) {
                                val allItems: List<StudioItem> = (params.customShapes + params.customTexts + params.customImages)
                                if (isMultiSelectMode && selectedItemIds.isNotEmpty()) {
                                    val touchedAny = allItems.filter { it.id in selectedItemIds }.any {
                                        it.contains(startOffset, canvasW, canvasH, baseDim, params.scale)
                                    }
                                    if (touchedAny) {
                                        activeDragItemId = "MULTI_GROUP"
                                    }
                                } else {
                                    val currSelected = selectedShape ?: selectedText ?: selectedImage
                                    if (currSelected != null && currSelected.contains(startOffset, canvasW, canvasH, baseDim, params.scale)) {
                                        activeDragItemId = currSelected.id
                                    } else {
                                        val topHit = allItems.sortedByDescending { it.zIndex }.firstOrNull { item ->
                                            item.contains(startOffset, canvasW, canvasH, baseDim, params.scale)
                                        }
                                        if (topHit != null) {
                                            activeDragItemId = topHit.id
                                            when (topHit) {
                                                is com.example.engine.CustomCanvasShape -> {
                                                    onSelectShape(topHit.id)
                                                    onSelectTextLayer(null)
                                                    onSelectImageLayer(null)
                                                }
                                                is StudioTextLayer -> {
                                                    onSelectTextLayer(topHit.id)
                                                    onSelectShape(null)
                                                    onSelectImageLayer(null)
                                                }
                                                is StudioImageLayer -> {
                                                    onSelectImageLayer(topHit.id)
                                                    onSelectShape(null)
                                                    onSelectTextLayer(null)
                                                }
                                            }
                                        } else {
                                            activeDragItemId = null
                                        }
                                    }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (activeCornerHandleIndex != -1 || activeDragItemId != null) {
                                change.consume()
                            }
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val baseDim = minOf(canvasW, canvasH)
                            val scaleFactor = params.scale * baseDim

                            if (activeCornerHandleIndex != -1) {
                                if (selectedShape != null) {
                                    val cx = selectedShape.normalizedX * canvasW
                                    val cy = selectedShape.normalizedY * canvasH
                                    val isCircle = selectedShape.type == com.example.engine.CustomShapeType.CIRCLE
                                    val isLocked = isCircle || selectedShape.lockProportions
                                    val rotRad = ((selectedShape.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                    if (activeCornerHandleIndex == 4) { // Rotation handle
                                        val currentTouch = change.position
                                        val angleRad = kotlin.math.atan2(currentTouch.y - cy, currentTouch.x - cx)
                                        var deg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                                        if (deg < 0) deg += 360f
                                        localRotationDeg = deg % 360f
                                    } else if (activeCornerHandleIndex in 0..3) { // 4 Corners - Proportional scale
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val currentDist = sqrt(local.x * local.x + local.y * local.y)
                                        val origW = selectedShape.normalizedWidth * scaleFactor
                                        val origH = if (isLocked) origW else selectedShape.normalizedHeight * scaleFactor
                                        val initialDist = sqrt((origW / 2f) * (origW / 2f) + (origH / 2f) * (origH / 2f)).coerceAtLeast(1f)
                                        val ratio = (currentDist / initialDist).coerceIn(0.2f, 3.5f)
                                        localScaleMultiplier = ratio
                                        localNormW = (selectedShape.normalizedWidth * ratio).coerceIn(0.08f, 1.6f)
                                        localNormH = if (isLocked) localNormW else (selectedShape.normalizedHeight * ratio).coerceIn(0.08f, 1.6f)
                                    } else if (activeCornerHandleIndex == 5 || activeCornerHandleIndex == 7) { // Top/Bottom - Vertical stretch
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val newHalfH = (kotlin.math.abs(local.y) - 16.dp.toPx()).coerceAtLeast(14.dp.toPx())
                                        val newH = newHalfH * 2f
                                        val normH = (newH / scaleFactor).coerceIn(0.08f, 1.6f)
                                        localNormH = normH
                                        if (isLocked) {
                                            localNormW = normH
                                        }
                                    } else if (activeCornerHandleIndex == 6 || activeCornerHandleIndex == 8) { // Left/Right - Horizontal stretch
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val newHalfW = (kotlin.math.abs(local.x) - 16.dp.toPx()).coerceAtLeast(14.dp.toPx())
                                        val newW = newHalfW * 2f
                                        val normW = (newW / scaleFactor).coerceIn(0.08f, 1.6f)
                                        localNormW = normW
                                        if (isLocked) {
                                            localNormH = normW
                                        }
                                    }
                                } else if (selectedText != null) {
                                    val cx = selectedText.normalizedX * canvasW
                                    val cy = selectedText.normalizedY * canvasH
                                    val rotRad = ((selectedText.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                    if (activeCornerHandleIndex == 4) {
                                        val currentTouch = change.position
                                        val angleRad = kotlin.math.atan2(currentTouch.y - cy, currentTouch.x - cx)
                                        var deg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                                        if (deg < 0) deg += 360f
                                        localRotationDeg = deg % 360f
                                    } else if (activeCornerHandleIndex in 0..3) {
                                        val currentDist = sqrt((change.position.x - cx) * (change.position.x - cx) + (change.position.y - cy) * (change.position.y - cy))
                                        val bounds = ProceduralM3Assembler.measureTextBounds(selectedText, baseDim, params.scale)
                                        val initialDist = sqrt((bounds.width() / 2f) * (bounds.width() / 2f) + (bounds.height() / 2f) * (bounds.height() / 2f)).coerceAtLeast(1f)
                                        val ratio = (currentDist / initialDist).coerceIn(0.25f, 3.5f)
                                        localScaleMultiplier = ratio
                                    } else if (activeCornerHandleIndex == 5 || activeCornerHandleIndex == 7) { // Top/Bottom - vertical stretch
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val bounds = ProceduralM3Assembler.measureTextBounds(selectedText.copy(scaleY = 1.0f), baseDim, params.scale)
                                        val initialHalfH = (bounds.height() / 2f).coerceAtLeast(8f)
                                        val currentHalfH = kotlin.math.abs(local.y)
                                        val newScaleY = (currentHalfH / initialHalfH).coerceIn(0.25f, 4.0f)
                                        localNormH = newScaleY
                                    } else if (activeCornerHandleIndex == 6 || activeCornerHandleIndex == 8) { // Left/Right - horizontal stretch
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val bounds = ProceduralM3Assembler.measureTextBounds(selectedText.copy(scaleX = 1.0f), baseDim, params.scale)
                                        val initialHalfW = (bounds.width() / 2f).coerceAtLeast(8f)
                                        val currentHalfW = kotlin.math.abs(local.x)
                                        val newScaleX = (currentHalfW / initialHalfW).coerceIn(0.25f, 4.0f)
                                        localNormW = newScaleX
                                    }
                                } else if (selectedImage != null) {
                                    val cx = selectedImage.normalizedX * canvasW
                                    val cy = selectedImage.normalizedY * canvasH
                                    val isLocked = selectedImage.lockProportions
                                    val rotRad = ((selectedImage.rotationDeg + params.rotationDegrees) * (PI / 180f)).toFloat()

                                    if (activeCornerHandleIndex == 4) {
                                        val currentTouch = change.position
                                        val angleRad = kotlin.math.atan2(currentTouch.y - cy, currentTouch.x - cx)
                                        var deg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                                        if (deg < 0) deg += 360f
                                        localRotationDeg = deg % 360f
                                    } else if (activeCornerHandleIndex in 0..3) {
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val currentDist = sqrt(local.x * local.x + local.y * local.y)
                                        val origW = selectedImage.normalizedWidth * scaleFactor
                                        val origH = if (isLocked) origW else selectedImage.normalizedHeight * scaleFactor
                                        val initialDist = sqrt((origW / 2f) * (origW / 2f) + (origH / 2f) * (origH / 2f)).coerceAtLeast(1f)
                                        val ratio = (currentDist / initialDist).coerceIn(0.2f, 3.5f)
                                        localScaleMultiplier = ratio
                                        localNormW = (selectedImage.normalizedWidth * ratio).coerceIn(0.08f, 1.6f)
                                        localNormH = if (isLocked) localNormW else (selectedImage.normalizedHeight * ratio).coerceIn(0.08f, 1.6f)
                                    } else if (activeCornerHandleIndex == 5 || activeCornerHandleIndex == 7) { // Top/Bottom - vertical stretch
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val newHalfH = (kotlin.math.abs(local.y) - 16.dp.toPx()).coerceAtLeast(14.dp.toPx())
                                        val newH = newHalfH * 2f
                                        val normH = (newH / scaleFactor).coerceIn(0.08f, 1.6f)
                                        localNormH = normH
                                        if (isLocked) {
                                            localNormW = normH
                                        }
                                    } else if (activeCornerHandleIndex == 6 || activeCornerHandleIndex == 8) { // Left/Right - horizontal stretch
                                        val local = StudioTransformOverlay.projectToLocal(change.position, cx, cy, rotRad)
                                        val newHalfW = (kotlin.math.abs(local.x) - 16.dp.toPx()).coerceAtLeast(14.dp.toPx())
                                        val newW = newHalfW * 2f
                                        val normW = (newW / scaleFactor).coerceIn(0.08f, 1.6f)
                                        localNormW = normW
                                        if (isLocked) {
                                            localNormH = normW
                                        }
                                    }
                                }
                            } else if (activeDragItemId != null) {
                                localDragOffset = Offset(
                                    localDragOffset.x + dragAmount.x,
                                    localDragOffset.y + dragAmount.y
                                )
                            }
                        },
                        onDragEnd = {
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()

                            if (activeCornerHandleIndex == 4 && localRotationDeg != null) {
                                if (selectedShape != null) {
                                    onCommitShapeRotation(selectedShape.id, localRotationDeg!!)
                                } else if (selectedText != null) {
                                    onCommitTextRotation(selectedText.id, localRotationDeg!!)
                                } else if (selectedImage != null) {
                                    onCommitImageRotation(selectedImage.id, localRotationDeg!!)
                                }
                            } else if (activeCornerHandleIndex in 0..3 || activeCornerHandleIndex in 5..8) {
                                if (selectedShape != null) {
                                    onCommitShapeDimensions(selectedShape.id, localNormW, localNormH)
                                    onCommitShapeScale(selectedShape.id, localNormW)
                                } else if (selectedText != null) {
                                    if (activeCornerHandleIndex in 0..3) {
                                        val newSize = (selectedText.normalizedSize * localScaleMultiplier).coerceIn(0.06f, 0.45f)
                                        onCommitTextScale(selectedText.id, newSize)
                                    } else if (activeCornerHandleIndex in 5..8) {
                                        onCommitTextDimensions(selectedText.id, localNormW, localNormH)
                                    }
                                } else if (selectedImage != null) {
                                    onCommitImageDimensions(selectedImage.id, localNormW, localNormH)
                                }
                            } else if (activeDragItemId != null && localDragOffset != Offset.Zero) {
                                if (activeDragItemId == "MULTI_GROUP" && isMultiSelectMode) {
                                    onCommitGroupTranslation(localDragOffset.x / canvasW, localDragOffset.y / canvasH)
                                } else {
                                    val shapeTarget = params.customShapes.find { it.id == activeDragItemId }
                                    val textTarget = params.customTexts.find { it.id == activeDragItemId }
                                    val imageTarget = params.customImages.find { it.id == activeDragItemId }

                                    if (shapeTarget != null) {
                                        val rawX = shapeTarget.normalizedX + (localDragOffset.x / canvasW)
                                        val rawY = shapeTarget.normalizedY + (localDragOffset.y / canvasH)
                                        val snapThreshold = 0.02f
                                        val snappedX = if (kotlin.math.abs(rawX - 0.5f) < snapThreshold) {
                                            ExpressiveMotionSpec.performSnapHaptic(haptic)
                                            0.5f
                                        } else rawX
                                        val snappedY = if (kotlin.math.abs(rawY - 0.5f) < snapThreshold) {
                                            ExpressiveMotionSpec.performSnapHaptic(haptic)
                                            0.5f
                                        } else rawY
                                        onCommitShapePosition(shapeTarget.id, snappedX.coerceIn(0.05f, 0.95f), snappedY.coerceIn(0.05f, 0.95f))
                                    } else if (textTarget != null) {
                                        val rawX = textTarget.normalizedX + (localDragOffset.x / canvasW)
                                        val rawY = textTarget.normalizedY + (localDragOffset.y / canvasH)
                                        val snapThreshold = 0.02f
                                        val snappedX = if (kotlin.math.abs(rawX - 0.5f) < snapThreshold) {
                                            ExpressiveMotionSpec.performSnapHaptic(haptic)
                                            0.5f
                                        } else rawX
                                        val snappedY = if (kotlin.math.abs(rawY - 0.5f) < snapThreshold) {
                                            ExpressiveMotionSpec.performSnapHaptic(haptic)
                                            0.5f
                                        } else rawY
                                        onCommitTextPosition(textTarget.id, snappedX.coerceIn(0.05f, 0.95f), snappedY.coerceIn(0.05f, 0.95f))
                                    } else if (imageTarget != null) {
                                        val rawX = imageTarget.normalizedX + (localDragOffset.x / canvasW)
                                        val rawY = imageTarget.normalizedY + (localDragOffset.y / canvasH)
                                        val snapThreshold = 0.02f
                                        val snappedX = if (kotlin.math.abs(rawX - 0.5f) < snapThreshold) {
                                            ExpressiveMotionSpec.performSnapHaptic(haptic)
                                            0.5f
                                        } else rawX
                                        val snappedY = if (kotlin.math.abs(rawY - 0.5f) < snapThreshold) {
                                            ExpressiveMotionSpec.performSnapHaptic(haptic)
                                            0.5f
                                        } else rawY
                                        onCommitImagePosition(imageTarget.id, snappedX.coerceIn(0.05f, 0.95f), snappedY.coerceIn(0.05f, 0.95f))
                                    }
                                }
                            }

                            localDragOffset = Offset.Zero
                            activeCornerHandleIndex = -1
                            localScaleMultiplier = 1f
                            localRotationDeg = null
                            activeDragItemId = null
                        },
                        onDragCancel = {
                            localDragOffset = Offset.Zero
                            activeCornerHandleIndex = -1
                            localScaleMultiplier = 1f
                            localRotationDeg = null
                            activeDragItemId = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val baseDim = minOf(size.width, size.height)
                val colors = palette.colors

                // Unified Render Items sorted by zIndex
                val allItems: List<StudioItem> = (params.customShapes + params.customTexts + params.customImages).sortedBy { it.zIndex }

                for (item in allItems) {
                    when (item) {
                        is com.example.engine.CustomCanvasShape -> {
                            val shape = item
                            val isSelected = shape.id == selectedShapeId || shape.id in selectedItemIds
                            val isActivelyDragged = shape.id == activeDragItemId || (activeDragItemId == "MULTI_GROUP" && shape.id in selectedItemIds)
                            val dragX = if (isActivelyDragged || isSelected) localDragOffset.x else 0f
                            val dragY = if (isActivelyDragged || isSelected) localDragOffset.y else 0f

                            val cx = (shape.normalizedX * size.width) + dragX
                            val cy = (shape.normalizedY * size.height) + dragY
                            val scaleFactor = params.scale * baseDim
                            val effectiveRot = if (isSelected && activeCornerHandleIndex == 4 && localRotationDeg != null) {
                                localRotationDeg!!
                            } else {
                                shape.rotationDeg + params.rotationDegrees
                            }

                            val isCircle = shape.type == com.example.engine.CustomShapeType.CIRCLE
                            val isLocked = isCircle || shape.lockProportions

                            val effectiveNormW = if (isSelected && activeCornerHandleIndex != -1) localNormW else shape.normalizedWidth
                            val effectiveNormH = if (isLocked) effectiveNormW else if (isSelected && activeCornerHandleIndex != -1) localNormH else shape.normalizedHeight

                            val w = effectiveNormW * scaleFactor
                            val h = effectiveNormH * scaleFactor

                            val shapeColor = if (shape.customColorHex != null) {
                                Color(shape.customColorHex.toULong())
                            } else {
                                colors[shape.colorIndex % colors.size]
                            }

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
                                    color = shapeColor.hashCode(),
                                    opacity = shape.opacity,
                                    isWireframe = shape.isWireframe || params.isWireframe,
                                    strokeWidth = shape.strokeWidth * (size.width / 500f),
                                    scallopLobes = shape.scallopLobes,
                                    castShadow = true,
                                    shadowRadius = shape.shadowRadius,
                                    blurRadius = shape.blurRadius
                                )
                            }

                            if (isSelected && !isMultiSelectMode) {
                                with(StudioTransformOverlay) {
                                    draw8PointBoundingBox(
                                        cx = cx,
                                        cy = cy,
                                        w = w,
                                        h = h,
                                        rotationDeg = effectiveRot
                                    )
                                }
                            }
                        }
                        is StudioTextLayer -> {
                            val textLayer = item
                            val isSelected = textLayer.id == selectedTextId || textLayer.id in selectedItemIds
                            val isActivelyDragged = textLayer.id == activeDragItemId || (activeDragItemId == "MULTI_GROUP" && textLayer.id in selectedItemIds)
                            val dragX = if (isActivelyDragged || isSelected) localDragOffset.x else 0f
                            val dragY = if (isActivelyDragged || isSelected) localDragOffset.y else 0f

                            val cx = (textLayer.normalizedX * size.width) + dragX
                            val cy = (textLayer.normalizedY * size.height) + dragY
                            val effectiveScale = if (isSelected && activeCornerHandleIndex in 0..3) localScaleMultiplier else 1f
                            val effectiveScaleX = if (isSelected && (activeCornerHandleIndex == 6 || activeCornerHandleIndex == 8)) localNormW else textLayer.scaleX
                            val effectiveScaleY = if (isSelected && (activeCornerHandleIndex == 5 || activeCornerHandleIndex == 7)) localNormH else textLayer.scaleY
                            val effectiveRot = if (isSelected && activeCornerHandleIndex == 4 && localRotationDeg != null) {
                                localRotationDeg!!
                            } else {
                                textLayer.rotationDeg + params.rotationDegrees
                            }
                            val fontSizePx = textLayer.normalizedSize * baseDim * params.scale * effectiveScale

                            val textColor = if (textLayer.customColorHex != null) {
                                textLayer.customColorHex.toInt()
                            } else {
                                colors[textLayer.colorIndex % colors.size].hashCode()
                            }

                            val runtimeTextLayer = textLayer.copy(scaleX = effectiveScaleX, scaleY = effectiveScaleY)

                            drawIntoCanvas { composeCanvas ->
                                ProceduralM3Assembler.drawSingleText(
                                    canvas = composeCanvas.nativeCanvas,
                                    textLayer = runtimeTextLayer,
                                    cx = cx,
                                    cy = cy,
                                    fontSizePx = fontSizePx,
                                    color = textColor,
                                    rotationDeg = effectiveRot
                                )
                            }

                            if (isSelected && !isMultiSelectMode) {
                                val bounds = ProceduralM3Assembler.measureTextBounds(runtimeTextLayer, baseDim, params.scale * effectiveScale)
                                with(StudioTransformOverlay) {
                                    draw8PointBoundingBox(
                                        cx = cx,
                                        cy = cy,
                                        w = bounds.width(),
                                        h = bounds.height(),
                                        rotationDeg = effectiveRot
                                    )
                                }
                            }
                        }
                        is StudioImageLayer -> {
                            val imageLayer = item
                            val isSelected = imageLayer.id == selectedImageId || imageLayer.id in selectedItemIds
                            val isActivelyDragged = imageLayer.id == activeDragItemId || (activeDragItemId == "MULTI_GROUP" && imageLayer.id in selectedItemIds)
                            val dragX = if (isActivelyDragged || isSelected) localDragOffset.x else 0f
                            val dragY = if (isActivelyDragged || isSelected) localDragOffset.y else 0f

                            val cx = (imageLayer.normalizedX * size.width) + dragX
                            val cy = (imageLayer.normalizedY * size.height) + dragY
                            val scaleFactor = params.scale * baseDim
                            val effectiveRot = if (isSelected && activeCornerHandleIndex == 4 && localRotationDeg != null) {
                                localRotationDeg!!
                            } else {
                                imageLayer.rotationDeg + params.rotationDegrees
                            }

                            val isLocked = imageLayer.lockProportions
                            val effectiveNormW = if (isSelected && activeCornerHandleIndex != -1) localNormW else imageLayer.normalizedWidth
                            val effectiveNormH = if (isLocked) effectiveNormW else if (isSelected && activeCornerHandleIndex != -1) localNormH else imageLayer.normalizedHeight

                            val w = effectiveNormW * scaleFactor
                            val h = effectiveNormH * scaleFactor

                            val bmp = imageLayer.cachedBitmap ?: runCatching {
                                if (java.io.File(imageLayer.imagePath).exists()) {
                                    android.graphics.BitmapFactory.decodeFile(imageLayer.imagePath)
                                } else null
                            }.getOrNull()

                            if (bmp != null) {
                                drawIntoCanvas { composeCanvas ->
                                    StudioImagePipeline.drawPictureInWidget(
                                        canvas = composeCanvas.nativeCanvas,
                                        imageLayer = imageLayer,
                                        bitmap = bmp,
                                        cx = cx,
                                        cy = cy,
                                        w = w,
                                        h = h,
                                        rotationDeg = effectiveRot,
                                        densityMultiplier = size.width / 500f
                                    )
                                }
                            }

                            if (isSelected && !isMultiSelectMode) {
                                with(StudioTransformOverlay) {
                                    draw8PointBoundingBox(
                                        cx = cx,
                                        cy = cy,
                                        w = w,
                                        h = h,
                                        rotationDeg = effectiveRot
                                    )
                                }
                            }
                        }
                    }
                }

                // Draw Group Multi-Selection Bounds
                if (selectedItemIds.size > 1) {
                    val groupBounds = StudioGroupManager.calculateGroupBounds(selectedItemIds, params)
                    if (groupBounds != null) {
                        val gcx = groupBounds.centerNormX * size.width + localDragOffset.x
                        val gcy = groupBounds.centerNormY * size.height + localDragOffset.y
                        val gw = groupBounds.widthNorm * size.width
                        val gh = groupBounds.heightNorm * size.height

                        with(StudioTransformOverlay) {
                            draw8PointBoundingBox(
                                cx = gcx,
                                cy = gcy,
                                w = gw,
                                h = gh,
                                rotationDeg = 0f
                            )
                        }
                    }
                }
            }
        }
    }
}
